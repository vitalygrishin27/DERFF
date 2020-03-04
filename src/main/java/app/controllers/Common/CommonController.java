package app.controllers.Common;

import app.Models.*;
import app.Utils.MessageGenerator;
import app.services.impl.GameServiceImpl;
import app.services.impl.GoalServiceImpl;
import app.services.impl.OffenseServiceImpl;
import app.services.impl.TeamServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class CommonController {

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    GoalServiceImpl goalService;

    @Autowired
    TeamServiceImpl teamService;

    @Autowired
    OffenseServiceImpl offenseService;

    @Autowired
    GameServiceImpl gameService;

    @Autowired
    Context context;

    final String DETAILS_4_YELLOW_CARDS = "details.4YellowCards";
    final String DETAILS_RED_CARD = "details.RedCard";

    @GetMapping(value = "/")
    public String getMainPage(Model model) {
        if (messageGenerator.isActive())
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
        model.addAttribute("bombardiers", getGoals(5));
        model.addAttribute("yellowCards", getYellowCards(5));
        model.addAttribute("skipGames", getSkipGameListByYellowCards(1));
        model.addAttribute("needShowAllBombardiers", context.getFromContext("needShowAllBombardiers") == null ? Boolean.TRUE : context.getFromContext("needShowAllBombardiers"));
        model.addAttribute("needShowAllYellowCards", context.getFromContext("needShowAllYellowCards") == null ? Boolean.TRUE : context.getFromContext("needShowAllYellowCards"));
        model.addAttribute("needShowAllSkipGames", context.getFromContext("needShowAllSkipGames") == null ? Boolean.TRUE : context.getFromContext("needShowAllSkipGames"));
        return "administration/mainPage";
    }

    @GetMapping(value = "/clearCache")
    public String getMainPageWithClearCache(Model model) {
       context.clear();
        return "redirect:/";
    }

    @GetMapping(value = "/login")
    public String geLoginPage(Model model) {
        if (messageGenerator.isActive())
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
        model.addAttribute("user", new User());
        return "login";
    }

    @GetMapping(value = "/bombardiers")
    public String getBombardiers(Model model, HttpServletRequest request) {
        if (request.getParameter("show").equals("all")) {
            model.addAttribute("bombardiers", getGoals());
        } else {
            model.addAttribute("bombardiers", getGoals(5));
        }
        model.addAttribute("needShowAllBombardiers", context.getFromContext("needShowAllBombardiers") == null ? Boolean.TRUE : context.getFromContext("needShowAllBombardiers"));
        return "common/statistic/bombardiers";
    }

    @GetMapping(value = "/yellowCards")
    public String getYellowCardsStatistic(Model model, HttpServletRequest request) {
        if (request.getParameter("show").equals("all")) {
            model.addAttribute("yellowCards", getYellowCards());
        } else {
            model.addAttribute("yellowCards", getYellowCards(5));
        }
        model.addAttribute("needShowAllYellowCards", context.getFromContext("needShowAllYellowCards") == null ? Boolean.TRUE : context.getFromContext("needShowAllYellowCards"));
        return "common/statistic/yellowCards";
    }

    @GetMapping(value = "/skipGames")
    public String getSkipGamesStatistic(Model model, HttpServletRequest request) {
        if (request.getParameter("show").equals("all")) {
            model.addAttribute("skipGames", getSkipGameListByYellowCards());
        } else {
            model.addAttribute("skipGames", getSkipGameListByYellowCards(1));
        }
        model.addAttribute("needShowAllSkipGames", context.getFromContext("needShowAllSkipGames") == null ? Boolean.TRUE : context.getFromContext("needShowAllSkipGames"));
        return "common/statistic/skipGames";
    }

    private Map<Player, Integer> getGoals(int count) {
        if (count == -1 && context.getFromContext("bombardiersAll") != null) {
            context.putToContext("needShowAllBombardiers", Boolean.FALSE);
            return (Map<Player, Integer>) context.getFromContext("bombardiersAll");
        }
        if (count > 0 && context.getFromContext("bombardiersFirsts") != null) {
            context.putToContext("needShowAllBombardiers", Boolean.TRUE);
            return (Map<Player, Integer>) context.getFromContext("bombardiersFirsts");
        }

        Map<Player, Integer> result = new HashMap<>();
        Map<Player, Integer> resultSorted = new LinkedHashMap<>();
        goalService.findAll().forEach(goal -> result.put(goal.getPlayer(), result.containsKey(goal.getPlayer()) ? result.get(goal.getPlayer()) + 1 : 1));
        // TODO: 02.03.2020 create method or constant of AUTOGOAL instead below code
        result.remove(teamService.findTeamByName("AUTOGOAL").getPlayers().toArray()[0]);
        result.entrySet().stream().sorted(Map.Entry.<Player, Integer>comparingByValue().reversed()).forEach(e -> resultSorted.put(e.getKey(), e.getValue()));
        Map<Player, Integer> resultSortedFirsts = resultSorted.entrySet().stream().limit(count).collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
        context.putToContext("bombardiersAll", resultSorted);
        context.putToContext("bombardiersFirsts", resultSortedFirsts);

        if (count > resultSorted.size()) count = -1;
        if (count == -1) {
            context.putToContext("needShowAllBombardiers", Boolean.FALSE);
            return resultSorted;
        }
        context.putToContext("needShowAllBombardiers", Boolean.TRUE);
        return resultSortedFirsts;
    }

    private Map<Player, Integer> getYellowCards(int count) {
        if (count == -1 && context.getFromContext("yellowCardsAll") != null) {
            context.putToContext("needShowAllYellowCards", Boolean.FALSE);
            return (Map<Player, Integer>) context.getFromContext("yellowCardsAll");
        }
        if (count > 0 && context.getFromContext("yellowCardsAll") != null) {
            context.putToContext("needShowAllYellowCards", Boolean.TRUE);
            return (Map<Player, Integer>) context.getFromContext("yellowCardsFirsts");
        }

        Map<Player, Integer> result = new HashMap<>();
        Map<Player, Integer> resultSorted = new LinkedHashMap<>();

        offenseService.getAllYellowCards().forEach(offense -> result.put(offense.getPlayer(), result.containsKey(offense.getPlayer()) ? result.get(offense.getPlayer()) + 1 : 1));
        result.entrySet().stream().sorted(Map.Entry.<Player, Integer>comparingByValue().reversed()).forEach(e -> resultSorted.put(e.getKey(), e.getValue()));
        Map<Player, Integer> resultSortedFirsts = resultSorted.entrySet().stream().limit(count).collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);

        context.putToContext("yellowCardsAll", resultSorted);
        context.putToContext("yellowCardsFirsts", resultSortedFirsts);

        if (count > resultSorted.size()) count = -1;
        if (count == -1) {
            context.putToContext("needShowAllYellowCards", Boolean.FALSE);
            return resultSorted;
        }
        context.putToContext("needShowAllYellowCards", Boolean.TRUE);
        return resultSortedFirsts;
    }

    private Map<Player, Integer> getGoals() {
        return getGoals(-1);
    }

    private Map<Player, Integer> getYellowCards() {
        return getYellowCards(-1);
    }

    private List<SkipGameEntry> getSkipGameListByYellowCards() {
        return getSkipGameListByYellowCards(-1);
    }


    private List<SkipGameEntry> getSkipGameListByYellowCards(int countTours) {
       if (countTours == -1 && context.getFromContext("skipGamesAll") != null) {
            context.putToContext("needShowAllSkipGames", Boolean.FALSE);
            return (List<SkipGameEntry>) context.getFromContext("skipGamesAll");
        }
        if (countTours > 0 && context.getFromContext("skipGamesLastTour") != null) {
            context.putToContext("needShowAllSkipGames", Boolean.TRUE);
            return (List<SkipGameEntry>) context.getFromContext("skipGamesLastTour");
        }


        List<SkipGameEntry> resultAll = new LinkedList<>();
        List<Game> allGames = gameService.findAllGames();
        for (int i = 0; i < allGames.size(); i++) {
            Game currentGame = allGames.get(i);
            if (currentGame.isResultSave()) {
                Collection<Offense> offenses = currentGame.getOffenses();
                for (Offense offense : offenses
                ) {
                    Player currentPlayer = offense.getPlayer();
                    if (offense.getType().equals("RED")) {
                        resultAll.addAll(createSkipEntry(allGames, currentPlayer, i, 1, DETAILS_RED_CARD));
                    } else {
                        int countYellowCardsBefore = getCountYellowCardsBeforeCurrentGame(allGames, currentPlayer, i);
                        // TODO: 04.03.2020 3,7,11,15 should be be continuously. Try (count+1)%4==0
                        if (countYellowCardsBefore == 3 || countYellowCardsBefore == 7 || countYellowCardsBefore == 11 || countYellowCardsBefore == 15) {
                            resultAll.addAll(createSkipEntry(allGames, currentPlayer, i, (countYellowCardsBefore + 1) / 2, DETAILS_4_YELLOW_CARDS));
                        }
                    }
                }
            }
        }

        Collections.reverse(resultAll);
        context.putToContext("skipGamesAll",resultAll );
        List<SkipGameEntry> resultLastTour = getOnlyForLastTour(resultAll);
        context.putToContext("skipGamesLastTour", resultLastTour);

        if (countTours > resultAll.size()) countTours = -1;
        if (countTours == -1) {
            context.putToContext("needShowAllSkipGames", Boolean.FALSE);
            return resultAll;
        }
        context.putToContext("needShowAllSkipGames", Boolean.TRUE);
        return resultLastTour;
    }

    private int getCountYellowCardsBeforeCurrentGame(List<Game> allGames, Player player, int gameIndex) {
        int result = 0;
        for (int i = 0; i < gameIndex; i++) {
            Game game = allGames.get(i);
            if (game.getMasterTeam().equals(player.getTeam()) || game.getSlaveTeam().equals(player.getTeam())) {
                Collection<Offense> offenses = game.getOffenses();
                for (Offense offense : offenses
                ) {
                    if (offense.getPlayer().equals(player)) {
                        result++;
                    }
                }
            }
        }
        return result;
    }

    private List<SkipGameEntry> createSkipEntry(List<Game> allGames, Player player, int indexGame, int countGameToSkip, String details) {
        List<SkipGameEntry> skipGameEntryList = new LinkedList<>();
        int countAlreadyAddedToSkip = 0;
        for (int i = indexGame + 1; i < allGames.size(); i++) {
            Game game = allGames.get(i);
            if (game.getMasterTeam().equals(player.getTeam()) || game.getSlaveTeam().equals(player.getTeam())) {
                skipGameEntryList.add(new SkipGameEntry(player, game, messageSource.getMessage(details, new Object[]{game.getMasterTeam().getTeamName(),game.getSlaveTeam().getTeamName(),game.getStringDate(),allGames.get(indexGame).getMasterTeam().getTeamName(),allGames.get(indexGame).getSlaveTeam().getTeamName(),allGames.get(indexGame).getStringDate()}, Locale.getDefault())));
                countAlreadyAddedToSkip++;
            }
            if (countAlreadyAddedToSkip == countGameToSkip) {
                return skipGameEntryList;
            }
        }
        return skipGameEntryList;
    }

    private List<SkipGameEntry> getOnlyForLastTour(List<SkipGameEntry> allEntry) {
        List<SkipGameEntry> result = new LinkedList<>();
        try {
            result.add(allEntry.get(0));
            for (int i = 1; i < allEntry.size(); i++) {
                if (allEntry.get(i).getGame().getDate().equals(result.get(0).getGame().getDate())) {
                    result.add(allEntry.get(i));
                } else {
                    return result;
                }
            }
            return result;
        } catch (Exception e) {
            return result;
        }
    }


}
