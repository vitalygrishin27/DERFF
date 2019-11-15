package app.controllers;

import app.Models.Competition;
import app.Models.Context;
import app.Models.Team;
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
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
    public String getDefaultForm(Model model) {
        List<Competition> competitions = competitionService.findAllCompetitions();
        Collections.reverse(competitions);
        if (messageGenerator.isActive())
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());

        model.addAttribute("competitions", competitions);
        return "competition/competitionsList";
    }

    @PostMapping(value = "/selectCompetition")
    public void selectCompetition(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long id = Long.valueOf(req.getParameter("competitionId"));
        Competition competition = competitionService.findCompetitionById(id);
        // TODO: 15.11.2019 Может нужно обнулять весь контекст
        context.putToContext("competition", competition);
        JSONObject jsonObjectResponse = new JSONObject();
        jsonObjectResponse.put("url", "/selectTeamForCompetition");
        resp.getWriter().write(String.valueOf(jsonObjectResponse));
        resp.flushBuffer();
    }

    @GetMapping(value = "/selectTeamForCompetition")
    public String getForm4SelectTeam(Model model) throws DerffException {
        if (context.getFromContext("competition") == null) {
            throw new DerffException("notSelectedCompetition", null, null, "/competitions");
        } else {
            model.addAttribute("competition", context.getFromContext("competition"));
        }
        List<Team> teams = teamService.findAllTeams();
        Collections.reverse(teams);
        if (messageGenerator.isActive())
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());

        model.addAttribute("teams", teams);
        return "competition/selectTeamForCompetition";
    }

    @PostMapping(value = "/selectTeamForCompetition")
    public void selectTeam(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long id = Long.valueOf(req.getParameter("teamId"));
        Team team = teamService.findTeamById(id);
        context.putToContext("team", team);
        JSONObject jsonObjectResponse = new JSONObject();
        jsonObjectResponse.put("url", "/players");
        resp.getWriter().write(String.valueOf(jsonObjectResponse));
        resp.flushBuffer();
    }


    @PostMapping(value = "/competitions")
    public void postDefault(HttpServletRequest req, HttpServletResponse resp) throws DerffException {
        long id = Long.valueOf(req.getParameter("competitionId"));
        Competition competition = competitionService.findCompetitionById(id);
        context.putToContext("competition", competition);
        /*    long id= Long.valueOf(req.getParameter("seasonId"));
        String command= req.getParameter("command");
        try{
            Competition competitionForDelete=competitionService.findSeasonById(id);
            switch (command){
                case "delete":
                    competitionService.deleteById(id);
                    messageGenerator.setMessage((messageSource.getMessage("success.deleteSeason", new Object[]{competitionForDelete.getName()}, Locale.getDefault())));
                    break;
            }
        }catch (Exception e){
            throw new DerffException("database", null,new Object[]{e.getMessage()});
        }


        System.out.println("req.get");*/
        //   return "redirect:/competitions";
    }

}
