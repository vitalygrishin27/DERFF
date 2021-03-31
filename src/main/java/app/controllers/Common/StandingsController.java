package app.controllers.Common;

import app.Models.Game;
import app.Models.StandingsRow;
import app.Models.Team;
import app.services.SeasonService;
import app.services.impl.CompetitionServiceImpl;
import app.services.impl.GameServiceImpl;
import app.services.impl.TeamServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class StandingsController {
    @Autowired
    TeamServiceImpl teamService;

    @Autowired
    GameServiceImpl gameService;

    @Autowired
    CompetitionServiceImpl competitionService;

    @Autowired
    SeasonService seasonService;

    @GetMapping(value = "/standings")
    public String getStandings(Model model) {
        List<StandingsRow> standingsRows = new ArrayList<>();
        for (Team team : teamService.findBySeason(seasonService.findByYear(2021))
        ) {
            StandingsRow standingsRow = new StandingsRow();
            standingsRow.setTeamName(team.getTeamName());
            // TODO: 13.02.2020 refactor hardcode for 'findCompetitionById(1)'. This value should be set in Configuration
            for (Game game : gameService.findGamesWithResultByTeamAndCompetition(team, competitionService.findCompetitionById(1), true)
            ) {
                standingsRow.setGames(standingsRow.getGames() + 1);
                if (team.equals(game.getMasterTeam())) {
                    standingsRow.setScoredGoals(standingsRow.getScoredGoals() + game.getMasterGoalsCount());
                    standingsRow.setConcededGoals(standingsRow.getConcededGoals() + game.getSlaveGoalsCount());
                    if (game.getMasterGoalsCount().equals(game.getSlaveGoalsCount()) && !game.isTechnicalMasterTeamWin() && !game.isTechnicalSlaveTeamWin()) {
                        standingsRow.setDraws(standingsRow.getDraws() + 1);
                    } else if (game.getMasterGoalsCount() > game.getSlaveGoalsCount() || game.isTechnicalMasterTeamWin()) {
                        standingsRow.setWins(standingsRow.getWins() + 1);
                    } else {
                        standingsRow.setLosses(standingsRow.getLosses() + 1);
                    }
                } else {
                    standingsRow.setScoredGoals(standingsRow.getScoredGoals() + game.getSlaveGoalsCount());
                    standingsRow.setConcededGoals(standingsRow.getConcededGoals() + game.getMasterGoalsCount());
                    if (game.getSlaveGoalsCount().equals(game.getMasterGoalsCount())  && !game.isTechnicalMasterTeamWin() && !game.isTechnicalSlaveTeamWin()) {
                        standingsRow.setDraws(standingsRow.getDraws() + 1);
                    } else if (game.getSlaveGoalsCount() > game.getMasterGoalsCount() || game.isTechnicalSlaveTeamWin()) {
                        standingsRow.setWins(standingsRow.getWins() + 1);
                    } else {
                        standingsRow.setLosses(standingsRow.getLosses() + 1);
                    }
                }
            }
            standingsRow.setRatioGoals(standingsRow.getScoredGoals() - standingsRow.getConcededGoals());
            standingsRow.setPoints(standingsRow.getWins() * 3 + standingsRow.getDraws());
            standingsRows.add(standingsRow);
        }
        sortStandings(standingsRows);
        model.addAttribute("standings", standingsRows);
        return "common/standings/standings";
    }

    private void sortStandings(List<StandingsRow> standingsRows) {
        standingsRows.sort(StandingsRow.COMPARE_BY_POINTS);
        Collections.reverse(standingsRows);
        for (int i = 0; i < standingsRows.size(); i++) {
            standingsRows.get(i).setNumber(i + 1);
        }
    }
}

/*
8.2 у випадку рівності очок у двох команд Чемпіон визначається по результату додаткової гри (золотий матч) «Золотий матч» проводиться з однієї гри на нейтральному полі. В разі якщо додаткова гра закінчилася нічийним рахунком - призначаються 11- метрові удари, які визначають переможця. Якщо команд, які набрали однакову кількість очок більше ніж дві, то між ними рахуються такі показники: результат особистих ігор; різниця забитих і пропущених м'ячів в іграх між ними; найбільша кількість м'ячів забитих в іграх між ними. Команда, яка має найгірші вище перераховані показники не бере участь у «Золотому матчі». Якщо всі показники рівні, то відбувається жеребкування.
8.3 при визначенні наступних місць, у випадку рівності очок у двох або більше команд перевага віддається команді, яка має кращі результати в особистих зустрічах з конкурентом (конкурентами). За рівності цього показника набувають чинності такі показники: найбільша кількість перемог в усіх зустрічах; найкраща різниця забитих і пропущених м’ячів в усіх зустрічах; найбільша кількість м’ячів, забитих в усіх зустрічах.
 */