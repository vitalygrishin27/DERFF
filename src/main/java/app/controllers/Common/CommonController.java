package app.controllers.Common;

import app.Models.Context;
import app.Models.Player;
import app.Models.User;
import app.Utils.MessageGenerator;
import app.services.impl.GoalServiceImpl;
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
    Context context;

    @GetMapping(value = "/")
    public String getMainPage(Model model) {
        if (messageGenerator.isActive())
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
        model.addAttribute("bombardiers", getGoals(5));
        model.addAttribute("needShowAllBombardiers", context.getFromContext("needShowAllBombardiers")==null?Boolean.TRUE:context.getFromContext("needShowAllBombardiers"));
        return "administration/mainPage";
    }

    @GetMapping(value = "/login")
    public String geLoginPage(Model model) {
        if (messageGenerator.isActive())
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
        model.addAttribute("user", new User());
        return "login";
    }

    /*  @GetMapping(value = "statistic/goals")
      public String getGoals(Model model) {
          Map<Player,Integer> result=new HashMap<>();
          for (Goal goal : goalService.findAll()
          ) {
              result.put(goal.getPlayer(),result.containsKey(goal.getPlayer())?result.get(goal.getPlayer())+1:1);
          }
          model.addAttribute("bombardiers", result);
          return "common/statistic/goals";
      }*/

    @GetMapping(value = "/bombardiers")
    public String getAllBombardiers(Model model, HttpServletRequest request) {
        if (request.getParameter("show").equals("all")) {
            model.addAttribute("bombardiers", getGoals());
        } else {
            model.addAttribute("bombardiers", getGoals(5));
        }
        model.addAttribute("needShowAllBombardiers", context.getFromContext("needShowAllBombardiers")==null?Boolean.TRUE:context.getFromContext("needShowAllBombardiers"));
        return "common/statistic/bombardiers";
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

    public Map<Player, Integer> getGoals() {
        return getGoals(-1);
    }

}
