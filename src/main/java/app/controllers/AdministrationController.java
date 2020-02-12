package app.controllers;

import app.Models.*;
import app.Utils.BooleanWrapper;
import app.Utils.MessageGenerator;
import app.exceptions.DerffException;
import app.services.impl.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static app.Utils.ConfigurationKey.*;

@Controller
public class AdministrationController {

    @Value("${maxUploadFileSizeTeamSymbol}")
    private Long maxUploadFileSizeTeamSymbol;

    @Value("${maxUploadFileSizePlayerPhoto}")
    private Long maxUploadFileSizePlayerPhoto;

    @Value("${availableFileExtension}")
    private String availableFileExtension;

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    TeamServiceImpl teamService;

    @Autowired
    PlayerServiceImpl playerService;

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    GameServiceImpl gameService;

    @Autowired
    GoalServiceImpl goalService;

    @Autowired
    OffenseServiceImpl offenseService;

    @Autowired
    ConfigurationImpl configurationService;

    @Autowired
    Context context;

    @GetMapping(value = "/")
    public String getMainPage(Model model) throws DerffException {
        if (messageGenerator.isActive())
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());

        return "administration/mainPage";
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


    /*   @DeleteMapping(value = "/administration/calendar")
       public void deleteGame(Integer gameId) throws DerffException {
           Game game = gameService.findGameById(gameId);
           try {
               gameService.delete(game);
               messageGenerator.setMessage((messageSource.getMessage("success.deleteGame", null, Locale.getDefault())));
           } catch (Exception e) {
               throw new DerffException("database", game, new Object[]{e.getMessage()});
           }

       }
   */





    @GetMapping(value = "/administration/newGame")
    public String getFormforNewGame(Model model) throws DerffException {
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

            // model.addAttribute("preDate", dateToString(((Player)obj).getBirthday()));

        }
        model.addAttribute("game", game);
        model.addAttribute("stringDate", "");
        model.addAttribute("teams", teamService.findAllTeams());

        // model.addAttribute("players", playerService.findAllPlayers());
        // model.addAttribute("players", players);
        return "administration/newGame";
    }


    @PostMapping(value = "/administration/newGame")
    public String saveNewGame(@ModelAttribute("game") Game game,
                              // @ModelAttribute("team") Team team,
                              // @ModelAttribute("preDate") String preDate,
                              @ModelAttribute("masterTeamName") String masterTeamName,
                              @ModelAttribute("slaveTeamName") String slaveTeamName,
                              @ModelAttribute("stringDate") String stringDate) throws DerffException
    // @ModelAttribute("isLegionary") String isLegionary,
    // @ModelAttribute("file") MultipartFile file)
    {

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

    @GetMapping(value = "/administration/editGame/{id}")
    public String getFormforEditGame(Model model, @PathVariable("id") long id) throws DerffException {
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
        model.addAttribute("titlePage", messageSource.getMessage("page.title.game.editing", null, Locale.getDefault()));

        return "administration/editGame";
    }

    @PostMapping(value = "/administration/editGame/{id}")
    public String savePlayerAfterEdit(@ModelAttribute("game") Game game,
                                      @ModelAttribute("masterTeamName") String masterTeamName,
                                      @ModelAttribute("slaveTeamName") String slaveTeamName,
                                      @ModelAttribute("stringDate") String stringDate
                                      // @ModelAttribute("isLegionary") String isLegionary,
                                      //  @ModelAttribute("file") MultipartFile file
    ) throws DerffException {

        validateGameInformation(game, masterTeamName, slaveTeamName, stringDate);
        try {
            gameService.update(game);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.editGame", new Object[]{game.getMasterTeam().getTeamName() + " - " +
                            game.getSlaveTeam().getTeamName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", game, new Object[]{e.getMessage()});
        }

        return "redirect:/administration/calendar";
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

    @PostMapping(value = "/administration/deleteGame")
    public String deleteGames(HttpServletRequest request) throws DerffException {
        List<String> gamesIdForDelete = new ArrayList<>();
        Collections.addAll(gamesIdForDelete, request.getParameterValues("gameIdForDelete[]"));
        for (String s : gamesIdForDelete
        ) {
            deleteGame(Long.valueOf(s));
        }
        messageGenerator.setMessage((messageSource.getMessage("success.deleteGames", new Object[]{gamesIdForDelete.size()}, Locale.getDefault())));

        return "administration/calendar";
    }


    @GetMapping(value = "/administration/resultGame/{id}")
    public String firstStepResultsGoalsCount(Model model, @PathVariable("id") long id) throws DerffException {
        context.clear();
        Game game = gameService.findGameById(id);
        if (!game.isResultSave()) {
            context.putToContext("game", game);
            model.addAttribute("masterTeamName", game.getMasterTeam().getTeamName());
            model.addAttribute("slaveTeamName", game.getSlaveTeam().getTeamName());
            model.addAttribute("countGoalsMasterTeam", 0);
            model.addAttribute("countGoalsSlaveTeam", 0);
            return "administration/resultGame";
        } else {
            throw new DerffException("gameResultsAreAlreadyExists", null, null, "/administration/calendar");
        }

    }

    @PostMapping(value = "/administration/resultGame")
    public String saveCountOfGoals(HttpServletRequest request, Model model,
                                   @ModelAttribute("step") String step,
                                   @ModelAttribute("countGoalsMasterTeam") String countGoalsMasterTeam,
                                   @ModelAttribute("countGoalsSlaveTeam") String countGoalsSlaveTeam,
                                   @ModelAttribute("countYellowCardsMasterTeam") String countYellowCardsMasterTeam,
                                   @ModelAttribute("countYellowCardsSlaveTeam") String countYellowCardsSlaveTeam,
                                   @ModelAttribute("countRedCardsMasterTeam") String countRedCardsMasterTeam,
                                   @ModelAttribute("countRedCardsSlaveTeam") String countRedCardsSlaveTeam
    ) throws DerffException {
        Game game = (Game) context.getFromContext("game");
        if (step.equals("goalsCount") &&
                (countGoalsMasterTeam.equals("") || countGoalsMasterTeam.equals("0")) &&
                (countGoalsSlaveTeam.equals("") || countGoalsSlaveTeam.equals("0"))) {
            game.setGoals(new ArrayList<Goal>());
            return "administration/resultGameYellowCardsCount";
        }
        if (step.equals("yellowCardsCount") &&
                (countYellowCardsMasterTeam.equals("") || countYellowCardsMasterTeam.equals("0")) &&
                (countYellowCardsSlaveTeam.equals("") || countYellowCardsSlaveTeam.equals("0"))) {
            game.setOffenses(new ArrayList<Offense>());
            return "administration/resultGameRedCardsCount";
        }
        if (step.equals("redCardsCount") &&
                (countRedCardsMasterTeam.equals("") || countRedCardsMasterTeam.equals("0")) &&
                (countRedCardsSlaveTeam.equals("") || countRedCardsSlaveTeam.equals("0"))) {
            step = "saveResult";
        }


        switch (step) {
            case "goalsCount":
                if (countGoalsMasterTeam.equals("")) countGoalsMasterTeam = "0";
                if (countGoalsSlaveTeam.equals("")) countGoalsSlaveTeam = "0";
                game.setMasterGoalsCount(Integer.valueOf(countGoalsMasterTeam));
                game.setSlaveGoalsCount(Integer.valueOf(countGoalsSlaveTeam));

                model.addAttribute("masterTeamName", game.getMasterTeam().getTeamName());
                model.addAttribute("slaveTeamName", game.getSlaveTeam().getTeamName());
                model.addAttribute("countMasterGoals", countGoalsMasterTeam);
                model.addAttribute("countSlaveGoals", countGoalsSlaveTeam);
                model.addAttribute("masterTeamPlayersMap", getFullNamePlayersMap(playerService
                        .findAllPlayersInTeam(game.getMasterTeam())));
                model.addAttribute("slaveTeamPlayersMap", getFullNamePlayersMap(playerService
                        .findAllPlayersInTeam(game.getSlaveTeam())));
                return "administration/resultGameGoalsPlayers";
            case "goalsPlayers":
                ArrayList<String> masterPlayerIdListGoals = new ArrayList<>();
                List<Goal> goals = new ArrayList<>();
                if (request.getParameterValues("masterPlayerIdListGoals[]") != null) {
                    Collections
                            .addAll(masterPlayerIdListGoals, request.getParameterValues("masterPlayerIdListGoals[]"));
                    for (String id : masterPlayerIdListGoals
                    ) {
                        Goal goal = new Goal();
                        goal.setTeam(game.getMasterTeam());
                        goal.setGame(game);
                        goal.setPlayer(playerService.getPlayerById(Long.valueOf(id)));
                        goals.add(goal);
                    }
                }
                ArrayList<String> slavePlayerIdListGoals = new ArrayList<>();
                if (request.getParameterValues("slavePlayerIdListGoals[]") != null) {
                    Collections.addAll(slavePlayerIdListGoals, request.getParameterValues("slavePlayerIdListGoals[]"));
                    for (String id : slavePlayerIdListGoals
                    ) {
                        Goal goal = new Goal();
                        goal.setTeam(game.getSlaveTeam());
                        goal.setGame(game);
                        goal.setPlayer(playerService.getPlayerById(Long.valueOf(id)));
                        goals.add(goal);
                    }
                }
                game.setGoals(goals);
                //   context.putToContext("game",game);
                return "administration/resultGameYellowCardsCount";
            case "yellowCardsCount":
                if (countYellowCardsMasterTeam.equals("")) countYellowCardsMasterTeam = "0";
                if (countYellowCardsSlaveTeam.equals("")) countYellowCardsSlaveTeam = "0";
                model.addAttribute("countYellowCardsMasterTeam", countYellowCardsMasterTeam);
                model.addAttribute("countYellowCardsSlaveTeam", countYellowCardsSlaveTeam);
                model.addAttribute("masterTeamPlayersMap", getFullNamePlayersMap(playerService
                        .findAllPlayersInTeam(game.getMasterTeam())));
                model.addAttribute("slaveTeamPlayersMap", getFullNamePlayersMap(playerService
                        .findAllPlayersInTeam(game.getSlaveTeam())));
                return "administration/resultGameYellowCardsPlayer";
            case "yellowCardsPlayers":
                ArrayList<String> masterPlayerIdListYellowCards = new ArrayList<>();
                List<Offense> offenses = new ArrayList<>();
                if (request.getParameterValues("masterPlayerIdListYellowCards[]") != null) {
                    Collections.addAll(masterPlayerIdListYellowCards, request
                            .getParameterValues("masterPlayerIdListYellowCards[]"));
                    for (String id : masterPlayerIdListYellowCards
                    ) {
                        Offense offense = new Offense();
                        offense.setGame(game);
                        offense.setType("YELLOW");
                        offense.setPlayer(playerService.getPlayerById(Long.valueOf(id)));
                        offenses.add(offense);
                    }
                }
                ArrayList<String> slavePlayerIdListYellowCards = new ArrayList<>();
                if (request.getParameterValues("slavePlayerIdListYellowCards[]") != null) {
                    Collections.addAll(slavePlayerIdListYellowCards, request
                            .getParameterValues("slavePlayerIdListYellowCards[]"));
                    for (String id : slavePlayerIdListYellowCards
                    ) {
                        Offense offense = new Offense();
                        offense.setGame(game);
                        offense.setType("YELLOW");
                        offense.setPlayer(playerService.getPlayerById(Long.valueOf(id)));
                        offenses.add(offense);
                    }
                }
                game.setOffenses(offenses);
                //context.putToContext("offenses", offenses);
                return "administration/resultGameRedCardsCount";
            case "redCardsCount":
                if (countRedCardsMasterTeam.equals("")) countRedCardsMasterTeam = "0";
                if (countRedCardsSlaveTeam.equals("")) countRedCardsSlaveTeam = "0";
                model.addAttribute("countRedCardsMasterTeam", countRedCardsMasterTeam);
                model.addAttribute("countRedCardsSlaveTeam", countRedCardsSlaveTeam);
                model.addAttribute("masterTeamPlayersMap", getFullNamePlayersMap(playerService
                        .findAllPlayersInTeam(game.getMasterTeam())));
                model.addAttribute("slaveTeamPlayersMap", getFullNamePlayersMap(playerService
                        .findAllPlayersInTeam(game.getSlaveTeam())));
                return "administration/resultGameRedCardsPlayer";
            case "saveResult":
                ArrayList<String> masterPlayerIdListRedCards = new ArrayList<>();
                List<Offense> offensesRed = new ArrayList<>();
                if (request.getParameterValues("masterPlayerIdListRedCards[]") != null) {
                    Collections.addAll(masterPlayerIdListRedCards, request
                            .getParameterValues("masterPlayerIdListRedCards[]"));
                    for (String id : masterPlayerIdListRedCards
                    ) {
                        Offense offense = new Offense();
                        offense.setGame(game);
                        offense.setType("RED");
                        offense.setPlayer(playerService.getPlayerById(Long.valueOf(id)));
                        offensesRed.add(offense);
                    }
                }
                ArrayList<String> slavePlayerIdListRedCards = new ArrayList<>();
                if (request.getParameterValues("slavePlayerIdListRedCards[]") != null) {
                    Collections.addAll(slavePlayerIdListRedCards, request
                            .getParameterValues("slavePlayerIdListRedCards[]"));
                    for (String id : slavePlayerIdListRedCards
                    ) {
                        Offense offense = new Offense();
                        offense.setGame(game);
                        offense.setType("RED");
                        offense.setPlayer(playerService.getPlayerById(Long.valueOf(id)));
                        offensesRed.add(offense);
                    }
                }
                game.getOffenses().addAll(offensesRed);
                try {
                    saveGameResult(game);
                    messageGenerator.setMessage((messageSource
                            .getMessage("success.resultGame", new Object[]{game.getMasterTeam().getTeamName() + " - " +
                                    game.getSlaveTeam().getTeamName()}, Locale.getDefault())));
                } catch (Exception e) {
                    throw new DerffException("database", game, new Object[]{e.getMessage()});
                }


                return "administration/calendar";
        }


        return "redirect:/administration/calendar";
    }




    //При дообавление игрока выводит сообщение удачно добавлен и заполянет поля для нового игроа старыми данными нужно после вывода сообщения стирать и объект
    //При редактировании игрока не меняя картоску айди выводится ошибка что такой номер не допустим.

    private Map<Long, String> getFullNamePlayersMap(List<Player> players) {
        Map<Long, String> result = new HashMap<>();
        for (Player player : players
        ) {
            result.put(player.getId(), player.getLastName() + " " + player.getFirstName() + " " + player
                    .getSecondName());
        }
        return result;
    }


    private void saveGameResult(Game game) throws DerffException {
        if (!game.isResultSave()) {
            for (Goal goal : game.getGoals()
            ) {
                goalService.save(goal);
            }
            for (Offense offense : game.getOffenses()
            ) {
                offenseService.save(offense);
            }
            game.setResultSave(true);
            gameService.save((Game) context.getFromContext("game"));
        } else {
            throw new DerffException("gameResultsAreAlreadyExists");
        }

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
    }

    private String dateToString(Date date) {
        Format formatter = new SimpleDateFormat("dd/MM/yyyy");
        return date == null ? null : formatter.format(date);
    }

}
