package app.controllers.Administration;

import app.Models.*;
import app.Utils.MessageGenerator;
import app.exceptions.DerffException;
import app.services.impl.GameServiceImpl;
import app.services.impl.GoalServiceImpl;
import app.services.impl.OffenseServiceImpl;
import app.services.impl.PlayerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class ResultGameController {
    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    Context context;

    @Autowired
    GameServiceImpl gameService;

    @Autowired
    PlayerServiceImpl playerService;

    @Autowired
    GoalServiceImpl goalService;

    @Autowired
    OffenseServiceImpl offenseService;

    @GetMapping(value = "/administration/resultGame/{id}")
    public String firstStepResultsGoalsCount(Model model, @PathVariable("id") long id) throws DerffException {
        context.clear();
        Game game = gameService.findGameById(id);
        if (game.isResultSave()) {
            model.addAttribute("game",game);
            model.addAttribute("masterPlayersWithYellowCards",game.getOffenses().forEach();)
            model.addAttribute("message", messageSource.getMessage("warning.gameResultsAlreadyExists", new Object[]{game.getMasterTeam().getTeamName() + "-" +
                    game.getSlaveTeam().getTeamName()}, Locale.getDefault()));
            return "administration/resultGames/gameOverview";

        }
            context.putToContext("game", game);
            model.addAttribute("masterTeamName", game.getMasterTeam().getTeamName());
            model.addAttribute("slaveTeamName", game.getSlaveTeam().getTeamName());
            model.addAttribute("countGoalsMasterTeam", 0);
            model.addAttribute("countGoalsSlaveTeam", 0);
            return "administration/resultGames/resultGame";
       /* } else {
            throw new DerffException("gameResultsAreAlreadyExists", null, null, "/administration/calendar");
        }*/
    }

    @PostMapping(value = "/administration/resultGame")
    public String saveResultsOfGame(HttpServletRequest request, Model model,
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
            return "administration/resultGames/resultGameYellowCardsCount";
        }
        if (step.equals("yellowCardsCount") &&
                (countYellowCardsMasterTeam.equals("") || countYellowCardsMasterTeam.equals("0")) &&
                (countYellowCardsSlaveTeam.equals("") || countYellowCardsSlaveTeam.equals("0"))) {
            game.setOffenses(new ArrayList<Offense>());
            return "administration/resultGames/resultGameRedCardsCount";
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
                return "administration/resultGames/resultGameGoalsPlayers";
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
                return "administration/resultGames/resultGameYellowCardsCount";
            case "yellowCardsCount":
                if (countYellowCardsMasterTeam.equals("")) countYellowCardsMasterTeam = "0";
                if (countYellowCardsSlaveTeam.equals("")) countYellowCardsSlaveTeam = "0";
                model.addAttribute("countYellowCardsMasterTeam", countYellowCardsMasterTeam);
                model.addAttribute("countYellowCardsSlaveTeam", countYellowCardsSlaveTeam);
                model.addAttribute("masterTeamPlayersMap", getFullNamePlayersMap(playerService
                        .findAllPlayersInTeam(game.getMasterTeam())));
                model.addAttribute("slaveTeamPlayersMap", getFullNamePlayersMap(playerService
                        .findAllPlayersInTeam(game.getSlaveTeam())));
                return "administration/resultGames/resultGameYellowCardsPlayer";
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
                return "administration/resultGames/resultGameRedCardsCount";
            case "redCardsCount":
                if (countRedCardsMasterTeam.equals("")) countRedCardsMasterTeam = "0";
                if (countRedCardsSlaveTeam.equals("")) countRedCardsSlaveTeam = "0";
                model.addAttribute("countRedCardsMasterTeam", countRedCardsMasterTeam);
                model.addAttribute("countRedCardsSlaveTeam", countRedCardsSlaveTeam);
                model.addAttribute("masterTeamPlayersMap", getFullNamePlayersMap(playerService
                        .findAllPlayersInTeam(game.getMasterTeam())));
                model.addAttribute("slaveTeamPlayersMap", getFullNamePlayersMap(playerService
                        .findAllPlayersInTeam(game.getSlaveTeam())));
                return "administration/resultGames/resultGameRedCardsPlayer";
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

                return "redirect:/administration/calendar";
              // return "administration/game/calendar";
        }


        return "redirect:/administration/calendar";
    }

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

}
