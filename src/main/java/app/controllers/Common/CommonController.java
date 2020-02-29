package app.controllers.Common;

import app.Models.Goal;
import app.Models.Player;
import app.Models.Team;
import app.Models.User;
import app.Utils.MessageGenerator;
import app.services.impl.GoalServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class CommonController {

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    GoalServiceImpl goalService;

    @GetMapping(value = "/")
    public String getMainPage(Model model) {
        if (messageGenerator.isActive())
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
        model.addAttribute("bombardiers", getGoals());
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
  public Map<Player,Integer> getGoals() {
      Map<Player, Integer> result = new HashMap<>();
      goalService.findAll().forEach(goal -> result.put(goal.getPlayer(), result.containsKey(goal.getPlayer()) ? result.get(goal.getPlayer()) + 1 : 1));
      return result;
  }

}
