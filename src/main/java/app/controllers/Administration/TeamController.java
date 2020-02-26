package app.controllers.Administration;

import app.Models.Player;
import app.Models.PlayerRole;
import app.Models.Team;
import app.Models.User;
import app.Utils.BooleanWrapper;
import app.Utils.MessageGenerator;
import app.exceptions.DerffException;
import app.services.impl.GameServiceImpl;
import app.services.impl.PlayerServiceImpl;
import app.services.impl.TeamServiceImpl;
import app.services.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
public class TeamController {

    @Value("${maxUploadFileSizeTeamSymbol}")
    private Long maxUploadFileSizeTeamSymbol;

    @Value("${maxUploadFileSizePlayerPhoto}")
    private Long maxUploadFileSizePlayerPhoto;

    @Value("${availableFileExtension}")
    private String availableFileExtension;

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    TeamServiceImpl teamService;

    @Autowired
    GameServiceImpl gameService;

    @Autowired
    PlayerServiceImpl playerService;

    @Autowired
    UserServiceImpl userService;

    @GetMapping(value = "teams")
    public String getTeams(Model model) {
        if (messageGenerator.isActive())
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
        model.addAttribute("teams", teamService.findAllTeams());
        return "common/team/teams";
    }

    @GetMapping(value = "teamOverview/{id}")
    public String teamOverview(Model model, @PathVariable("id") long id, Principal principal) {
        User user = new User();
        if (principal != null) {
            user = userService.findUserByLogin(principal.getName());
        }
        if (messageGenerator.isActive())
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
        Team team = teamService.findTeamById(id);
        model.addAttribute("team", team);
        model.addAttribute("goalkeepers", addStatisticToPlayers(playerService.findAllActivePlayersInTeamByRole(team, PlayerRole.GOALKEEPER)));
        model.addAttribute("defenders", addStatisticToPlayers(playerService.findAllActivePlayersInTeamByRole(team, PlayerRole.DEFENDER)));
        model.addAttribute("midfielders", addStatisticToPlayers(playerService.findAllActivePlayersInTeamByRole(team, PlayerRole.MIDFIELDER)));
        model.addAttribute("forwards", addStatisticToPlayers(playerService.findAllActivePlayersInTeamByRole(team, PlayerRole.FORWARD)));
        model.addAttribute("undefineds", addStatisticToPlayers(playerService.findAllActivePlayersInTeamByRole(team, PlayerRole.UNDEFINED)));
        model.addAttribute("user", user);
        return "common/team/teamOverview";
    }

    @GetMapping(value = "administration/deleteTeam/{id}")
    public String deleteTeam(@PathVariable("id") long id) throws DerffException {
        Team team = teamService.findTeamById(id);
        if (!gameService.findGameWithTeam(team).isEmpty()) {
            throw new DerffException("notAvailableDeleteTeamWithGame", null, new Object[]{team.getTeamName()});
        }
        try {
            teamService.delete(team);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.deleteTeam", new Object[]{team.getTeamName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", new Object[]{e.getMessage()});
        }
        return "redirect:/teams";
    }

    @GetMapping(value = "administration/newTeam")
    public String getTeamForm(Model model) {
        Team team = new Team();
        if (messageGenerator.isActive()) {
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator
                    .getTemporaryObjectForMessage().getClass().isInstance(new Team()))
                team = (Team) messageGenerator.getTemporaryObjectForMessageWithSetNull();
        }
        model.addAttribute("team", team);
        return "administration/team/newTeam";
    }

    @PostMapping(value = "administration/newTeam")
    public String newTeam(@ModelAttribute("team") Team team, @ModelAttribute("file") MultipartFile file) throws DerffException {
        validateTeamInformation(team, file, true);
        try {
            teamService.save(team);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.newTeam", new Object[]{team.getTeamName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", team, new Object[]{e.getMessage()});
        }
        return "redirect:/teams";
    }

    @GetMapping(value = "administration/editTeam/{id}")
    public String getTeamForEdit(Model model, @PathVariable("id") long id) {
        Team team = new Team();
        if (messageGenerator.isActive()) {
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator
                    .getTemporaryObjectForMessage().getClass().isInstance(new Team())) {
                team = (Team) messageGenerator.getTemporaryObjectForMessageWithSetNull();
            }
        } else {
            team = teamService.findTeamById(id);
        }
        model.addAttribute("team", team);
        model.addAttribute("needToReplaceFile", new BooleanWrapper(false));
        return "administration/team/editTeam";
    }

    @PostMapping(value = "administration/editTeam/{id}")
    public String saveTeamAfterEdit(@ModelAttribute("team") Team team, @ModelAttribute("file") MultipartFile file, @ModelAttribute("needToReplaceFile") BooleanWrapper needToReplaceFile) throws DerffException {
        validateTeamInformation(team, file, needToReplaceFile.isValue());
        try {
            teamService.save(team);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.editTeam", new Object[]{team.getTeamName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", team, new Object[]{e.getMessage()});
        }
        return "redirect:/teams";
    }

    private List<Player> addStatisticToPlayers(List<Player> source) {
        List<Player> result = new ArrayList<>();
        source.forEach(player -> {
            player.setGoalsCount(player.getGoals().size());
            player.setYellowCardCount((int) player.getOffenses().stream().filter(offense -> offense.getType().equals("YELLOW")).count());
            player.setRedCardCount((int) player.getOffenses().stream().filter(offense -> offense.getType().equals("RED")).count());
            result.add(player);
        });
        return result;
    }

    private void validateTeamInformation(Team team, MultipartFile file, boolean needToReplaceFile) throws DerffException {
        //validate Team name
        if (teamService.findTeamByName(team.getTeamName()) != null && team.getId() == 0) {
            throw new DerffException("notAvailableTeamName", team);
        }

        // File size validation
        if (file.getSize() > maxUploadFileSizeTeamSymbol)
            throw new DerffException("maxUploadFileSizeTeamSymbol", team, new Object[]{maxUploadFileSizeTeamSymbol, file.getSize()});

        // File extension validation
        if (file.getSize() > 0) {
            boolean isCorrectFileExtention = false;
            for (String regex : availableFileExtension.split(";")
            ) {
                if (file.getOriginalFilename().endsWith(regex)) {
                    isCorrectFileExtention = true;
                    break;
                }
            }
            if (!isCorrectFileExtention)
                throw new DerffException("notAvailableFileExtension", team, new Object[]{availableFileExtension});

            //Set byte[] to Team
            try {
                if (team.getSymbolString() == null || needToReplaceFile) {
                    byte[] bytes = file.getBytes();
                    team.setSymbolString("data:image/jpeg;base64, " + Base64Utils.encodeToString(bytes));
                    team.setSymbol(bytes);
                } else if (team.getId() != 0) {
                    team.setSymbol(teamService.findTeamById(team.getId()).getSymbol());
                    team.setSymbolString(teamService.findTeamById(team.getId()).getSymbolString());
                }
            } catch (IOException e) {
                throw new DerffException("fileGetBytes", team, new Object[]{e.getMessage()});
            }
        } else {
            if (!needToReplaceFile && team.getId() != 0) {
                team.setSymbol(teamService.findTeamById(team.getId()).getSymbol());
                team.setSymbolString(teamService.findTeamById(team.getId()).getSymbolString());
            }
        }
    }
}