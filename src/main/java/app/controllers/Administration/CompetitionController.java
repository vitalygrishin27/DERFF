package app.controllers.Administration;

import app.Models.Competition;
import app.Models.Game;
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
public class CompetitionController {
    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    CompetitionServiceImpl competitionService;

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @GetMapping(value = "/administration/competitions")
    public String getCompetitions(Model model) {
        if (messageGenerator.isActive()) {
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
            model.addAttribute("competitions", competitionService.findAllCompetition());
        }
        return "administration/competition/competitions";
    }

    @GetMapping(value = "/administration/newCompetition")
    public String getFormforNewCompetition(Model model) throws DerffException {
        Competition competition = new Competition();
        if (messageGenerator.isActive()) {
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator
                    .getTemporaryObjectForMessage().getClass().isInstance(new Competition())) {
                competition = (Competition) messageGenerator.getTemporaryObjectForMessage();
            }
        }
        model.addAttribute("competition", competition);
        return "administration/competition/newCompetition";
    }

    @PostMapping(value = "/administration/newCompetition")
    public String saveNewCompetition(@ModelAttribute("competition") Competition competition) throws DerffException {

        validateCompetitionInformation(competition);
        try {
            competitionService.save(competition);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.newCompetition", new Object[]{competition.getName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", competition, new Object[]{e.getMessage()});
        }

        return "redirect:/administration/competitions";
    }

    private void validateCompetitionInformation(Competition competition) throws DerffException {
        //validate Competition name
        if (competitionService.findCompetitionByName(competition.getName()) != null) {
            throw new DerffException("notAvailableCompetitionName", competition);
        }

    }
}
