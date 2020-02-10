package app.controllers.Administration;

import app.Utils.MessageGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GameController {
    @Autowired
    MessageGenerator messageGenerator;

    @GetMapping(value = "/administration/calendar")
    public String getCalendar(Model model) {
        if (messageGenerator.isActive()) {
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
        }
        return "administration/game/calendar";
    }

}
