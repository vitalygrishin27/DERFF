package app.controllers.Crud;

import app.Models.Team;
import app.Utils.MessageGenerator;
import app.controllers.Administration.TeamController;
import app.exceptions.DerffException;
import app.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@RestController
@CrossOrigin
public class TeamCrud {
    @Value("${maxUploadFileSizeTeamSymbol}")
    private Long maxUploadFileSizeTeamSymbol;

    @Value("${maxUploadFileSizePlayerPhoto}")
    private Long maxUploadFileSizePlayerPhoto;

    @Value("${availableFileExtension}")
    private String availableFileExtension;

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    TeamService teamService;

    @RequestMapping("/ui/teams")
    public ResponseEntity<Collection<Team>> getAllTeamNames() {
        List<Team> list = teamService.findAllTeams();
        list.forEach(team -> team.setPlayers(null));
        list.forEach(team -> team.setSymbol(null));
        //list.forEach(team -> team.setSymbolString(null));
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping("/ui/team")
    public ResponseEntity<String> saveNewTeam(@ModelAttribute Team team, @RequestParam(value = "file", required = false) MultipartFile file) throws DerffException {
       validateTeamInformation(team, file, true);
        try {
            teamService.save(team);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.newTeam", new Object[]{team.getTeamName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", team, new Object[]{e.getMessage()});
        }
        return new ResponseEntity<>(String.valueOf(teamService.findTeamByName(team.getTeamName()).getId()), HttpStatus.OK);
    }


    private void validateTeamInformation(Team team, MultipartFile file, boolean needToReplaceFile) throws DerffException {
        //validate Team name
        if (teamService.findTeamByName(team.getTeamName()) != null && team.getId() == 0) {
            throw new DerffException("notAvailableTeamName", team);
        }

        // File size validation
        if (file != null && file.getSize() > maxUploadFileSizeTeamSymbol)
            throw new DerffException("maxUploadFileSizeTeamSymbol", team, new Object[]{maxUploadFileSizeTeamSymbol, file.getSize()});

        // File extension validation
        if (file != null && file.getSize() > 0) {
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
                if (team.getSymbolString() == null || needToReplaceFile) {
                    byte[] bytes = file.getBytes();
                    team.setSymbolString("data:image/jpeg;base64, " + Base64Utils.encodeToString(bytes));
                    team.setSymbol(bytes);
                } else if (team.getId() != 0) {
                    team.setSymbol(teamService.findTeamById(team.getId()).getSymbol());
                    team.setSymbolString(teamService.findTeamById(team.getId()).getSymbolString());
                }
            } catch (IOException e) {
                throw new DerffException("fileGetBytes", team, new Object[]{e.getMessage()});
            }
        } else {
            if (!needToReplaceFile && team.getId() != 0) {
                team.setSymbol(teamService.findTeamById(team.getId()).getSymbol());
                team.setSymbolString(teamService.findTeamById(team.getId()).getSymbolString());
            }
        }
    }

}
