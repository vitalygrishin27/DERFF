package app.controllers.Crud;

import app.Models.Player;
import app.Models.Season;
import app.Models.Team;
import app.controllers.Crud.Service.TeamCrudService;
import app.services.PlayerService;
import app.services.SeasonService;
import app.services.TeamService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
public class TeamCrud {

    @Autowired
    TeamService teamService;

    @Autowired
    PlayerService playerService;

    @Autowired
    TeamCrudService teamCrudService;

    @Autowired
    SeasonService seasonService;

    int CURRENT_SEASON_YEAR = 2020;

    @RequestMapping("/ui/teams")
    public ResponseEntity<Collection<Team>> getAllTeam() {
        List<Team> list = teamService.findAllTeams();
        list.forEach(team -> team.setPlayers(null));
        list.forEach(team -> team.setSymbol(null));
        //list.forEach(team -> team.setSymbolString(null));
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @RequestMapping("/ui/unRegisteredTeams")
    public ResponseEntity<Collection<Team>> getUnregisteredTeams() {
        List<Team> list = teamService.findAllTeams().stream().filter(team -> team.getSeason() == null || team.getSeason().getYear() != CURRENT_SEASON_YEAR).collect(Collectors.toList());
        list.forEach(team -> team.setPlayers(null));
        list.forEach(team -> team.setSymbol(null));
        //Collections.sort(list);
        //list.forEach(team -> team.setSymbolString(null));
        return new ResponseEntity<>(list, HttpStatus.OK);
    }


    @RequestMapping("/ui/unRegisteredPlayers")
    public ResponseEntity<Collection<Player>> getUnregisteredPlayers() {
        // TODO: 03.06.2020 create List
        List<Player> result = playerService.findAllInactivePlayers();
        result.forEach(player -> {player.setSeason(null); player.setTeam(null);});
        Collections.sort(result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping("/ui/currentSeason")
    public ResponseEntity<Integer> getCurrentSeasonYear() {
        return new ResponseEntity<>(CURRENT_SEASON_YEAR, HttpStatus.OK);
    }


    @RequestMapping("/ui/teamsInSeason/{year}")
    public ResponseEntity<Collection<Team>> getAllTeamBySeason(@PathVariable String year) {
        // TODO: 01.06.2020 Error processed when year is not integer
        Season season = seasonService.findByYear(Integer.parseInt(year));
        List<Team> list = teamService.findBySeason(season);
        list.forEach(team -> team.setPlayers(null));
        list.forEach(team -> team.setSymbol(null));
        list.forEach(team -> team.setSeason(null));
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
        // TODO: 01.06.2020 Set current season year from settings
        team.setSeason(seasonService.findByYear(CURRENT_SEASON_YEAR));
        return ResponseEntity.status(teamCrudService.saveTeamFlow(team, file, true)).build();
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
        // TODO: 01.06.2020 Set current season year from settings
        team.setSeason(seasonService.findByYear(CURRENT_SEASON_YEAR));
        return ResponseEntity.status(teamCrudService.updateTeamFlow(team, file)).build();
    }

    @DeleteMapping("/ui/team/{id}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Team deleted from season successfully")
    })
    public ResponseEntity deleteTeamFromSeason(@PathVariable Long id) {
        return ResponseEntity.status(teamCrudService.deleteTeamFromSeasonFlow(id)).build();
    }

    @GetMapping("/ui/team/{id}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Team find successfully"),
            @ApiResponse(code = 404, message = "Team not found"),
            @ApiResponse(code = 500, message = "DataBase error")

    })
    public ResponseEntity<Team> getTeamById(@PathVariable Long id) {
        Team team = teamService.findTeamById(id);
        team.setPlayers(null);
        team.setSeason(null);
        return new ResponseEntity<>(team, HttpStatus.OK);
    }

    @GetMapping("/ui/seasons/{year}/teams/{teamId}/players")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Players find successfully"),
            @ApiResponse(code = 404, message = "Players not found"),
            @ApiResponse(code = 500, message = "DataBase error")

    })
    public ResponseEntity<List<Player>> getPlayersBySeasonAndTeam(@PathVariable String year, @PathVariable String teamId) {
        Team team = teamService.findTeamById(Integer.parseInt(teamId));
        List<Player> result = team.getPlayers().stream().filter(player -> player.getSeason() != null && player.getSeason().getYear() == Integer.parseInt(year)).collect(Collectors.toList());
        result.forEach(player -> {player.setSeason(null);player.setTeam(null);});
        Collections.sort(result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/ui/players/{id}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Player find successfully"),
            @ApiResponse(code = 404, message = "Player not found"),
            @ApiResponse(code = 500, message = "DataBase error")

    })
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        Player player = playerService.findPlayerById(id);
        player.setTeam(null);
        player.setSeason(null);
        player.setGoals(null);
        player.setOffenses(null);
        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @PostMapping("/ui/player")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Player saved successfully"),
            @ApiResponse(code = 412, message = "Precondition Failed")
    })
    public ResponseEntity saveNewPlayer(@ModelAttribute Player player, @RequestParam(value = "file", required = false) MultipartFile file, @RequestParam(value="teamId", required = true) String teamId) {
        // TODO: 01.06.2020 Set current season year from settings
        Team team = teamService.findTeamById(Long.parseLong(teamId));
        player.setTeam(team);
        player.setIsNotActive(false);
        player.setSeason(seasonService.findByYear(CURRENT_SEASON_YEAR));
        return ResponseEntity.status(teamCrudService.savePlayerFlow(player, file, true)).build();
    }

    @PutMapping("/ui/player")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Player updated successfully"),
            @ApiResponse(code = 412, message = "Precondition Failed"),
            @ApiResponse(code = 404, message = "Player not found")
    })
    public ResponseEntity updatePlayer(@ModelAttribute Player player, @RequestParam(value = "file", required = false) MultipartFile file, @RequestParam(value="teamId")  String teamId) {
        // TODO: 01.06.2020 Set current season year from settings
        Team team = teamService.findTeamById(Long.parseLong(teamId));
        player.setTeam(team);
        player.setIsNotActive(false);
        player.setSeason(seasonService.findByYear(CURRENT_SEASON_YEAR));
        return ResponseEntity.status(teamCrudService.updatePlayerFlow(player, file)).build();
    }

    @DeleteMapping("/ui/players/{id}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Player deleted from season successfully")
    })
    public ResponseEntity deletePlayerFromSeason(@PathVariable Long id) {
        return ResponseEntity.status(teamCrudService.deletePlayerFromSeasonFlow(id)).build();
    }

}
