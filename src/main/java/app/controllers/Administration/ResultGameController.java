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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    TeamServiceImpl teamService;

    @Autowired
    OffenseServiceImpl offenseService;

    @GetMapping(value = "/administration/resultGame/{id}/reenterResult")
    public String deleteResultsAndStartReenter(Model model, @PathVariable("id") long id) {
        Game game = gameService.findGameById(id);
        game.getOffenses().forEach(offense -> offenseService.delete(offense));
        game.getGoals().forEach(goal -> goalService.delete(goal));
        game.setOffenses(null);
        game.setGoals(null);
        game.setResultSave(false);
        game.setMasterGoalsCount(0);
        game.setSlaveGoalsCount(0);
        gameService.save(game);
        return "redirect:/administration/resultGame/" + id;
    }

    @GetMapping(value = "/administration/resultGame/{id}")
    public String firstStepResultsGoalsCount(Model model, @PathVariable("id") long id) {
        context.deleteFromContext("game");
        Game game = gameService.findGameById(id);
        if (game.isResultSave()) {
            model.addAttribute("game", game);
            model.addAttribute("masterPlayersGoals", game.getGoals().stream().filter(goal -> goal.getTeam().equals(game.getMasterTeam())).map(Goal::getPlayer).collect(Collectors.toList()));
            model.addAttribute("slavePlayersGoals", game.getGoals().stream().filter(goal -> goal.getTeam().equals(game.getSlaveTeam())).map(Goal::getPlayer).collect(Collectors.toList()));
            model.addAttribute("masterPlayersWithYellowCards", game.getOffenses().stream().filter(offense -> offense.getPlayer().getTeam().equals(game.getMasterTeam()) && offense.getType().equals("YELLOW")).map(Offense::getPlayer).collect(Collectors.toList()));
            model.addAttribute("slavePlayersWithYellowCards", game.getOffenses().stream().filter(offense -> offense.getPlayer().getTeam().equals(game.getSlaveTeam()) && offense.getType().equals("YELLOW")).map(Offense::getPlayer).collect(Collectors.toList()));
            model.addAttribute("masterPlayersWithRedCards", game.getOffenses().stream().filter(offense -> offense.getPlayer().getTeam().equals(game.getMasterTeam()) && offense.getType().equals("RED")).map(Offense::getPlayer).collect(Collectors.toList()));
            model.addAttribute("slavePlayersWithRedCards", game.getOffenses().stream().filter(offense -> offense.getPlayer().getTeam().equals(game.getSlaveTeam()) && offense.getType().equals("RED")).map(Offense::getPlayer).collect(Collectors.toList()));
            return "administration/resultGames/gameOverview";

        }
        context.putToContext("game", game);
        model.addAttribute("masterTeamName", game.getMasterTeam().getTeamName());
        model.addAttribute("slaveTeamName", game.getSlaveTeam().getTeamName());
        model.addAttribute("countGoalsMasterTeam", 0);
        model.addAttribute("countGoalsSlaveTeam", 0);
        return "administration/resultGames/resultGame";
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
                        .findAllPlayersInTeam(game.getMasterTeam()), true));
                model.addAttribute("slaveTeamPlayersMap", getFullNamePlayersMap(playerService
                        .findAllPlayersInTeam(game.getSlaveTeam()), true));
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
                        goal.setPlayer(playerService.findPlayerById(Long.valueOf(id)));
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
                        goal.setPlayer(playerService.findPlayerById(Long.valueOf(id)));
                        goals.add(goal);
                    }
                }
                game.setGoals(goals);
                return "administration/resultGames/resultGameYellowCardsCount";
            case "yellowCardsCount":
                if (countYellowCardsMasterTeam.equals("")) countYellowCardsMasterTeam = "0";
                if (countYellowCardsSlaveTeam.equals("")) countYellowCardsSlaveTeam = "0";
                model.addAttribute("countYellowCardsMasterTeam", countYellowCardsMasterTeam);
                model.addAttribute("countYellowCardsSlaveTeam", countYellowCardsSlaveTeam);
                model.addAttribute("masterTeamPlayersMap", getFullNamePlayersMap(playerService
                        .findAllPlayersInTeam(game.getMasterTeam()), false));
                model.addAttribute("slaveTeamPlayersMap", getFullNamePlayersMap(playerService
                        .findAllPlayersInTeam(game.getSlaveTeam()), false));
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
                        offense.setPlayer(playerService.findPlayerById(Long.valueOf(id)));
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
                        offense.setPlayer(playerService.findPlayerById(Long.parseLong(id)));
                        offenses.add(offense);
                    }
                }
                game.setOffenses(offenses);
                return "administration/resultGames/resultGameRedCardsCount";
            case "redCardsCount":
                if (countRedCardsMasterTeam.equals("")) countRedCardsMasterTeam = "0";
                if (countRedCardsSlaveTeam.equals("")) countRedCardsSlaveTeam = "0";
                model.addAttribute("countRedCardsMasterTeam", countRedCardsMasterTeam);
                model.addAttribute("countRedCardsSlaveTeam", countRedCardsSlaveTeam);
                model.addAttribute("masterTeamPlayersMap", getFullNamePlayersMap(playerService
                        .findAllPlayersInTeam(game.getMasterTeam()), false));
                model.addAttribute("slaveTeamPlayersMap", getFullNamePlayersMap(playerService
                        .findAllPlayersInTeam(game.getSlaveTeam()), false));
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
                        offense.setPlayer(playerService.findPlayerById(Long.valueOf(id)));
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
                        offense.setPlayer(playerService.findPlayerById(Long.valueOf(id)));
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

                return "administration/resultGames/resultGame";
            // return "administration/game/calendar";
        }


        return "redirect:/administration/calendar";
    }

    private Map<Long, String> getFullNamePlayersMap(List<Player> players, Boolean autogoal) {
        if (autogoal) {
            checkAvailableAutogoalInDB();
            players.add(playerService.findPlayerByRegistration("AUTOGOAL"));
        }
        Map<Long, String> result = new HashMap<>();
        //      Collections.sort(players);
        for (Player player : players
        ) {
            result.put(player.getId(), player.getLastName() + " " + player.getFirstName() + " " + player
                    .getSecondName());
        }
 /*       Map<Long, String> result1 = new LinkedHashMap<>();
        Stream<Map.Entry<Long,String>> st = result.entrySet().stream();

        st.sorted(Comparator.comparing(e -> e.getValue()))
                .forEach(e ->result1.put(e.getKey(),e.getValue()));
*/
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

    private void checkAvailableAutogoalInDB() {
        if (teamService.findTeamByName("AUTOGOAL") == null) {
            Team team = new Team();
            team.setTeamName("AUTOGOAL");
            teamService.save(team);
        }
        if (playerService.findPlayerByRegistration("AUTOGOAL") == null) {
            Player player = new Player();
            player.setFirstName(messageSource.getMessage("label.autogoal.default.firstWord", null, Locale.getDefault()));
            player.setLastName(messageSource.getMessage("label.autogoal.default.lastWord", null, Locale.getDefault()));
            player.setSecondName(messageSource.getMessage("label.autogoal.default.secondWord", null, Locale.getDefault()));
            player.setRegistration("AUTOGOAL");
            player.setTeam(teamService.findTeamByName("AUTOGOAL"));
            playerService.save(player);
        }
    }

}
