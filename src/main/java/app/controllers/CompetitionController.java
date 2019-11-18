package app.controllers;

import app.Models.Competition;
import app.Models.Context;
import app.Models.Region;
import app.Utils.MessageGenerator;
import app.exceptions.DerffException;
import app.services.impl.CompetitionServiceImpl;
import app.services.impl.TeamServiceImpl;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Controller
public class CompetitionController {

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    CompetitionServiceImpl competitionService;

    @Autowired
    TeamServiceImpl teamService;

    @Autowired
    Context context;


    @GetMapping(value = "/competitions")
    public String getCompetitions(Model model) throws DerffException {
        Region region = (Region) context.getFromContext("region");
        if (region == null) {
            throw new DerffException("notSelectedRegion", null, null, "/regions");
        }
        model.addAttribute("pageTitle", (messageSource.getMessage("page.title.competitions.list", new Object[]{region.getName()}, Locale.getDefault())));
        List<Competition> competitions = competitionService.findAllCompetitionsInRegion(region);
        Collections.reverse(competitions);
        if (messageGenerator.isActive())
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());

        model.addAttribute("competitions", competitions);
        return "competition/competitions";
    }

    @PostMapping(value = "/selectCompetition")
    public void selectCompetition(HttpServletRequest req, HttpServletResponse resp) throws IOException, DerffException {
        JSONObject jsonObjectResponse = new JSONObject();
        if (context.getFromContext("region") == null) {
            throw new DerffException("notSelectedRegion", null, null, "/regions");
        }
        long id = Long.valueOf(req.getParameter("competitionId"));
        Competition competition = competitionService.findCompetitionById(id);
        context.putToContext("competition", competition);
        jsonObjectResponse.put("url", "/teams");
        resp.getWriter().write(String.valueOf(jsonObjectResponse));
        resp.flushBuffer();
    }


    @GetMapping(value = "/newCompetition")
    public String getCompetitionForm(Model model) throws DerffException {
        if (context.getFromContext("region") == null) {
            throw new DerffException("notSelectedRegion", null, null, "/regions");
        }
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
    public String postNewCompetition(@ModelAttribute("competition") Competition competition) throws DerffException {
        if (context.getFromContext("region") == null) {
            throw new DerffException("notSelectedRegion", null, null, "/regions");
        }
        validateCompetitionInformation(competition);
        try {
            competition.setRegion((Region) context.getFromContext("region"));
            competitionService.save(competition);
            messageGenerator.setMessage((messageSource.getMessage("success.newCompetition", new Object[]{competition.getName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", competition, new Object[]{e.getMessage()});
        }
        return "redirect:/competitions";
    }

    private void validateCompetitionInformation(Competition competition) throws DerffException {
        //Competition name validation
        if (competitionService.findCompetitionByName(competition.getName()) != null)
            throw new DerffException("notAvailableCompetitionName", competition);

    }
}
