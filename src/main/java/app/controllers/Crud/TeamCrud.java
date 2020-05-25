package app.controllers.Crud;

import app.Models.Team;
import app.controllers.Crud.Service.TeamCrudService;
import app.services.TeamService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

@RestController
@CrossOrigin
public class TeamCrud {

    @Autowired
    TeamService teamService;

    @Autowired
    TeamCrudService teamCrudService;

    @RequestMapping("/ui/teams")
    public ResponseEntity<Collection<Team>> getAllTeamNames() {
        List<Team> list = teamService.findAllTeams();
        list.forEach(team -> team.setPlayers(null));
        list.forEach(team -> team.setSymbol(null));
        //list.forEach(team -> team.setSymbolString(null));
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping("/ui/team")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Team saved successfully"),
            @ApiResponse(code = 412, message = "Precondition Failed"),
            // @ApiResponse(code = 501, message = "SLA's not found"),
            //  @ApiResponse(code = 403, message = "SLA's update not possible"),
            //   @ApiResponse(code = 406, message = "Incorrect SLA's Times definition"),
    })
    public ResponseEntity saveNewTeam(@ModelAttribute Team team, @RequestParam(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(teamCrudService.saveTeamFlow(team, file)).build();
    }

    @PutMapping("/ui/team")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Team updated successfully"),
            @ApiResponse(code = 412, message = "Precondition Failed"),
            @ApiResponse(code = 404, message = "Team not found"),
            //  @ApiResponse(code = 403, message = "SLA's update not possible"),
            //   @ApiResponse(code = 406, message = "Incorrect SLA's Times definition"),
    })
    public ResponseEntity updateTeam(@ModelAttribute Team team, @RequestParam(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(teamCrudService.updateTeamFlow(team, file)).build();
    }

    @DeleteMapping("/ui/team/{id}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Team saved successfully"),
            @ApiResponse(code = 412, message = "Team has game in DB"),
            @ApiResponse(code = 500, message = "DataBase error")

    })
    public ResponseEntity deleteTeam(@PathVariable Long id) {
        return ResponseEntity.status(teamCrudService.deleteTeamFlow(id)).build();
    }

    @GetMapping("/ui/team/{id}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Team find successfully"),
            @ApiResponse(code = 404, message = "Team not found"),
            @ApiResponse(code = 500, message = "DataBase error")

    })
    public ResponseEntity<Team> getTeamById(@PathVariable Long id) {
        Team team =teamService.findTeamById(id);
        team.setPlayers(null);
        return new ResponseEntity<>(teamService.findTeamById(id), HttpStatus.OK);
    }
}
