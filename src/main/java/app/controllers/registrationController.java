package app.controllers;

import app.Models.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Locale;

@Controller
public class registrationController {

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @GetMapping(value = "/newTeam")
    public String getdefaultForm(Model model){
        model.addAttribute("titlePage", messageSource.getMessage("page.title.team.creating", null, Locale.getDefault()));
    /*    model.addAttribute("placeholderName", messageSource.getMessage("placeholder.name", null, Locale.getDefault()));
        model.addAttribute("placeholderDate", messageSource.getMessage("placeholder.date", null, Locale.getDefault()));
        model.addAttribute("placeholderRegion", messageSource.getMessage("placeholder.region", null, Locale.getDefault()));
        model.addAttribute("placeholderBoss", messageSource.getMessage("placeholder.boss", null, Locale.getDefault()));
        model.addAttribute("placeholderPhone", messageSource.getMessage("placeholder.phone", null, Locale.getDefault()));
        model.addAttribute("buttonSubmit", messageSource.getMessage("button.submit", null, Locale.getDefault()));*/
        //  userService.save(new User(34,"oiuoiu"));
        return "/regForms/regForm4Team";
    }
    @PostMapping(value = "/newTeam")
    public String postdefault(@ModelAttribute("team") Team team){
        System.out.println(team);
        //  userService.save(new User(34,"oiuoiu"));
        return "/regForms/regForm4Team";
    }

}
