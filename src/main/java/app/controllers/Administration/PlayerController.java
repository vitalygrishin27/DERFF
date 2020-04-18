package app.controllers.Administration;

import app.Models.Context;
import app.Models.Player;
import app.Models.PlayerRole;
import app.Models.Team;
import app.Utils.BooleanWrapper;
import app.Utils.MessageGenerator;
import app.exceptions.DerffException;
import app.services.impl.PlayerServiceImpl;
import app.services.impl.TeamServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PlayerController {

    @Value("${maxUploadFileSizeTeamSymbol}")
    private Long maxUploadFileSizeTeamSymbol;

    @Value("${maxUploadFileSizePlayerPhoto}")
    private Long maxUploadFileSizePlayerPhoto;

    @Value("${availableFileExtension}")
    private String availableFileExtension;

    @Autowired
    Context context;

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    TeamServiceImpl teamService;

    @Autowired
    PlayerServiceImpl playerService;

    @GetMapping(value = "/administration/players/{id}")
    public String getListContainerWithId(Model model, @PathVariable("id") long id) {
        if (messageGenerator.isActive())
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
        List<Team> teams = teamService.findAllTeams();
        Team teamForUnregisteredPlayers = new Team();
        teamForUnregisteredPlayers.setId(-1L);
        teamForUnregisteredPlayers.setTeamName(messageSource
                .getMessage("label.unregisteredPlayer", null, Locale.getDefault()));
        teams.add(teamForUnregisteredPlayers);
        model.addAttribute("teams", teams);
        model.addAttribute("activeTeamId", id);
        return "administration/player/players";
    }

    @GetMapping(value = "/administration/players")
    public String getListContainer() {
        List<Team> teams = teamService.findAllTeams();
        if (teams.isEmpty()) {
            return "redirect:/administration/players/-1";
        }
        return "redirect:/administration/players/" + teams.get(0).getId();
    }

    @PostMapping(value = "/administration/playerListByTeam/{id}")
    public String getPlayersByTeam(Model model, @PathVariable("id") long id) {
        List<Player> players = new ArrayList();
        if (id == -1) {
            players = playerService.findAllInactivePlayers();
        } else {
            players = playerService.findAllActivePlayersInTeam(teamService.findTeamById(id));
        }
        Collections.sort(players);
        model.addAttribute("players", players);
        return "administration/player/playersByTeam";
    }

    @PostMapping(value = "/administration/playerListByTeamInSelectBox/{id}")
    public String playerListByTeamInSelectBox(Model model, @PathVariable("id") long id) {
        List players = playerService.findAllActivePlayersInTeam(teamService.findTeamById(id));
        Collections.sort(players);
        model.addAttribute("players", players);
        return "administration/player/playersByTeamInSelectBox";
    }

    @GetMapping(value = "/administration/newPlayer/{id}")
    public String getFormforNewPlayer(Model model, @PathVariable("id") long id) {
        Player player = new Player();
        if (messageGenerator.isActive()) {
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator
                    .getTemporaryObjectForMessage().getClass().isInstance(new Player())) {
                player = (Player) messageGenerator.getTemporaryObjectForMessageWithSetNull();
                player.setBirthday(null);
            }
        }
        model.addAttribute("titleNewPlayer", messageSource
                .getMessage("label.newPlayer", new Object[]{teamService.findTeamById(id).getTeamName()}, Locale.getDefault()));
        model.addAttribute("player", player);
        model.addAttribute("playerRoles", getAllRoleList());
        return "administration/player/newPlayer";
    }

    @PostMapping(value = "/administration/newPlayer/{teamId}")
    public String saveNewPlayer(@ModelAttribute("player") Player player,
                                @ModelAttribute("file") MultipartFile file,
                                @PathVariable("teamId") long teamId) throws DerffException {
        validatePlayerInformation(player, teamId, file, false);
        try {
            playerService.save(player);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.newPlayer", new Object[]{player.getFirstName() + " " + player
                            .getLastName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", player, new Object[]{e.getMessage()});
        }
        return "redirect:/administration/newPlayer/" + teamId;
    }

    @GetMapping(value = "/administration/editPlayer/{teamId}/{id}")
    public String getFormforEditPlayer(Model model, @PathVariable("id") long id, HttpServletRequest request) throws DerffException {
        context.putToContext("referer", request.getHeader("referer"));
        Player player = new Player();
        try {
            player = playerService.findPlayerById(id);
        } catch (Exception e) {
            throw new DerffException("playerNotExists", player, new Object[]{id, e.getMessage()}, "/administration/players");
        }
        if (messageGenerator.isActive()) {
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator
                    .getTemporaryObjectForMessage().getClass().isInstance(new Player())) {
                player = (Player) messageGenerator.getTemporaryObjectForMessage();
                player.setBirthday(null);
            }
        }
        model.addAttribute("player", player);
        model.addAttribute("teams", teamService.findAllTeams());
        model.addAttribute("needToReplaceFile", new BooleanWrapper(false));
        return "administration/player/editPlayer";
    }

    @PostMapping(value = "/administration/editPlayer/{teamId}/{id}")
    public String savePlayerAfterEdit(@ModelAttribute("player") Player player,
                                      @ModelAttribute("teamName") String teamName,
                                      @ModelAttribute("file") MultipartFile file,
                                      @PathVariable("teamId") long teamId,
                                      @ModelAttribute("needToReplaceFile") BooleanWrapper needToReplaceFile,
                                      HttpServletRequest request) throws DerffException {
        validatePlayerInformation(player, teamService.findTeamByName(teamName).getId(), file, needToReplaceFile.isValue());
        try {
            playerService.update(player);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.updatePlayer", new Object[]{player.getFirstName() + " " + player
                            .getLastName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", player, new Object[]{e.getMessage()});
        }
        //return "redirect:/administration/players/" + teamId;
        if (context.getContext().containsKey("referer") && context.getFromContext("referer").toString().contains("administration")) {
            return "redirect:/administration/players/" + teamId;
        } else {
            return "redirect:/teamOverview/" + teamId;
        }

    }

    @GetMapping(value = "/administration/deletePlayer/{teamId}/{id}")
    public String deletePlayer(@PathVariable("id") long id, @PathVariable("teamId") long teamId) throws DerffException {
        Player player = new Player();
        try {
            player = playerService.findPlayerById(id);
        } catch (Exception e) {
            throw new DerffException("playerNotExists", player, new Object[]{id, e.getMessage()}, "/administration/players");
        }
        try {
            playerService.delete(player);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.deletePlayer", new Object[]{player.getFirstName() + " " + player
                            .getLastName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", player, new Object[]{e.getMessage()});
        }
        return "redirect:/administration/players/" + teamId;
    }

    private List<String> getAllRoleList() {
        return Arrays.stream(PlayerRole.values()).map(playerRole -> messageSource.getMessage(playerRole.getRole(), null, Locale.getDefault())).collect(Collectors.toList());
    }

    private void validatePlayerInformation(Player player, long id, MultipartFile file, boolean needToReplaceFile) throws DerffException {
        //Date validate
        if (player.getStringBirthday() == null) {
            throw new DerffException("date", player);
        }

        try {
            player.setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse(player.getStringBirthday()));
        } catch (ParseException e) {
            throw new DerffException("date", player);
        }

        //Team validation
        try {
            player.setTeam(teamService.findTeamById(id));

        } catch (Exception e) {
            throw new DerffException("database", player, new Object[]{e.getMessage()});
        }

        //Set photo ifd exists
        try {
            if (player.getId() != 0 && needToReplaceFile) {
                player.setPhoto(null);
                player.setPhotoString(null);
            } else if (player.getId() != 0 && !needToReplaceFile) {
                player.setPhoto(playerService.findPlayerById(player.getId()).getPhoto());
                player.setPhotoString(playerService.findPlayerById(player.getId()).getPhotoString());
            }
        } catch (Exception e) {
            throw new DerffException("fileGetBytes", player, new Object[]{e.getMessage()});
        }


        // Id card validation
        if (player.getIdCard() != null && playerService.getPlayerByIdCard(player.getIdCard()) != null) {
            throw new DerffException("IdCardNotCorrect", player);
        }

        // File size validation
        if (file.getSize() > maxUploadFileSizePlayerPhoto)
            throw new DerffException("maxUploadFileSizePlayerPhoto", player, new Object[]{maxUploadFileSizePlayerPhoto, file.getSize()});

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
                throw new DerffException("notAvailableFileExtension", player, new Object[]{availableFileExtension});
            try {
                byte[] bytes = file.getBytes();
                player.setPhotoString("data:image/jpeg;base64, " + Base64Utils.encodeToString(bytes));
                player.setPhoto(bytes);
            } catch (IOException e) {
                throw new DerffException("fileGetBytes", player, new Object[]{e.getMessage()});
            }
        }
    }
}