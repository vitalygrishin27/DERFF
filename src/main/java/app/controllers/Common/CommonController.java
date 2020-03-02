package app.controllers.Common;

import app.Models.Context;
import app.Models.Offense;
import app.Models.Player;
import app.Models.User;
import app.Utils.MessageGenerator;
import app.services.impl.GoalServiceImpl;
import app.services.impl.OffenseServiceImpl;
import app.services.impl.TeamServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class CommonController {

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    GoalServiceImpl goalService;

    @Autowired
    TeamServiceImpl teamService;

    @Autowired
    OffenseServiceImpl offenseService;

    @Autowired
    Context context;

    @GetMapping(value = "/")
    public String getMainPage(Model model) {
        if (messageGenerator.isActive())
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
        model.addAttribute("bombardiers", getGoals(5));
        model.addAttribute("yellowCards", getYellowCards(5));
        model.addAttribute("needShowAllBombardiers", context.getFromContext("needShowAllBombardiers")==null?Boolean.TRUE:context.getFromContext("needShowAllBombardiers"));
        model.addAttribute("needShowAllYellowCards", context.getFromContext("needShowAllYellowCards")==null?Boolean.TRUE:context.getFromContext("needShowAllYellowCards"));
        return "administration/mainPage";
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
        model.addAttribute("needShowAllBombardiers", context.getFromContext("needShowAllBombardiers")==null?Boolean.TRUE:context.getFromContext("needShowAllBombardiers"));
        return "common/statistic/bombardiers";
    }

    @GetMapping(value = "/yellowCards")
    public String getYellowCardsStatistic(Model model, HttpServletRequest request) {
        if (request.getParameter("show").equals("all")) {
            model.addAttribute("yellowCards", getYellowCards());
        } else {
            model.addAttribute("yellowCards", getYellowCards(5));
        }
        model.addAttribute("needShowAllYellowCards", context.getFromContext("needShowAllYellowCards")==null?Boolean.TRUE:context.getFromContext("needShowAllYellowCards"));
        return "common/statistic/yellowCards";
    }

    public Map<Player, Integer> getGoals(int count) {
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

    public Map<Player, Integer> getYellowCards(int count) {
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

    public Map<Player, Integer> getGoals() {
        return getGoals(-1);
    }
    public Map<Player, Integer> getYellowCards() {
        return getYellowCards(-1);
    }


}
