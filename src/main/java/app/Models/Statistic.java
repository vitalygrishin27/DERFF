package app.Models;

import app.services.impl.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional
@Service
public class Statistic implements Runnable {

    @Getter
    private Context context = new Context();

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    GoalServiceImpl goalService;

    @Autowired
    TeamServiceImpl teamService;

    @Autowired
    OffenseServiceImpl offenseService;

    @Autowired
    GameServiceImpl gameService;

    @Autowired
    ManualSkipGameServiceImpl manualSkipGameService;

    @Getter
    private boolean processIsAlreadyRun;

    final String DETAILS_4_YELLOW_CARDS = "details.4YellowCards";
    final String DETAILS_RED_CARD = "details.RedCard";
    final String DETAILS_MANUAL_RED_CARD = "details.ManualRedCard";

    @Transactional
    @Override
    public void run() {
        if (!processIsAlreadyRun && !isStatisticReady()) {
            processIsAlreadyRun = true;
            context.clear();
            getGoals(5);
            getYellowCards(5);
            getSkipGameListByYellowCards(1);
            processIsAlreadyRun = false;
        }
    }

    public boolean isStatisticReady() {
        return !context.getContext().isEmpty() &&
                context.getContext().get("bombardiersAll") != null &&
                context.getContext().get("yellowCardsAll") != null &&
                context.getContext().get("skipGamesAll") != null &&
                context.getContext().get("skipGamesLastTour") != null &&
                context.getContext().get("yellowCardsFirsts") != null &&
                context.getContext().get("bombardiersFirsts") != null;
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
        if (teamService.findTeamByName("AUTOGOAL") != null) {
            result.remove(teamService.findTeamByName("AUTOGOAL").getPlayers().toArray()[0]);
        }
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
            for (ManualSkipGame manualSkipGame : manualSkipGameService.findByGame(currentGame)
            ) {
                if (manualSkipGame.getPlayer().getTeam().equals(currentGame.getMasterTeam()) ||
                        manualSkipGame.getPlayer().getTeam().equals(currentGame.getSlaveTeam())) {
                    resultAll.addAll(createSkipEntry(allGames, manualSkipGame.getPlayer(), i - 1, 1, DETAILS_MANUAL_RED_CARD, manualSkipGame.getDescription()));
                }
            }
            if (currentGame.isResultSave()) {
                Collection<Offense> offenses = currentGame.getOffenses();
                for (Offense offense : offenses
                ) {
                    Player currentPlayer = offense.getPlayer();
                    if (offense.getType().equals("RED")) {
                        resultAll.addAll(createSkipEntry(allGames, currentPlayer, i, 1, DETAILS_RED_CARD, null));
                    } else {
                        int countYellowCardsBefore = getCountYellowCardsBeforeCurrentGame(allGames, currentPlayer, i);
                        // TODO: 04.03.2020 3,7,11,15 should be be continuously. Try (count+1)%4==0
                        if (countYellowCardsBefore == 3 || countYellowCardsBefore == 7 || countYellowCardsBefore == 11 || countYellowCardsBefore == 15) {
                            resultAll.addAll(createSkipEntry(allGames, currentPlayer, i, (countYellowCardsBefore + 1) / 4, DETAILS_4_YELLOW_CARDS, null));
                        }
                    }
                }
            }
        }

        Collections.reverse(resultAll);
        context.putToContext("skipGamesAll", resultAll);
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

    private List<SkipGameEntry> createSkipEntry(List<Game> allGames, Player player, int indexGame, int countGameToSkip, String details, String additionalInformation) {
        List<SkipGameEntry> skipGameEntryList = new LinkedList<>();
        int countAlreadyAddedToSkip = 0;
        for (int i = indexGame + 1; i < allGames.size(); i++) {
            Game game = allGames.get(i);
            if (game.getMasterTeam().equals(player.getTeam()) || game.getSlaveTeam().equals(player.getTeam())) {
                skipGameEntryList.add(new SkipGameEntry(player, game, game.getStringDate(), messageSource.getMessage(details, new Object[]{game.getMasterTeam().getTeamName(), game.getSlaveTeam().getTeamName(), game.getStringDate(), allGames.get(indexGame).getMasterTeam().getTeamName(), allGames.get(indexGame).getSlaveTeam().getTeamName(), allGames.get(indexGame).getStringDate(), additionalInformation}, Locale.getDefault())));
                countAlreadyAddedToSkip++;
            }
            if (countAlreadyAddedToSkip == countGameToSkip) {
                return skipGameEntryList;
            }
        }
        return skipGameEntryList;
    }

    private List<SkipGameEntry> getOnlyForLastTour(List<SkipGameEntry> allEntry) {
        //Determine last tour by game which not have results
        int startIndex = 0;
        for (int i = 0; i < allEntry.size(); i++) {
            if (allEntry.get(i).getGame().isResultSave()) {
                startIndex = i == 0 ? 0 : i - 1;
                break;
            }
        }
        List<SkipGameEntry> result = new LinkedList<>();
        try {
            // result.add(allEntry.get(startIndex));
            for (int i = startIndex; i < allEntry.size(); i++) {
                if (allEntry.get(i).getGame().getDate().equals(allEntry.get(startIndex).getGame().getDate())) {
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
