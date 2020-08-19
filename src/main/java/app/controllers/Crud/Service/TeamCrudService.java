package app.controllers.Crud.Service;

import app.Models.Player;
import app.Models.Statistic;
import app.Models.Team;
import app.services.GameService;
import app.services.PlayerService;
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
    PlayerService playerService;

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
        playerService.findAllPlayersInTeam(team).forEach(player -> player.setTeam(null));
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


    public HttpStatus savePlayerFlow(Player player, MultipartFile file, boolean replaceFile) {
        //Date validate
    //    if (player.getStringBirthday() == null) {
    //        throw new DerffException("date", player);
   //     }

    //    try {
   //         player.setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse(player.getStringBirthday()));
   //     } catch (ParseException e) {
   //         throw new DerffException("date", player);
   //     }


        // Id card validation
        if(player.getIdCard().isEmpty() || player.getIdCard().equals("null")){
            player.setIdCard(null);
        }
        if (player.getIdCard() != null && !player.getIdCard().isEmpty() && playerService.getPlayerByIdCard(player.getIdCard()) != null && !playerService.getPlayerByIdCard(player.getIdCard()).equals(player)) {
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
                player.setPhotoString("data:image/jpeg;base64, " + Base64Utils.encodeToString(bytes));
                player.setPhoto(bytes);
            } catch (IOException e) {
                return HttpStatus.PRECONDITION_FAILED;
            }
        } else if (replaceFile) {
            player.setPhotoString(null);
            player.setPhoto(null);
        }
        try {
            playerService.save(player);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.PRECONDITION_FAILED;
        }
    }

    public HttpStatus updatePlayerFlow(Player player, MultipartFile file) {
        boolean replaceFile = true;
        Player playerFromDB = playerService.findPlayerById(player.getId());
        if (playerFromDB == null) {
            return HttpStatus.NOT_FOUND;
        }
        playerFromDB.setTeam(player.getTeam());
        playerFromDB.setSeason(player.getSeason());
        playerFromDB.setLastName(player.getLastName());
        playerFromDB.setFirstName(player.getFirstName());
        playerFromDB.setSecondName(player.getSecondName());
        playerFromDB.setBirthday(player.getBirthday());
        playerFromDB.setRegistration(player.getRegistration());
        playerFromDB.setIdCard(player.getIdCard());
        playerFromDB.setIsNotActive(player.getIsNotActive());
        playerFromDB.setInn(player.getInn());
        playerFromDB.setIsLegionary(player.getIsLegionary());
        playerFromDB.setRole(player.getRole());
        playerFromDB.setStringBirthday(null);

        if (file == null && (player.getPhotoString() != null && !player.getPhotoString().isEmpty())) {
            replaceFile = false;
        }
        return savePlayerFlow(playerFromDB, file, replaceFile);
    }

    public HttpStatus deletePlayerFromSeasonFlow(Long playerId) {
        Player player = playerService.findPlayerById(playerId);
        player.setSeason(null);
        player.setIsNotActive(true);
      //  player.setTeam(null);
        playerService.save(player);
        return HttpStatus.OK;
    }

    public void checkAvailableAutogoalInDB() {
        if (teamService.findTeamByName("AUTOGOAL") == null) {
            Team team = new Team();
            team.setTeamName("AUTOGOAL");
            teamService.save(team);
        }
        if (playerService.findPlayerByRegistration("AUTOGOAL") == null) {
            Player player = new Player();
            player.setFirstName("Автогол");
            player.setLastName("соперника");
            player.setSecondName("*");
            player.setRegistration("AUTOGOAL");
            player.setTeam(teamService.findTeamByName("AUTOGOAL"));
            playerService.save(player);
        }
    }

}
