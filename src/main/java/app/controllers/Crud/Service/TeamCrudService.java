package app.controllers.Crud.Service;

import app.Models.Player;
import app.Models.Team;
import app.services.GameService;
import app.services.TeamService;
import app.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

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

    public HttpStatus saveTeamFlow(Team team, MultipartFile file, boolean replaceFile) {
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
                byte[] bytes = file.getBytes();
                team.setSymbolString("data:image/jpeg;base64, " + Base64Utils.encodeToString(bytes));
                team.setSymbol(bytes);
            } catch (IOException e) {
                return HttpStatus.PRECONDITION_FAILED;
            }
        } else if (replaceFile) {
            team.setSymbolString(null);
            team.setSymbol(null);
        }
        try {
            teamService.save(team);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.PRECONDITION_FAILED;
        }
    }

    public HttpStatus updateTeamFlow(Team team, MultipartFile file) {
        boolean replaceFile = true;
        Team teamFromDB = teamService.findTeamById(team.getId());
        if (teamFromDB == null) {
            return HttpStatus.NOT_FOUND;
        }
        teamFromDB.setTeamName(team.getTeamName());
        teamFromDB.setDate(team.getDate());
        teamFromDB.setBoss(team.getBoss());
        teamFromDB.setVillage(team.getVillage());
        teamFromDB.setPhone(team.getPhone());
        teamFromDB.setSeason(team.getSeason());
        if (file == null && team.getSymbolString() != null) {
            replaceFile = false;
        }
        return saveTeamFlow(teamFromDB, file, replaceFile);
    }

    public HttpStatus deleteTeamFromSeasonFlow(Long teamId) {
        Team team = teamService.findTeamById(teamId);
        team.setSeason(null);
        teamService.save(team);
        return HttpStatus.OK;
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
