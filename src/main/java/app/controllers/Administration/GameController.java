package app.controllers.Administration;

import app.Models.*;
import app.Utils.MessageGenerator;
import app.exceptions.DerffException;
import app.services.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static app.Utils.ConfigurationKey.*;
import static app.Utils.ConfigurationKey.SECOND_ROUND_END;

@Controller
public class GameController {
    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    GameServiceImpl gameService;

    @Autowired
    TeamServiceImpl teamService;

    @Autowired
    ConfigurationImpl configurationService;

    @Autowired
    CompetitionServiceImpl competitionService;

    @Autowired
    OffenseServiceImpl offenseService;

    @Autowired
    GoalServiceImpl goalService;

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @GetMapping(value = "/administration/calendar")
    public String getCalendar(Model model) {
        if (messageGenerator.isActive()) {
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
        }
        List<Competition> competitions = competitionService.findAllCompetition();
        competitions.add(new Competition(-1, messageSource.getMessage("label.competitions.all", null, Locale.getDefault()), null));
        Map<Long, String> comp = new HashMap<>();
        for (Competition competition : competitions
        ) {
            comp.put(competition.getId(), competition.getName());
        }
        model.addAttribute("competitions", comp);
        return "administration/game/calendar";
    }

    @PostMapping(value = "/administration/gameListByDate")
    public String getGamesByDate(Model model, @ModelAttribute("date") String stringDate, @ModelAttribute("round") String round, @ModelAttribute("competitionId") Long competitionId) throws DerffException {
        List<Game> games = new ArrayList<>();
        try {
            switch (round) {
                case "date":
                    Date date;
                    try {
                        date = new SimpleDateFormat("yyyy-MM-dd").parse(stringDate);
                    } catch (ParseException e) {
                        break;
                    }
                    if (competitionId == -1) {
                        games = gameService.findGamesByDate(date);
                    } else {
                        games = gameService.findGamesByDateAndCompetition(date, competitionService.findCompetitionById(competitionId));
                    }
                    break;
                case "first":
                    if (competitionId == -1) {
                        games = gameService.findGamesBetweenDates(new SimpleDateFormat("yyyy-MM-dd")
                                .parse(configurationService.getValue(FIRST_ROUND_BEGIN)), new SimpleDateFormat("yyyy-MM-dd")
                                .parse(configurationService.getValue(FIRST_ROUND_END)));
                    } else {
                        games = gameService.findGamesBetweenDatesAndCompetition(new SimpleDateFormat("yyyy-MM-dd")
                                .parse(configurationService.getValue(FIRST_ROUND_BEGIN)), new SimpleDateFormat("yyyy-MM-dd")
                                .parse(configurationService.getValue(FIRST_ROUND_END)), competitionService.findCompetitionById(competitionId));
                    }
                    break;
                case "second":
                    if (competitionId == -1) {
                        games = gameService.findGamesBetweenDates(new SimpleDateFormat("yyyy-MM-dd")
                                .parse(configurationService
                                        .getValue(SECOND_ROUND_BEGIN)), new SimpleDateFormat("yyyy-MM-dd")
                                .parse(configurationService.getValue(SECOND_ROUND_END)));
                    } else {
                        games = gameService.findGamesBetweenDatesAndCompetition(new SimpleDateFormat("yyyy-MM-dd")
                                .parse(configurationService
                                        .getValue(SECOND_ROUND_BEGIN)), new SimpleDateFormat("yyyy-MM-dd")
                                .parse(configurationService.getValue(SECOND_ROUND_END)), competitionService.findCompetitionById(competitionId));
                    }
                    break;
                case "all":
                    if (competitionId == -1) {
                        games = gameService.findAllGames();
                    } else {
                        games = gameService.findAllGamesByCompetition(competitionService.findCompetitionById(competitionId));
                    }
                    break;
            }
        } catch (Exception e) {
            throw new DerffException("database");
        }
        model.addAttribute("games", games);
        return "administration/game/gamesByDate";
    }


    @GetMapping(value = "/administration/newGame")
    public String getFormforNewGame(Model model) {
        Game game = new Game();
        Team team = new Team();
        game.setMasterTeam(team);
        game.setSlaveTeam(team);
        game.setDate(new Date());
        if (messageGenerator.isActive()) {
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator
                    .getTemporaryObjectForMessage().getClass().isInstance(new Game())) {
                game = (Game) messageGenerator.getTemporaryObjectForMessage();
            }
        }
        model.addAttribute("game", game);
        model.addAttribute("stringDate", "");
        model.addAttribute("teams", teamService.findAllTeams());
        model.addAttribute("competitions", competitionService.findAllCompetition());
        return "administration/game/newGame";
    }


    @PostMapping(value = "/administration/newGame")
    public String saveNewGame(@ModelAttribute("game") Game game,
                              @ModelAttribute("masterTeamName") String masterTeamName,
                              @ModelAttribute("slaveTeamName") String slaveTeamName,
                              @ModelAttribute("stringDate") String stringDate) throws DerffException {

        validateGameInformation(game, masterTeamName, slaveTeamName, stringDate);
        try {
            gameService.save(game);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.newGame", new Object[]{game.getMasterTeam().getTeamName() + " - " +
                            game.getSlaveTeam().getTeamName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", game, new Object[]{e.getMessage()});
        }
        return "redirect:/administration/newGame";
    }

    private void validateGameInformation(Game game, String masterTeamName, String slaveTeamName, String stringDate) throws DerffException {
        //validate Teams
        if (masterTeamName.equals(slaveTeamName)) {
            throw new DerffException("sameTeam", game);
        }
        game.setMasterTeam(teamService.findTeamByName(masterTeamName));
        game.setSlaveTeam(teamService.findTeamByName(slaveTeamName));
        game.setStringDate(stringDate);
        try {
            game.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(stringDate));
        } catch (ParseException e) {
            throw new DerffException("date", game);
        }
        game.setMasterGoalsCount(0);
        game.setSlaveGoalsCount(0);
        //Validate competition
        try {
            game.setCompetition(competitionService.findCompetitionByName(game.getCompetition().getName()));
        } catch (Exception e) {
            throw new DerffException("database", game, new Object[]{e.getMessage()});
        }
    }

    @GetMapping(value = "/administration/editGame/{id}")
    public String getFormForEditGame(Model model, @PathVariable("id") long id) throws DerffException {
        Game game = new Game();
        try {
            game = gameService.findGameById(id);
        } catch (Exception e) {
            throw new DerffException("gameNotExists", game, new Object[]{id, e.getMessage()}, "/administration/calendar");
        }

        if (messageGenerator.isActive()) {
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator
                    .getTemporaryObjectForMessage().getClass().isInstance(new Game())) {
                game = (Game) messageGenerator.getTemporaryObjectForMessage();
            }
        }
        model.addAttribute("game", game);
        model.addAttribute("teams", teamService.findAllTeams());
        model.addAttribute("competitions", competitionService.findAllCompetition());
        return "administration/game/editGame";
    }

    @PostMapping(value = "/administration/editGame/{id}")
    public String saveGameAfterEdit(@ModelAttribute("game") Game game,
                                      @ModelAttribute("masterTeamName") String masterTeamName,
                                      @ModelAttribute("slaveTeamName") String slaveTeamName,
                                      @ModelAttribute("stringDate") String stringDate
    ) throws DerffException {

        validateGameInformation(game, masterTeamName, slaveTeamName, stringDate);
        try {
            gameService.save(game);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.editGame", new Object[]{game.getMasterTeam().getTeamName() + " - " +
                            game.getSlaveTeam().getTeamName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", game, new Object[]{e.getMessage()});
        }

        return "redirect:/administration/calendar";
    }


    @PostMapping(value = "/administration/deleteGame")
    public String deleteGames(HttpServletRequest request) throws DerffException {
        List<String> gamesIdForDelete = new ArrayList<>();
        Collections.addAll(gamesIdForDelete, request.getParameterValues("gameIdForDelete[]"));
        for (String s : gamesIdForDelete
        ) {
            deleteGame(Long.valueOf(s));
        }
        messageGenerator.setMessage((messageSource.getMessage("success.deleteGames", new Object[]{gamesIdForDelete.size()}, Locale.getDefault())));

        return "administration/game/calendar";
    }

    private void deleteGame(long id) throws DerffException {
        Game game = new Game();
        try {
            game = gameService.findGameById(id);
        } catch (Exception e) {
            throw new DerffException("gameNotExists", game, new Object[]{id, e.getMessage()}, "/administration/calendar");
        }
        try {
            for (Offense offense : game.getOffenses()
            ) {
                offenseService.delete(offense);
            }
            for (Goal goal : game.getGoals()
            ) {
                goalService.delete(goal);
            }

            gameService.delete(game);
        } catch (Exception e) {
            throw new DerffException("database", game, new Object[]{e.getMessage()});
        }

    }

}
