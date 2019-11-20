package app.controllers;

import app.Models.Game;
import app.Models.Team;
import app.Utils.MessageGenerator;
import app.exceptions.DerffException;
import app.services.impl.GameServiceImpl;
import app.services.impl.TeamServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Controller
public class AdministrationController {

    @Value("${maxUploadFileSizeTeamSymbol}")
    private Long maxUploadFileSizeTeamSymbol;

    @Value("${availableFileExtension}")
    private String availableFileExtension;

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    TeamServiceImpl teamService;

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    GameServiceImpl gameService;

    @GetMapping(value = "/administration/teams")
    public String getTeams(Model model) throws DerffException {
        if (messageGenerator.isActive())
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());
        model.addAttribute("teams", teamService.findAllTeams());
        return "administration/teams";
    }

    @DeleteMapping(value = "/administration/teams")
    public void deleteTeam(@ModelAttribute("teamId") Long teamId) throws DerffException {
        Team team = teamService.findTeamById(teamId);
        if(!gameService.findGameWithTeam(team).isEmpty()){
            // TODO: 20.11.2019 циклический вызов
            throw new DerffException("notAvailableDeleteTeamWithGame",null,new Object[]{team.getTeamName()});
        }
        try {

            teamService.delete(team);
            messageGenerator.setMessage((messageSource.getMessage("success.deleteTeam", new Object[]{team.getTeamName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", new Object[]{e.getMessage()});
        }
    }

    @GetMapping(value = "/newTeam")
    public String getTeamForm(Model model) throws DerffException {
        Team team = new Team();
        if (messageGenerator.isActive()) {
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator.getTemporaryObjectForMessage().getClass().isInstance(new Team()))
                team = (Team) messageGenerator.getTemporaryObjectForMessageWithSetNull();
        }
        model.addAttribute("team", team);
        return "administration/newTeam";
    }

    @PostMapping(value = "/newTeam")
    public String newTeam(@ModelAttribute("team") Team team, @ModelAttribute("file") MultipartFile file) throws DerffException {
        validateTeamInformation(team, file);
        try {
            teamService.save(team);
            messageGenerator.setMessage((messageSource.getMessage("success.newTeam", new Object[]{team.getTeamName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", team, new Object[]{e.getMessage()});
        }
        return "redirect:/administration/teams";
    }


    private void validateTeamInformation(Team team, MultipartFile file) throws DerffException {
        //validate Team name
        if (teamService.findTeamByName(team.getTeamName()) != null) {
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
                byte[] bytes = file.getBytes();

                team.setSymbolString("data:image/jpeg;base64, " + Base64Utils.encodeToString(bytes));
                team.setSymbol(bytes);
            } catch (IOException e) {
                throw new DerffException("fileGetBytes", team, new Object[]{e.getMessage()});
            }
        }


    }


    @GetMapping(value = "/administration/calendar")
    public String getCalendar(Model model) throws DerffException {
        Game game = new Game();
        if (messageGenerator.isActive()) {
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator.getTemporaryObjectForMessage().getClass().isInstance(new Game())) {
                game = (Game) messageGenerator.getTemporaryObjectForMessageWithSetNull();
                model.addAttribute("preDate", dateToString(game.getDate()));
            }
        }

        model.addAttribute("game", game);
        model.addAttribute("games", gameService.findAllGames());
        model.addAttribute("teams", teamService.findAllTeams());
        return "administration/calendar";
    }

    @PostMapping(value = "/administration/calendar")
    public String addGame(@ModelAttribute("preDate") String preDate, @ModelAttribute("masterTeamName") String masterTeamName, @ModelAttribute("slaveTeamName") String slaveTeamName) throws DerffException {
        Game newGame = new Game();
        validateTeamInformation(newGame, preDate, masterTeamName, slaveTeamName);
        try {
            gameService.save(newGame);
            messageGenerator.setMessage((messageSource.getMessage("success.newGame", null, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", newGame, new Object[]{e.getMessage()});
        }
        return "redirect:/administration/calendar";
    }


    private void validateTeamInformation(Game newGame, String preDate, String masterTeamName, String slaveTeamName) throws DerffException {

        Team master;
        Team slave;

        //Date parse
        try {
            newGame.setDate(new SimpleDateFormat("dd/MM/yyyy").parse(preDate));
        } catch (Exception e) {
            throw new DerffException("date", newGame);
        }


        // validate teams
        try {
            master = teamService.findTeamByName(masterTeamName);
            slave = teamService.findTeamByName(slaveTeamName);
            newGame.setMasterTeam(master);
            newGame.setSlaveTeam(slave);
        } catch (Exception e) {
            throw new DerffException("game");
        }

        // validate not same teams
        if (master.getId() == slave.getId()) {
            throw new DerffException("sameTeams", newGame);
        }


    }

    private String dateToString(Date date) {
        Format formatter = new SimpleDateFormat("dd/MM/yyyy");
        return date == null ? null : formatter.format(date);
    }

}
