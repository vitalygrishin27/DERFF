package app.controllers.Administration;

import app.Models.*;
import app.Utils.BooleanWrapper;
import app.Utils.MessageGenerator;
import app.exceptions.DerffException;
import app.services.impl.*;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

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
    ManualSkipGameServiceImpl manualSkipGameService;

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    private static final Logger logger = Logger.getLogger(GameController.class);

    @GetMapping(value = "/administration/calendar")
    public String getCalendar(Model model) {
        logger.info("Calendar is called");
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


    @PostMapping(value = "/administration/gameUpcomingList")
    public String getUpcomingGames(Model model) throws DerffException {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        Date resultDate = cal.getTime();
        for (Game game : gameService.findAllGames()
        ) {
            if (game.getDate().before(resultDate)) {
                resultDate = game.getDate();
            }
        }
        return getGamesByDate(model, new SimpleDateFormat("yyyy-MM-dd").format(resultDate), "date", -1L);
    }

    @PostMapping(value = "/administration/gameListByDate")
    public String getGamesByDate(Model model, @ModelAttribute("date") String stringDate, @ModelAttribute("round") String round, @ModelAttribute("competitionId") Long competitionId) throws DerffException {
        List<Game> games = new ArrayList<>();
        try {
            switch (round) {
                case "date":
                    Date date;
                    Date dateTo;
                    try {
                        date = new SimpleDateFormat("yyyy-MM-dd").parse(stringDate);
                        Calendar instance = Calendar.getInstance();
                        instance.setTime(date); //устанавливаем дату, с которой будет производить операции
                        instance.add(Calendar.DAY_OF_MONTH, 1);// прибавляем 3 дня к установленной дате
                        dateTo = instance.getTime(); // получаем измененную дату
                    } catch (ParseException e) {
                        break;
                    }
                    if (competitionId == -1) {
                        games = gameService.findGamesBetweenDates(date, dateTo);
                    } else {
                        games = gameService.findGamesBetweenDatesAndCompetition(date, dateTo, competitionService.findCompetitionById(competitionId));
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
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(stringDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.HOUR, 6);
            game.setDate(cal.getTime());
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

    @GetMapping(value = "/administration/listSkipGames")
    public String getFormForSkipGamesManually(Model model) {
        model.addAttribute("manualSkipGames", manualSkipGameService.findAll());
        return "administration/game/skipGamesManually";
    }

    @GetMapping(value = "/administration/newManualSkipGame")
    public String getFormForNewSkipGamesManually(Model model) {
        model.addAttribute("manualSkipGame", new ManualSkipGame());
        model.addAttribute("teams", teamService.findAllTeams());
        return "administration/game/newManualSkipGame";
    }

    @PostMapping(value = "/administration/newManualSkipGame")
    public String setNewManualSkipGame(@ModelAttribute("manualSkipGame") ManualSkipGame manualSkipGame) throws ParseException {
        setDate(manualSkipGame);
        manualSkipGameService.save(manualSkipGame);
        return "redirect:/administration/listSkipGames";
    }

    @GetMapping(value = "/administration/deleteSkipGame/{id}")
    public String deleteSkipGame(@PathVariable("id") long id, HttpServletRequest request) throws DerffException {
        manualSkipGameService.delete(manualSkipGameService.findById(id));
        return "redirect:/administration/listSkipGames";
    }

    private void setDate(ManualSkipGame manualSkipGame) throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(manualSkipGame.getStringStartDate());
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, 6);
        manualSkipGame.setStartDate(cal.getTime());

       date = new SimpleDateFormat("yyyy-MM-dd").parse(manualSkipGame.getStringEndDate());
        cal.setTime(date);
        cal.add(Calendar.HOUR, 6);
        manualSkipGame.setEndDate(cal.getTime());
    }

}
