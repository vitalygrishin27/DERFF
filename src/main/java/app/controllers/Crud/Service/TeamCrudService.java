package app.controllers.Crud.Service;

import app.Models.Team;
import app.exceptions.DerffException;
import app.services.GameService;
import app.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

@Service
public class TeamCrudService {

    @Value("${maxUploadFileSizeTeamSymbol}")
    private Long maxUploadFileSizeTeamSymbol;

    @Value("${maxUploadFileSizePlayerPhoto}")
    private Long maxUploadFileSizePlayerPhoto;

    @Value("${availableFileExtension}")
    private String availableFileExtension;

    @Autowired
    TeamService teamService;

    @Autowired
    GameService gameService;

    public HttpStatus saveTeamFlow(Team team, MultipartFile file) {
        //validate Team name
        if (teamService.findTeamByName(team.getTeamName()) != null && team.getId() == 0) {
            return HttpStatus.PRECONDITION_FAILED;
        }
        // File size validation
        if (file != null && file.getSize() > maxUploadFileSizeTeamSymbol) {
            return HttpStatus.PRECONDITION_FAILED;
        }
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
            if (!isCorrectFileExtention) {
                return HttpStatus.PRECONDITION_FAILED;
            }
            //Set byte[] to Team
            try {
                if (team.getSymbolString() == null) {
                    byte[] bytes = file.getBytes();
                    team.setSymbolString("data:image/jpeg;base64, " + Base64Utils.encodeToString(bytes));
                    team.setSymbol(bytes);
                } else if (team.getId() != 0) {
                    team.setSymbol(teamService.findTeamById(team.getId()).getSymbol());
                    team.setSymbolString(teamService.findTeamById(team.getId()).getSymbolString());
                }
            } catch (IOException e) {
                return HttpStatus.PRECONDITION_FAILED;
            }
        }
        try {
            teamService.save(team);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.PRECONDITION_FAILED;
        }
    }

    public HttpStatus deleteTeamFlow(Long teamId) {
        Team team = teamService.findTeamById(teamId);
        if (!gameService.findGameWithTeam(team).isEmpty()) {
            return HttpStatus.PRECONDITION_FAILED;
        }
        try {
            teamService.delete(team);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

}
