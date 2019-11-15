package app.controllers;

import app.Models.Competition;
import app.Utils.MessageGenerator;
import app.exceptions.DerffException;
import app.services.impl.CompetitionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Locale;

@Controller
public class registrationCompetitionController {

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    CompetitionServiceImpl competitionService;


    @GetMapping(value = "/newCompetition")
    public String getDefaultForm(Model model) {
        model.addAttribute("titlePage", messageSource.getMessage("page.title.competition.creating", null, Locale.getDefault()));
        Competition competition = new Competition();
        if (messageGenerator.isActive()) {
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator.getTemporaryObjectForMessage().getClass().isInstance(new Competition()))
                competition = (Competition) messageGenerator.getTemporaryObjectForMessageWithSetNull();
        }
        model.addAttribute("competition", competition);
        return "regForms/regForm4Competition";
    }

    @PostMapping(value = "/newCompetition")
    public String postdefault(@ModelAttribute("competition") Competition competition) throws DerffException {
        System.out.println(competition);
        validateCompetitionInformation(competition);
        try {
            competitionService.save(competition);
            messageGenerator.setMessage((messageSource.getMessage("success.newCompetition", new Object[]{competition.getName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", competition, new Object[]{e.getMessage()});
        }
        return "redirect:/newCompetition";
    }

    private void validateCompetitionInformation(Competition competition) throws DerffException {
        //Competition name validation
        if (competitionService.findCompetitionByName(competition.getName()) != null)
            throw new DerffException("notAvailableCompetitionName", competition);

    }

}
