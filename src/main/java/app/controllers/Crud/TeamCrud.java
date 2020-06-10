package app.controllers.Crud;

import app.Models.*;
import app.Utils.EncryptedPasswordUtil;
import app.controllers.Crud.Service.TeamCrudService;
import app.services.*;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
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

    @Autowired
    GameService gameService;

    @Autowired
    Statistic statistic;

    @Autowired
    CompetitionService competitionService;

    @Autowired
    UserService userService;

    @Autowired
    DBLogService dbLogService;

    int CURRENT_SEASON_YEAR = 2020;

    @PostMapping("/ui/users/authenticate")
    @ApiResponses({
            @ApiResponse(code = 200, message = "User exists"),
            @ApiResponse(code = 404, message = "No user present")
    })
    public ResponseEntity getUser(@RequestParam(value = "login") String login, @RequestParam(value = "pass") String pass) {
        DBLog dbLog = new DBLog();
        dbLog.setLocalDate(LocalDate.now());
        dbLog.setUserName(login);
        dbLog.setOperation("Attempt to login");
        dbLog.setDescription("User with login=" + login + " with password=" + pass + " attempts to logIn with status=");
        User user = userService.findUserByLogin(login);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // encoder.matches()
        if (user == null || !encoder.matches(pass, user.getEncryptedPassword())) {
            dbLog.setDescription(dbLog.getDescription() + "unsuccessful");
            dbLogService.save(dbLog);
            return new ResponseEntity<>("User" + login + " not found.", HttpStatus.NOT_FOUND);
        }
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserName(user.getLogin());
        authenticatedUser.setRole(user.getRole());
        user.getResponsibility().stream().forEach(team -> {
            authenticatedUser.getTeamsIds().add((int) team.getId());
        });
        user.setEncryptedPassword(null);
        dbLog.setDescription(dbLog.getDescription() + "SUCCESFULLY");
        dbLogService.save(dbLog);
        return new ResponseEntity<>(authenticatedUser, HttpStatus.OK);
    }

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
        result.forEach(player -> {
            player.setSeason(null);
            player.setTeam(null);
            player.setOffenses(null);
            player.setGoals(null);
        });
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
    public ResponseEntity saveNewTeam(@ModelAttribute Team team, @RequestParam(value = "file", required = false) MultipartFile file, @RequestParam(value = "userName") String login) {
        DBLog dbLog = new DBLog();
        dbLog.setLocalDate(LocalDate.now());
        dbLog.setUserName(login);
        dbLog.setOperation("SAVE NEW TEAM");
        dbLog.setDescription("User with login=" + login + " attempts to save new team (" + team + ")");
        dbLogService.save(dbLog);
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
    public ResponseEntity updateTeam(@ModelAttribute Team team, @RequestParam(value = "file", required = false) MultipartFile file, @RequestParam(value = "userName") String login) {
        // TODO: 01.06.2020 Set current season year from settings
        DBLog dbLog = new DBLog();
        dbLog.setLocalDate(LocalDate.now());
        dbLog.setUserName(login);
        dbLog.setOperation("UPDATE TEAM");
        dbLog.setDescription("User with login=" + login + " attempts to update team (" + team + ")");
        dbLogService.save(dbLog);
        team.setSeason(seasonService.findByYear(CURRENT_SEASON_YEAR));
        return ResponseEntity.status(teamCrudService.updateTeamFlow(team, file)).build();
    }

    @DeleteMapping("/ui/team/{id}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Team deleted from season successfully")
    })
    public ResponseEntity deleteTeamFromSeason(@PathVariable Long id) {
        DBLog dbLog = new DBLog();
        dbLog.setLocalDate(LocalDate.now());
        dbLog.setUserName("UNDEFINED");
        dbLog.setOperation("DELETE TEAM FROM SEASON");
        dbLog.setDescription("User attempts to delete team with id=" + id + " from season");
        dbLogService.save(dbLog);
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
        fillStatisticForPlayers(result);
        result.forEach(player -> {
            player.setSeason(null);
            player.setTeam(null);
            player.setGoals(null);
            player.setOffenses(null);
        });
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
    public ResponseEntity saveNewPlayer(@ModelAttribute Player player, @RequestParam(value = "file", required = false) MultipartFile file, @RequestParam(value = "teamId", required = true) String teamId, @RequestParam(value = "userName") String login) {
        // TODO: 01.06.2020 Set current season year from settings
        Team team = teamService.findTeamById(Long.parseLong(teamId));
        DBLog dbLog = new DBLog();
        dbLog.setLocalDate(LocalDate.now());
        dbLog.setUserName(login);
        dbLog.setOperation("CREATE NEW PLAYER");
        dbLog.setDescription("User with login=" + login + " attempts to create new player (" + player + ") for team " + (team != null ? team.getId() : "null"));
        dbLogService.save(dbLog);
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
    public ResponseEntity updatePlayer(@ModelAttribute Player player, @RequestParam(value = "file", required = false) MultipartFile file, @RequestParam(value = "teamId") String teamId, @RequestParam(value = "userName") String login) {
        // TODO: 01.06.2020 Set current season year from settings
        Team team = teamService.findTeamById(Long.parseLong(teamId));
        DBLog dbLog = new DBLog();
        dbLog.setLocalDate(LocalDate.now());
        dbLog.setUserName(login);
        dbLog.setOperation("UPDATE PLAYER");
        dbLog.setDescription("User with login=" + login + " attempts to update player (" + player + ") for team " + (team != null ? team.getId() : "null"));
        dbLogService.save(dbLog);
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
        DBLog dbLog = new DBLog();
        dbLog.setLocalDate(LocalDate.now());
        dbLog.setUserName("UNDEFINED");
        dbLog.setOperation("DELETE PLAYER FROM SEASON");
        dbLog.setDescription("User attempts to delete player with id=" + id + " from season");
        dbLogService.save(dbLog);
        return ResponseEntity.status(teamCrudService.deletePlayerFromSeasonFlow(id)).build();
    }


    @GetMapping(value = "/ui/standings")
    public ResponseEntity<List<StandingsRow>> getStandings(Model model) {
        List<StandingsRow> standingsRows = new ArrayList<>();
        for (Team team : teamService.findAllTeams()
        ) {
            StandingsRow standingsRow = new StandingsRow();
            standingsRow.setTeamName(team.getTeamName());
            // TODO: 13.02.2020 refactor hardcode for 'findCompetitionById(1)'. This value should be set in Configuration
            for (Game game : gameService.findGamesWithResultByTeamAndCompetition(team, competitionService.findCompetitionById(1), true)
            ) {
                standingsRow.setGames(standingsRow.getGames() + 1);
                if (team.equals(game.getMasterTeam())) {
                    standingsRow.setScoredGoals(standingsRow.getScoredGoals() + game.getMasterGoalsCount());
                    standingsRow.setConcededGoals(standingsRow.getConcededGoals() + game.getSlaveGoalsCount());
                    if (game.getMasterGoalsCount().equals(game.getSlaveGoalsCount()) && !game.isTechnicalMasterTeamWin() && !game.isTechnicalSlaveTeamWin()) {
                        standingsRow.setDraws(standingsRow.getDraws() + 1);
                    } else if (game.getMasterGoalsCount() > game.getSlaveGoalsCount() || game.isTechnicalMasterTeamWin()) {
                        standingsRow.setWins(standingsRow.getWins() + 1);
                    } else {
                        standingsRow.setLosses(standingsRow.getLosses() + 1);
                    }
                } else {
                    standingsRow.setScoredGoals(standingsRow.getScoredGoals() + game.getSlaveGoalsCount());
                    standingsRow.setConcededGoals(standingsRow.getConcededGoals() + game.getMasterGoalsCount());
                    if (game.getSlaveGoalsCount().equals(game.getMasterGoalsCount()) && !game.isTechnicalMasterTeamWin() && !game.isTechnicalSlaveTeamWin()) {
                        standingsRow.setDraws(standingsRow.getDraws() + 1);
                    } else if (game.getSlaveGoalsCount() > game.getMasterGoalsCount() || game.isTechnicalSlaveTeamWin()) {
                        standingsRow.setWins(standingsRow.getWins() + 1);
                    } else {
                        standingsRow.setLosses(standingsRow.getLosses() + 1);
                    }
                }
            }
            standingsRow.setRatioGoals(standingsRow.getScoredGoals() - standingsRow.getConcededGoals());
            standingsRow.setPoints(standingsRow.getWins() * 3 + standingsRow.getDraws());
            standingsRows.add(standingsRow);
        }
        sortStandings(standingsRows);
        //     model.addAttribute("standings", standingsRows);
        return new ResponseEntity<>(standingsRows, HttpStatus.OK);
    }

    private void sortStandings(List<StandingsRow> standingsRows) {
        standingsRows.sort(StandingsRow.COMPARE_BY_POINTS);
        Collections.reverse(standingsRows);
        for (int i = 0; i < standingsRows.size(); i++) {
            standingsRows.get(i).setNumber(i + 1);
        }
    }

    @GetMapping(value = "/ui/statistic/{command}")
    public ResponseEntity<List<PlayersForStatistic>> getStatistic(@PathVariable String command) {
        command += "All";
        HashMap<Player, Integer> map = new HashMap<>();
        if (statistic.isStatisticReady()) {
            if (statistic.getContext() != null) {
                map = (HashMap<Player, Integer>) statistic.getContext().getFromContext(command);
            }
        } else {
            Thread threadForStatistic = new Thread(statistic);
            threadForStatistic.start();
            return new ResponseEntity<>(null, HttpStatus.CONTINUE);
        }
        List<PlayersForStatistic> result = convertToPlayerForStatistic(map);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private List<PlayersForStatistic> convertToPlayerForStatistic(Map<Player, Integer> map) {
        List<PlayersForStatistic> result = new LinkedList<>();
        map.forEach((player, value) -> {
            PlayersForStatistic playersForStatistic = new PlayersForStatistic();
            playersForStatistic.setPlayerName(player.getLastName() + " " + player.getFirstName());
            playersForStatistic.setPhotoString(player.getPhotoString());
            playersForStatistic.setTeamName(player.getTeam().getTeamName());
            playersForStatistic.setSymbolString(player.getTeam().getSymbolString());
            playersForStatistic.setValue(value);
            result.add(playersForStatistic);
        });
        return result;
    }

    private void fillStatisticForPlayers(List<Player> players) {
        players.stream().forEach(player -> {
            player.setGoalsCount(player.getGoals().size());
            player.getOffenses().stream().forEach(offense -> {
                if (offense.getType().equals("YELLOW")) {
                    player.setYellowCardCount(player.getYellowCardCount() + 1);
                } else {
                    player.setRedCardCount(player.getRedCardCount() + 1);
                }
            });
        });
    }

    ;

}
