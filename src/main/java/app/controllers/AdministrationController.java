package app.controllers;

import app.Models.Team;
import app.Utils.MessageGenerator;
import app.exceptions.DerffException;
import app.services.impl.TeamServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

@Controller
public class AdministrationController {

    @Value("${maxUploadFileSizeTeamSymbol}")
    private Long maxUploadFileSizeTeamSymbol;

    @Value("${availableFileExtension}")
    private String availableFileExtension;

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    TeamServiceImpl teamService;

    @Autowired
    MessageGenerator messageGenerator;

    @GetMapping(value = "/administration/teams")
    public String getTeams(Model model) throws DerffException {
        if (messageGenerator.isActive())
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());
        model.addAttribute("teams", teamService.findAllTeams());
        return "administration/teams";
    }

    @DeleteMapping(value = "/administration/teams")
    public void deleteTeam(@ModelAttribute("teamId") Long teamId) throws DerffException {
        try {
            Team team = teamService.findTeamById(teamId);
            teamService.delete(team);
            messageGenerator.setMessage((messageSource.getMessage("success.deleteTeam", new Object[]{team.getTeamName()}, Locale.getDefault())));
        }catch (Exception e){
            throw new DerffException("database", new Object[]{e.getMessage()});
        }
    }

    @GetMapping(value = "/newTeam")
    public String getTeamForm(Model model) throws DerffException {
        Team team = new Team();
        if (messageGenerator.isActive()) {
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator.getTemporaryObjectForMessage().getClass().isInstance(new Team()))
                team = (Team) messageGenerator.getTemporaryObjectForMessageWithSetNull();
        }
        model.addAttribute("team", team);
        return "administration/newTeam";
    }

    @PostMapping(value = "/newTeam")
    public String newTeam(@ModelAttribute("team") Team team, @ModelAttribute("file") MultipartFile file) throws DerffException {
        validateTeamInformation(team, file);
        try {
            teamService.save(team);
            messageGenerator.setMessage((messageSource.getMessage("success.newTeam", new Object[]{team.getTeamName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", team, new Object[]{e.getMessage()});
        }
        return "redirect:/administration/teams";
    }


    private void validateTeamInformation(Team team, MultipartFile file) throws DerffException {
        //validate Team name
        if (teamService.findTeamByName(team.getTeamName()) != null) {
            throw new DerffException("notAvailableTeamName", team);
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
                byte[] bytes = file.getBytes();

                team.setSymbolString("data:image/jpeg;base64, " + Base64Utils.encodeToString(bytes));
                team.setSymbol(bytes);
            } catch (IOException e) {
                throw new DerffException("fileGetBytes", team, new Object[]{e.getMessage()});
            }
        }


    }

}
