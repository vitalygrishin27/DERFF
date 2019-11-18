package app.controllers;

import app.Models.Competition;
import app.Models.Context;
import app.Models.Region;
import app.Models.Team;
import app.Utils.MessageGenerator;
import app.exceptions.DerffException;
import app.services.impl.TeamServiceImpl;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Controller
public class TeamController {
    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    Context context;

    @Autowired
    TeamServiceImpl teamService;

    @Value("${maxUploadFileSizeTeamSymbol}")
    private Long maxUploadFileSizeTeamSymbol;

    @Value("${availableFileExtension}")
    private String availableFileExtension;

    @GetMapping(value = "/teams")
    public String getTeamsList(Model model) throws DerffException {
        validateContext();
        Competition competition = (Competition) context.getFromContext("competition");
        List<Team> teams = teamService.findAllTeamsInCompetition(competition);
        Collections.reverse(teams);
        if (messageGenerator.isActive())
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());

        model.addAttribute("teams", teams);
        model.addAttribute("competition", context.getFromContext("competition"));
        model.addAttribute("region", context.getFromContext("region"));

        return "team/teams";
    }

    @PostMapping(value = "/selectTeam")
    public void selectTeam(HttpServletRequest req, HttpServletResponse resp) throws IOException, DerffException {
        validateContext();
        long id = Long.valueOf(req.getParameter("teamId"));
        Team team = teamService.findTeamById(id);
        context.putToContext("team", team);
        JSONObject jsonObjectResponse = new JSONObject();
        jsonObjectResponse.put("url", "/players");
        resp.getWriter().write(String.valueOf(jsonObjectResponse));
        resp.flushBuffer();
    }

    private void validateContext() throws DerffException {
        if (context.getFromContext("region") == null) {
            throw new DerffException("notSelectedRegion", null, null, "/regions");
        } else if (context.getFromContext("competition") == null) {
            throw new DerffException("notSelectedCompetition", null, null, "/competitions");
        }
    }

    @GetMapping(value = "/newTeam")
    public String getTeamForm(Model model) throws DerffException {
        validateContext();
        model.addAttribute("pageTitle", messageSource.getMessage("page.title.team.creating", new Object[]{((Competition)context.getFromContext("competition")).getName(), ((Region)context.getFromContext("region")).getName()}, Locale.getDefault()));
        Team team = new Team();
        if (messageGenerator.isActive()) {
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator.getTemporaryObjectForMessage().getClass().isInstance(new Team()))
                team = (Team) messageGenerator.getTemporaryObjectForMessageWithSetNull();
            model.addAttribute("preDate", dateToString(team.getDate()));
        } else {
            model.addAttribute("preDate", messageSource.getMessage("placeholder.DefaultDate", null, Locale.getDefault()));
        }
        model.addAttribute("team", team);
        model.addAttribute("region", context.getFromContext("region"));
        model.addAttribute("competition", context.getFromContext("competition"));
        return "regForms/regForm4Team";
    }

    @PostMapping(value = "/newTeam")
    public String postdefault(@ModelAttribute("team") Team team, @ModelAttribute("preDate") String preDate, @ModelAttribute("file") MultipartFile file) throws DerffException {
        validateContext();
        validateTeamInformation(team, preDate, file);
        try {
            team.setRegion((Region) context.getFromContext("region"));
            team.setCompetition((Competition) context.getFromContext("competition"));
            teamService.save(team);
            messageGenerator.setMessage((messageSource.getMessage("success.newTeam", new Object[]{team.getTeamName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", team, new Object[]{e.getMessage()});
        }
        return "redirect:/teams";
    }

    private void validateTeamInformation(Team team, String preDate, MultipartFile file) throws DerffException {
        //Region validation
        if (context.getFromContext("region") == null) {
            throw new DerffException("notSelectedRegion", null, null, "/regions");
        }

        // Competition validation
        if (context.getFromContext("competition") == null) {
            throw new DerffException("notSelectedCompetition", null, null, "/competitions");
        }

        //Date parse
        try {
            team.setDate(new SimpleDateFormat("dd/MM/yyyy").parse(preDate));
        } catch (Exception e) {
            throw new DerffException("date", team);
        }
        // File size validation
        if (file.getSize() > maxUploadFileSizeTeamSymbol)
            throw new DerffException("maxUploadFileSizeTeamSymbol", team, new Object[]{maxUploadFileSizeTeamSymbol, file.getSize()});

        // File extension validation
        if (file.getSize() > 0) {
            boolean isCorrectFileExtention = false;
            for (String regex : availableFileExtension.split(";")
            ) {
                if (file.getOriginalFilename().endsWith(regex)) {
                    isCorrectFileExtention = true;
                    break;
                }
            }
            if (!isCorrectFileExtention)
                throw new DerffException("notAvailableFileExtension", team, new Object[]{availableFileExtension});

            //Set byte[] to Team
            try {
                team.setSymbol(file.getBytes());
            } catch (IOException e) {
                throw new DerffException("fileGetBytes", team, new Object[]{e.getMessage()});
            }
        }


    }

    private String dateToString(Date date) {
        Format formatter = new SimpleDateFormat("dd/MM/yyyy");
        return date == null ? null : formatter.format(date);
    }


}
 /*  private void prepareFileToSaveIntoDB(Team team){
            try {
                byte[] bytes = team.getSymbol().getBytes();
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(new File(name + "-uploaded")));
                stream.write(bytes);
                // byte[] en= Base64.encode(bytes);
                String encodedString = Base64.getEncoder().encodeToString(bytes);
                byte[] bytes2 = Base64.getDecoder().decode(encodedString);
                photoService.save(new Photo(1,bytes,null));
                stream.close();
                return "Вы удачно загрузили " + name + " в " + name + "-uploaded !";
            } catch (Exception e) {
                return "Вам не удалось загрузить " + name + " => " + e.getMessage();
            }
        }*/