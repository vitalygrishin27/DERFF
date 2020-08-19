package app.controllers.Crud;

import app.Models.*;
import app.controllers.Crud.Service.TeamCrudService;
import app.services.*;
import app.services.impl.DBLogServiceImpl;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
    GoalService goalService;

    @Autowired
    OffenseService offenseService;

    @Autowired
    TourService tourService;

    @Autowired
    Statistic statistic;

    @Autowired
    CompetitionService competitionService;

    @Autowired
    UserService userService;

    @Autowired
    DBLogServiceImpl dbLogService;

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
        // user.setEncryptedPassword(null);
        dbLog.setDescription(dbLog.getDescription() + "SUCCESFULLY");
        dbLogService.save(dbLog);
        return new ResponseEntity<>(authenticatedUser, HttpStatus.OK);
    }

    @RequestMapping("/ui/teams")
    public ResponseEntity<List<Team>> getAllTeam() {
        List<Team> list = teamService.findAllTeams();
        list.forEach(team -> team.setPlayers(null));
        list.forEach(team -> team.setSymbol(null));
        //list.forEach(team -> team.setSymbolString(null));
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @RequestMapping("/ui/competitions")
    public ResponseEntity<List<CompetitionForUI>> getAllCompetitions() {
        List<Competition> list = competitionService.findAllCompetition();
        //  list.forEach(team -> team.setPlayers(null));
        //   list.forEach(team -> team.setSymbol(null));
        //list.forEach(team -> team.setSymbolString(null));
        return new ResponseEntity<>(convertToCompetitionForUI(list), HttpStatus.OK);
    }

    @RequestMapping("/ui/competition/{competitionId}/tours")
    public ResponseEntity<List<TourForUI>> getAllToursInCompetition(@PathVariable Long competitionId) {
        List<Tour> list = tourService.findAll();
        List<Tour> result = list.stream().filter(tour -> tour.getCompetition().getId() == competitionId).collect(Collectors.toList());

        //  list.forEach(team -> team.setPlayers(null));
        //   list.forEach(team -> team.setSymbol(null));
        //list.forEach(team -> team.setSymbolString(null));
        return new ResponseEntity<>(convertToTourForUI(result), HttpStatus.OK);
    }

    @RequestMapping("/ui/competition/tours/{tourId}")
    public ResponseEntity<TourForUI> getTourById(@PathVariable Long tourId) {
        Tour tour = tourService.findById(tourId);

        //  list.forEach(team -> team.setPlayers(null));
        //   list.forEach(team -> team.setSymbol(null));
        //list.forEach(team -> team.setSymbolString(null));
        return new ResponseEntity<>(convertToTourForUI(tour), HttpStatus.OK);
    }

    private List<CompetitionForUI> convertToCompetitionForUI(List<Competition> list) {
        List<CompetitionForUI> result = new ArrayList<>();
        list.forEach(competition -> {
            CompetitionForUI competitionForUI = new CompetitionForUI();
            competitionForUI.setId(competition.getId());
            competitionForUI.setName(competition.getName());
            result.add(competitionForUI);
        });
        return result;
    }

    private List<TourForUI> convertToTourForUI(List<Tour> list) {
        List<TourForUI> result = new ArrayList<>();
        list.forEach(tour -> {
            result.add(convertToTourForUI(tour));
        });
        return result;
    }

    private TourForUI convertToTourForUI(Tour tour) {
        TourForUI tourForUI = new TourForUI();
        tourForUI.setId(tour.getId());
        tourForUI.setName(tour.getTourName());
        tourForUI.setDate(tour.getDate());
        return tourForUI;
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
        Map<String, String> resultGames = new LinkedHashMap<>();
        List<StandingsRow> standingsRows = new ArrayList<>();
        for (Team team : teamService.findAllTeams()
        ) {
            StandingsRow standingsRow = new StandingsRow();
            standingsRow.setTeamName(team.getTeamName());
            // TODO: 13.02.2020 refactor hardcode for 'findCompetitionById(1)'. This value should be set in Configuration
            for (Game game : gameService.findGamesWithResultByTeamAndCompetition(team, competitionService.findCompetitionById(1), true)
            ) {
                String key = team.getTeamName() + "-" + (game.getMasterTeam().equals(team) ? game.getSlaveTeam().getTeamName() : game.getMasterTeam().getTeamName());
                resultGames.put(key, (resultGames.containsKey(key) ? resultGames.get(key) + ", " : "") + (game.getMasterTeam().equals(team) ? game.getMasterGoalsCount() + ":" + game.getSlaveGoalsCount() : game.getSlaveGoalsCount() + ":" + game.getMasterGoalsCount()));
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
        standingsRows.forEach(standingsRow -> {
            LinkedList<String> gameResults = new LinkedList<>();
            standingsRows.forEach(standingsRow1 -> {
                gameResults.add(resultGames.getOrDefault(standingsRow.getTeamName() + "-" + standingsRow1.getTeamName(), "-"));
            });
            standingsRow.setGameResults(gameResults);
        });

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
        List<SkipGameEntry> list = new LinkedList<>();
        if (statistic.isStatisticReady()) {
            if (statistic.getContext() != null) {
                if (command.equals("skipGamesAll")) {
                    list = (List) statistic.getContext().getFromContext(command);
                } else {
                    map = (HashMap<Player, Integer>) statistic.getContext().getFromContext(command);
                }
            }
        } else {
            Thread threadForStatistic = new Thread(statistic);
            threadForStatistic.start();
            return new ResponseEntity<>(convertToPlayerForStatistic(map), HttpStatus.CONTINUE);
        }
        List<PlayersForStatistic> result = command.equals("skipGamesAll") ? convertToPlayerForStatistic(list) : convertToPlayerForStatistic(map);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private List<PlayersForStatistic> convertToPlayerForList(List<Player> input) {
        List<PlayersForStatistic> result = new LinkedList<>();
        input.forEach((player) -> {
            PlayersForStatistic playersForStatistic = new PlayersForStatistic();
            playersForStatistic.setId(player.getId());
            playersForStatistic.setPlayerName(player.getLastName() + " " + player.getFirstName() + " " + player.getSecondName());
            // playersForStatistic.setPhotoString(player.getPhotoString());
            //  playersForStatistic.setTeamName(player.getTeam().getTeamName());
            //   playersForStatistic.setSymbolString(player.getTeam().getSymbolString());
            //  playersForStatistic.setValue(value);
            result.add(playersForStatistic);
        });
        return result;
    }

    private List<PlayersForStatistic> convertToPlayerForStatistic(Map<Player, Integer> map) {
        List<PlayersForStatistic> result = new LinkedList<>();
        map.forEach((player, value) -> {
            PlayersForStatistic playersForStatistic = new PlayersForStatistic();
            playersForStatistic.setId(player.getId());
            playersForStatistic.setPlayerName(player.getLastName() + " " + player.getFirstName());
            playersForStatistic.setPhotoString(player.getPhotoString());
            playersForStatistic.setTeamName(player.getTeam() != null ? player.getTeam().getTeamName() : "Відзаявлений");
            playersForStatistic.setSymbolString(player.getTeam() != null ? player.getTeam().getSymbolString() : "");
            playersForStatistic.setValue(value);
            result.add(playersForStatistic);
        });
        return result;
    }

    private List<PlayersForStatistic> convertToPlayerForStatistic(List<SkipGameEntry> list) {
        List<PlayersForStatistic> result = new LinkedList<>();
        list.forEach(skipGameEntry -> {
            PlayersForStatistic playersForStatistic = new PlayersForStatistic();
            playersForStatistic.setId(skipGameEntry.getPlayer().getId());
            playersForStatistic.setPlayerName(skipGameEntry.getPlayer().getLastName() + " " + skipGameEntry.getPlayer().getFirstName());
            playersForStatistic.setPhotoString(skipGameEntry.getPlayer().getPhotoString());
            playersForStatistic.setTeamName(skipGameEntry.getPlayer().getTeam().getTeamName());
            playersForStatistic.setSymbolString(skipGameEntry.getPlayer().getTeam().getSymbolString());
            playersForStatistic.setStringDate(skipGameEntry.getStringDate());
            playersForStatistic.setDetails(skipGameEntry.getDetails());
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

    @GetMapping(value = "/ui/tours")
    public ResponseEntity<List<Tour>> getTours() {
        List<Tour> result = tourService.findAll();
        result.forEach(tour -> tour.setGames(null));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/ui/tours/{tourName}")
    public ResponseEntity<List<GameForCalendar>> getGamesInTours(@PathVariable String tourName) {
        List<Game> games = tourService.findByTourName(tourName).getGames();
        return new ResponseEntity<>(convertToGamesForCalendar(games), HttpStatus.OK);
    }

    private List<GameForCalendar> convertToGamesForCalendar(List<Game> list) {
        List<GameForCalendar> result = new LinkedList<>();
        list.forEach(game -> {
            GameForCalendar gameForCalendar = new GameForCalendar();
            gameForCalendar.setId(game.getId());
            gameForCalendar.setDate(game.getDate());
            gameForCalendar.setStringDate(game.getStringDate());
            gameForCalendar.setMasterGoalsCount(game.getMasterGoalsCount());
            gameForCalendar.setSlaveGoalsCount(game.getSlaveGoalsCount());
            gameForCalendar.setMasterTeamName(game.getMasterTeam().getTeamName());
            gameForCalendar.setSlaveTeamName(game.getSlaveTeam().getTeamName());
            gameForCalendar.setMasterTeamSymbolString(game.getMasterTeam().getSymbolString());
            gameForCalendar.setSlaveTeamSymbolString(game.getSlaveTeam().getSymbolString());
            gameForCalendar.setResultSave(game.isResultSave());
            gameForCalendar.setTechnicalMasterTeamWin(game.isTechnicalMasterTeamWin());
            gameForCalendar.setTechnicalSlaveTeamWin(game.isTechnicalSlaveTeamWin());
            result.add(gameForCalendar);
        });
        return result;
    }

    private GameForEditing convertToGameForEditing(Game game){
        GameForEditing result = new GameForEditing();
        result.setMasterTeamId(game.getMasterTeam().getId());
        result.setSlaveTeamId(game.getSlaveTeam().getId());
        result.setMasterTeamName(game.getMasterTeam().getTeamName());
        result.setSlaveTeamName(game.getSlaveTeam().getTeamName());
        result.setResultSave(game.isResultSave());
        return result;
    }

    private GameForResultPage convertToGameForResultPage(Game game) {
        GameForResultPage result = new GameForResultPage();
        result.setId(game.getId());
        result.setDate(game.getDate());
        result.setStringDate(game.getStringDate());
        result.setMasterGoalsCount(game.getMasterGoalsCount());
        result.setSlaveGoalsCount(game.getSlaveGoalsCount());
        result.setMasterTeamName(game.getMasterTeam().getTeamName());
        result.setSlaveTeamName(game.getSlaveTeam().getTeamName());
        result.setMasterTeamSymbolString(game.getMasterTeam().getSymbolString());
        result.setSlaveTeamSymbolString(game.getSlaveTeam().getSymbolString());
        result.setResultSave(game.isResultSave());
        result.setTechnicalMasterTeamWin(game.isTechnicalMasterTeamWin());
        result.setTechnicalSlaveTeamWin(game.isTechnicalSlaveTeamWin());

        List<Long> idsMasterPlayersGoals = new LinkedList<>();
        List<Long> idsSlavePlayersGoals = new LinkedList<>();
        List<Long> idsMasterPlayersYellowCards = new LinkedList<>();
        List<Long> idsSlavePlayersYellowCards = new LinkedList<>();
        List<Long> idsMasterPlayersRedCards = new LinkedList<>();
        List<Long> idsSlavePlayersRedCards = new LinkedList<>();
        for (Goal goal : game.getGoals()
        ) {
            if (goal.getTeam().equals(game.getMasterTeam())) {
                idsMasterPlayersGoals.add(goal.getPlayer().getId());
            } else {
                idsSlavePlayersGoals.add(goal.getPlayer().getId());
            }
        }
        for (Offense offense : game.getOffenses()
        ) {
            if (offense.getPlayer().getTeam().equals(game.getMasterTeam())) {
                if (offense.getType().equals("YELLOW")) {
                    idsMasterPlayersYellowCards.add(offense.getPlayer().getId());
                } else {
                    idsMasterPlayersRedCards.add(offense.getPlayer().getId());
                }
            } else {
                if (offense.getType().equals("YELLOW")) {
                    idsSlavePlayersYellowCards.add(offense.getPlayer().getId());
                } else {
                    idsSlavePlayersRedCards.add(offense.getPlayer().getId());
                }
            }
        }
        result.setIdsMasterPlayersGoals(idsMasterPlayersGoals);
        result.setIdsSlavePlayersGoals(idsSlavePlayersGoals);
        result.setIdsMasterPlayersYellowCards(idsMasterPlayersYellowCards);
        result.setIdsSlavePlayersYellowCards(idsSlavePlayersYellowCards);
        result.setIdsMasterPlayersRedCards(idsMasterPlayersRedCards);
        result.setIdsSlavePlayersRedCards(idsSlavePlayersRedCards);

        result.setMasterYellowCardsCount(idsMasterPlayersYellowCards.size());
        result.setSlaveYellowCardsCount(idsSlavePlayersYellowCards.size());
        result.setMasterRedCardsCount(idsMasterPlayersRedCards.size());
        result.setSlaveRedCardsCount(idsSlavePlayersRedCards.size());
        return result;
    }

    @GetMapping("/ui/games/{gameId}/players")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Players find successfully"),
            @ApiResponse(code = 404, message = "Players not found"),
            @ApiResponse(code = 500, message = "DataBase error")

    })
    public ResponseEntity<List<List<PlayersForStatistic>>> getPlayersByGame(@PathVariable String gameId) {
        Game game = gameService.findGameById(Integer.parseInt(gameId));
        List<Player> masterResult = playerService.findAllActivePlayersInTeam(game.getMasterTeam());
        teamCrudService.checkAvailableAutogoalInDB();
        masterResult.add(playerService.findPlayerByRegistration("AUTOGOAL"));
        //     (List) game.getMasterTeam().getPlayers();
        Collections.sort(masterResult);
        List<Player> slaveResult = playerService.findAllActivePlayersInTeam(game.getSlaveTeam());
        // (List) game.getSlaveTeam().getPlayers();
        slaveResult.add(playerService.findPlayerByRegistration("AUTOGOAL"));
        Collections.sort(slaveResult);
        List<List<PlayersForStatistic>> result = new ArrayList<>();

        result.add(convertToPlayerForList(masterResult));
        result.add(convertToPlayerForList(slaveResult));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/ui/games/result/{gameId}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Game find successfully"),
            @ApiResponse(code = 404, message = "Game not found"),
            @ApiResponse(code = 500, message = "DataBase error")

    })
    public ResponseEntity<GameForResultPage> getGameResult(@PathVariable String gameId) {
        Game game = gameService.findGameById(Integer.parseInt(gameId));
        return new ResponseEntity<>(convertToGameForResultPage(game), HttpStatus.OK);
    }

    @GetMapping("/ui/games/{gameId}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Game find successfully"),
            @ApiResponse(code = 404, message = "Game not found"),
            @ApiResponse(code = 500, message = "DataBase error")

    })
    public ResponseEntity<GameForEditing> getGameResult(@PathVariable Long gameId) {
        Game game = gameService.findGameById(gameId);
        return new ResponseEntity<>(convertToGameForEditing(game), HttpStatus.OK);
    }

    @PostMapping("/ui/gameResult/{gameId}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Game result was saved successfully"),
            @ApiResponse(code = 412, message = "Precondition Failed")
    })
    public ResponseEntity saveGameResult(@RequestParam(value = "gameId") Long gameId,
                                         @RequestParam(value = "masterTeamName") String masterTeamName,
                                         @RequestParam(value = "slaveTeamName") String slaveTeamName,
                                         @RequestParam(value = "countMasterGoals") Integer countMasterGoals,
                                         @RequestParam(value = "countSlaveGoals") Integer countSlaveGoals,
                                         @RequestParam(value = "masterPlayersGoals") List<Integer> masterPlayersGoals,
                                         @RequestParam(value = "slavePlayersGoals") List<Integer> slavePlayersGoals,
                                         @RequestParam(value = "countMasterYellowCards") Integer countMasterYellowCards,
                                         @RequestParam(value = "countSlaveYellowCards") Integer countSlaveYellowCards,
                                         @RequestParam(value = "masterPlayersYellowCards") List<Integer> masterPlayersYellowCards,
                                         @RequestParam(value = "slavePlayersYellowCards") List<Integer> slavePlayersYellowCards,
                                         @RequestParam(value = "countMasterRedCards") Integer countMasterRedCards,
                                         @RequestParam(value = "countSlaveRedCards") Integer countSlaveRedCards,
                                         @RequestParam(value = "masterPlayersRedCards") List<Integer> masterPlayersRedCards,
                                         @RequestParam(value = "slavePlayersRedCards") List<Integer> slavePlayersRedCards,
                                         @RequestParam(value = "isMasterTechnicalWin") Boolean isMasterTechnicalWin,
                                         @RequestParam(value = "isSlaveTechnicalWin") Boolean isSlaveTechnicalWin) {

        Game game = gameService.findGameById(gameId);
        if (game.isResultSave()) {
            return ResponseEntity.status(412).build();
        }

        if (isMasterTechnicalWin || isSlaveTechnicalWin) {
            game.setGoals(new ArrayList<>());
            game.setMasterGoalsCount(0);
            game.setSlaveGoalsCount(0);
            game.setOffenses(new ArrayList<>());
            if (isMasterTechnicalWin) {
                game.setTechnicalMasterTeamWin(true);
            } else {
                game.setTechnicalSlaveTeamWin(true);
            }
        }
//Set goals counts
        game.setMasterGoalsCount(countMasterGoals);
        game.setSlaveGoalsCount(countSlaveGoals);
// Set players goals
        List<Goal> goals = new ArrayList<>();
        for (int id : masterPlayersGoals
        ) {
            Goal goal = new Goal();
            goal.setTeam(game.getMasterTeam());
            goal.setGame(game);
            goal.setPlayer(playerService.findPlayerById(id));
            goals.add(goal);
        }
        for (int id : slavePlayersGoals
        ) {
            Goal goal = new Goal();
            goal.setTeam(game.getSlaveTeam());
            goal.setGame(game);
            goal.setPlayer(playerService.findPlayerById(id));
            goals.add(goal);
        }
        game.setGoals(goals);
// Set yellow cards
        List<Offense> offenses = new ArrayList<>();
        for (int id : masterPlayersYellowCards
        ) {
            Offense offense = new Offense();
            offense.setGame(game);
            offense.setType("YELLOW");
            offense.setPlayer(playerService.findPlayerById(id));
            offenses.add(offense);
        }
        for (int id : slavePlayersYellowCards
        ) {
            Offense offense = new Offense();
            offense.setGame(game);
            offense.setType("YELLOW");
            offense.setPlayer(playerService.findPlayerById(id));
            offenses.add(offense);
        }
        game.setOffenses(offenses);
//Set red cards
        List<Offense> offensesRed = new ArrayList<>();
        for (int id : masterPlayersRedCards
        ) {
            Offense offense = new Offense();
            offense.setGame(game);
            offense.setType("RED");
            offense.setPlayer(playerService.findPlayerById(id));
            offensesRed.add(offense);
        }
        for (int id : slavePlayersRedCards
        ) {
            Offense offense = new Offense();
            offense.setGame(game);
            offense.setType("RED");
            offense.setPlayer(playerService.findPlayerById(id));
            offensesRed.add(offense);
        }
        game.getOffenses().addAll(offensesRed);
//Save to DB
        for (Goal goal : game.getGoals()
        ) {
            goalService.save(goal);
        }
        for (Offense offense : game.getOffenses()
        ) {
            offenseService.save(offense);
        }
        game.setResultSave(true);
        gameService.save(game);
        return ResponseEntity.status(200).build();
    }

    @GetMapping("/ui/teamsList")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Teams find successfully"),
            @ApiResponse(code = 404, message = "Teams not found"),
            @ApiResponse(code = 500, message = "DataBase error")

    })
    public ResponseEntity<List<TeamsForCreatingGame>> getTeamsForCreatingGame() {
        return new ResponseEntity<>(convertToTeamsForCreatingGame(teamService.findAllTeams()), HttpStatus.OK);
    }

    private List<TeamsForCreatingGame> convertToTeamsForCreatingGame(List<Team> teams) {
        List<TeamsForCreatingGame> result = new ArrayList<>();
        teams.forEach(team -> {
            TeamsForCreatingGame teamsForCreatingGame = new TeamsForCreatingGame();
            teamsForCreatingGame.setId(team.getId());
            teamsForCreatingGame.setTeamName(team.getTeamName());
            result.add(teamsForCreatingGame);
        });
        return result;
    }

    @PostMapping("/ui/tours/{tourId}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Tour saved successfully"),
            @ApiResponse(code = 412, message = "Precondition Failed")
    })
    public ResponseEntity saveTour(@RequestParam(value = "tourId") Long id,
                                   @RequestParam(value = "tourName") String tourName,
                                   @RequestParam(value = "competitionId") Long competitionId,
                                   @RequestParam(value = "tourDate") Date date) {
        Tour tour;
        if (id != 0) {
            tour = tourService.findById(id);
        } else {
            tour = new Tour();
            tour.setGames(new ArrayList<>());
            tour.setCompetition(competitionService.findCompetitionById(competitionId));
        }
        tour.setTourName(tourName);
        tour.setDate(date);
        tourService.save(tour);
        return ResponseEntity.status(200).build();
    }

    @PostMapping("/ui/games/{gameId}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Game was saved successfully"),
            @ApiResponse(code = 412, message = "Precondition Failed")
    })
    public ResponseEntity saveGame(@RequestParam(value = "gameId") Long gameId,
                                   @RequestParam(value = "tourId") Long tourId,
                                   @RequestParam(value = "masterTeamId") Long masterTeamId,
                                   @RequestParam(value = "slaveTeamId") Long slaveTeamId
    ) {

        Tour tour = tourService.findById(tourId);
        Game game;
        if (gameId != -1) {
            game = gameService.findGameById(gameId);
        } else {
            game = new Game();
            game.setTour(tour);
            game.setCompetition(tour.getCompetition());
            game.setDate(tour.getDate());
            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
            String strDate = dateFormat.format(tour.getDate());
            game.setStringDate(strDate);
            game.setGoals(new ArrayList<>());
            game.setOffenses(new ArrayList<>());
            game.setMasterGoalsCount(0);
            game.setSlaveGoalsCount(0);
            game.setTechnicalMasterTeamWin(false);
            game.setTechnicalSlaveTeamWin(false);
            game.setResultSave(false);
        }
        game.setMasterTeam(teamService.findTeamById(masterTeamId));
        game.setSlaveTeam(teamService.findTeamById(slaveTeamId));

        gameService.save(game);
        return ResponseEntity.status(200).build();
    }

}
