package app.controllers.Common;

import app.Models.User;
import app.Utils.MessageGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CommonController {

    @Autowired
    MessageGenerator messageGenerator;

    @GetMapping(value = "/")
    public String getMainPage(Model model){
        if (messageGenerator.isActive())
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
        return "administration/mainPage";
    }

    @GetMapping(value = "/login")
    public String geLoginPage(Model model){
        if (messageGenerator.isActive())
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
        model.addAttribute("user",new User());
        return "login";
    }
}
