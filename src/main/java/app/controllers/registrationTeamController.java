package app.controllers;

import app.Models.Competition;
import app.Models.Context;
import app.Models.Team;
import app.Utils.MessageGenerator;
import app.exceptions.DerffException;
import app.services.impl.TeamServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Controller
public class registrationTeamController {

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    TeamServiceImpl teamService;

    @Value("${maxUploadFileSizeTeamSymbol}")
    private Long maxUploadFileSizeTeamSymbol;

    @Value("${availableFileExtension}")
    private String availableFileExtension;

    @Autowired
    Context context;

    @GetMapping(value = "/newTeam")
    public String getDefaultForm(Model model) throws DerffException {
        if (context.getFromContext("competition") == null) {
            throw new DerffException("notSelectedCompetition", null, null, "/competitions");
        } else {
            model.addAttribute("competition", context.getFromContext("competition"));
        }
        model.addAttribute("titlePage", messageSource.getMessage("page.title.team.creating", null, Locale.getDefault()));
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
        return "regForms/regForm4Team";
    }

    @PostMapping(value = "/newTeam")
    public String postdefault(@ModelAttribute("team") Team team, @ModelAttribute("preDate") String preDate, @ModelAttribute("file") MultipartFile file) throws DerffException {
              validateTeamInformation(team, preDate, file);
        try {
            team.addCompetition((Competition)context.getFromContext("competition"));
            teamService.save(team);
            messageGenerator.setMessage((messageSource.getMessage("success.newTeam", new Object[]{team.getTeamName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", team, new Object[]{e.getMessage()});
        }
        return "redirect:/selectTeamForCompetition";
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

}
