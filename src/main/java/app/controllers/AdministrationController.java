package app.controllers;

import app.Models.*;
import app.Utils.ConfigurationKey;
import app.Utils.MessageGenerator;
import app.exceptions.DerffException;
import app.services.impl.ConfigurationImpl;
import app.services.impl.GameServiceImpl;
import app.services.impl.PlayerServiceImpl;
import app.services.impl.TeamServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static app.Utils.ConfigurationKey.*;

@Controller
public class AdministrationController {

    @Value("${maxUploadFileSizeTeamSymbol}")
    private Long maxUploadFileSizeTeamSymbol;

    @Value("${maxUploadFileSizePlayerPhoto}")
    private Long maxUploadFileSizePlayerPhoto;

    @Value("${availableFileExtension}")
    private String availableFileExtension;

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    TeamServiceImpl teamService;

    @Autowired
    PlayerServiceImpl playerService;

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    GameServiceImpl gameService;

    @Autowired
    ConfigurationImpl configurationService;

    @Autowired
    Context context;

    @GetMapping(value = "/")
    public String getMainPage(Model model) throws DerffException {
        if (messageGenerator.isActive())
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());

        return "administration/mainPage";
    }

    @GetMapping(value = "/administration/teams")
    public String getTeams(Model model) throws DerffException {
        if (messageGenerator.isActive())
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());
        model.addAttribute("teams", teamService.findAllTeams());
        return "administration/teams";
    }

    @DeleteMapping(value = "/administration/teams")
    public void deleteTeam(@ModelAttribute("teamId") Long teamId) throws DerffException {
        Team team = teamService.findTeamById(teamId);
        if (!gameService.findGameWithTeam(team).isEmpty()) {
            // TODO: 20.11.2019 циклический вызов
            throw new DerffException("notAvailableDeleteTeamWithGame", null, new Object[]{team.getTeamName()});
        }
        try {

            teamService.delete(team);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.deleteTeam", new Object[]{team.getTeamName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", new Object[]{e.getMessage()});
        }
    }

    @GetMapping(value = "/newTeam")
    public String getTeamForm(Model model) throws DerffException {
        Team team = new Team();
        if (messageGenerator.isActive()) {
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator
                    .getTemporaryObjectForMessage().getClass().isInstance(new Team()))
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
            messageGenerator.setMessage((messageSource
                    .getMessage("success.newTeam", new Object[]{team.getTeamName()}, Locale.getDefault())));
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


    @GetMapping(value = "/administration/calendar")
    public String getCalendar(Model model) throws DerffException {
       // model.addAttribute("teams", teamService.findAllTeams());
        return "administration/calendar";
    }

    @PostMapping(value = "/administration/calendar")
    public String addGame(@ModelAttribute("preDate") String preDate, @ModelAttribute("masterTeamName") String masterTeamName, @ModelAttribute("slaveTeamName") String slaveTeamName) throws DerffException {
        Game newGame = new Game();
        validateTeamInformation(newGame, preDate, masterTeamName, slaveTeamName);
        try {
            gameService.save(newGame);
            messageGenerator.setMessage((messageSource.getMessage("success.newGame", null, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", newGame, new Object[]{e.getMessage()});
        }
        return "redirect:/administration/calendar";
    }


    @DeleteMapping(value = "/administration/calendar")
    public void deleteGame(Integer gameId) throws DerffException {
        Game game = gameService.findGameById(gameId);
        try {
            gameService.delete(game);
            messageGenerator.setMessage((messageSource.getMessage("success.deleteGame", null, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", game, new Object[]{e.getMessage()});
        }

    }

    @GetMapping(value = "/administration/players")
    public String getListOfPlayers(Model model) throws DerffException {
        if (messageGenerator.isActive())
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());
        model.addAttribute("teams", teamService.findAllTeams());
        // model.addAttribute("players", playerService.findAllPlayers());
        // model.addAttribute("players", players);
        return "administration/players";
    }

    @PostMapping(value = "/administration/playerListByTeam")
    public String getPlayersByTeam(Model model, @ModelAttribute("teamName") String teamName) throws DerffException {
        List players = playerService.findAllPlayersInTeam(teamService.findTeamByName(teamName));

        model.addAttribute("players", players);
        // model.addAttribute("teams", teamService.findAllTeams());

        // model.addAttribute("players", playerService.findAllPlayers());
        //model.addAttribute("player", new Player());
        return "administration/playersByTeam";
        // return "efewfewfewf";
    }

    @GetMapping(value = "/administration/newPlayer")
    public String getFormforNewPlayer(Model model) throws DerffException {
        Player player = new Player();
        if (messageGenerator.isActive()) {
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator
                    .getTemporaryObjectForMessage().getClass().isInstance(new Player())) {
                player = (Player) messageGenerator.getTemporaryObjectForMessage();
                player.setBirthday(null);
            }

            // model.addAttribute("preDate", dateToString(((Player)obj).getBirthday()));

        }
        model.addAttribute("player", player);
        model.addAttribute("teams", teamService.findAllTeams());

        // model.addAttribute("players", playerService.findAllPlayers());
        // model.addAttribute("players", players);
        return "administration/newPlayer";
    }


    @PostMapping(value = "/administration/newPlayer")
    public String saveNewPlayer(@ModelAttribute("player") Player player,
                                // @ModelAttribute("team") Team team,
                                // @ModelAttribute("preDate") String preDate,
                                @ModelAttribute("teamName") String teamName,
                                // @ModelAttribute("isLegionary") String isLegionary,
                                @ModelAttribute("file") MultipartFile file) throws DerffException {

        validatePlayerInformation(player, teamName, file);
        try {
            playerService.save(player);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.newPlayer", new Object[]{player.getFirstName() + " " + player
                            .getLastName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", player, new Object[]{e.getMessage()});
        }

        return "redirect:/administration/newPlayer";
    }

    @GetMapping(value = "/administration/editPlayer/{id}")
    public String getFormforEditPlayer(Model model, @PathVariable("id") long id) throws DerffException {
        Player player = new Player();
        try {
            player = playerService.getPlayerById(id);
        } catch (Exception e) {
            throw new DerffException("playerNotExists", player, new Object[]{id, e.getMessage()}, "/administration/players");
        }

        if (messageGenerator.isActive()) {
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator
                    .getTemporaryObjectForMessage().getClass().isInstance(new Player())) {
                player = (Player) messageGenerator.getTemporaryObjectForMessage();
                player.setBirthday(null);
            }
        }
        model.addAttribute("player", player);
        model.addAttribute("teams", teamService.findAllTeams());
        model.addAttribute("titlePage", messageSource.getMessage("page.title.player.editing", null, Locale.getDefault()));

        return "administration/editPlayer";
    }

    @PostMapping(value = "/administration/editPlayer/{id}")
    public String savePlayerAfterEdit(@ModelAttribute("player") Player player,
                                      // @ModelAttribute("team") Team team,
                                      // @ModelAttribute("preDate") String preDate,
                                      @ModelAttribute("teamName") String teamName,
                                      // @ModelAttribute("isLegionary") String isLegionary,
                                      @ModelAttribute("file") MultipartFile file) throws DerffException {

        validatePlayerInformation(player, teamName, file);
        try {
            playerService.update(player);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.updatePlayer", new Object[]{player.getFirstName() + " " + player
                            .getLastName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", player, new Object[]{e.getMessage()});
        }

        return "redirect:/administration/players";
    }

    @GetMapping(value = "/administration/deletePlayer/{id}")
    public String deletePlayer(Model model, @PathVariable("id") long id) throws DerffException {
        Player player = new Player();
        try {
            player = playerService.getPlayerById(id);
        } catch (Exception e) {
            throw new DerffException("playerNotExists", player, new Object[]{id, e.getMessage()}, "/administration/players");
        }
        try {
            playerService.delete(player);
        } catch (Exception e) {
            throw new DerffException("database", player, new Object[]{e.getMessage()});
        }


        return "redirect:/administration/players";
    }


    @PostMapping(value = "/administration/gameListByDate")
    public String getGamesByDate(Model model, @ModelAttribute("date") String stringDate, @ModelAttribute("round") String round) throws DerffException {

        List<Game> games = new ArrayList<>();
        try {
            switch (round) {
                case "none":
                    Date date = null;
                    try {
                        date = new SimpleDateFormat("yyyy-MM-dd").parse(stringDate);
                    } catch (ParseException e) {
                        throw new DerffException("date");
                    }
                    games = gameService.findGamesByDate(date);
                    break;
                case "first":
                    games = gameService.findGamesBetweenDates(new SimpleDateFormat("yyyy-MM-dd").parse(configurationService.getValue(FIRST_ROUND_BEGIN)), new SimpleDateFormat("yyyy-MM-dd").parse(configurationService.getValue(FIRST_ROUND_END)));
                    break;
                case "second":
                    games = gameService.findGamesBetweenDates(new SimpleDateFormat("yyyy-MM-dd").parse(configurationService.getValue(SECOND_ROUND_BEGIN)), new SimpleDateFormat("yyyy-MM-dd").parse(configurationService.getValue(SECOND_ROUND_END)));
                    break;
            }
        } catch (Exception e) {
            throw new DerffException("database");
        }
        model.addAttribute("games", games);
        return "administration/gamesByDate";
    }

    @GetMapping(value = "/administration/newGame")
    public String getFormforNewGame(Model model) throws DerffException {
        Game game = new Game();
        Team team = new Team();
        game.setMasterTeam(team);
        game.setSlaveTeam(team);
        game.setDate(new Date());
        if (messageGenerator.isActive()) {
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator
                    .getTemporaryObjectForMessage().getClass().isInstance(new Game())) {
                game = (Game) messageGenerator.getTemporaryObjectForMessage();
            }

            // model.addAttribute("preDate", dateToString(((Player)obj).getBirthday()));

        }
        model.addAttribute("game", game);
        model.addAttribute("stringDate", "");
        model.addAttribute("teams", teamService.findAllTeams());

        // model.addAttribute("players", playerService.findAllPlayers());
        // model.addAttribute("players", players);
        return "administration/newGame";
    }


    @PostMapping(value = "/administration/newGame")
    public String saveNewGame(@ModelAttribute("game") Game game,
                              // @ModelAttribute("team") Team team,
                              // @ModelAttribute("preDate") String preDate,
                              @ModelAttribute("masterTeamName") String masterTeamName,
                              @ModelAttribute("slaveTeamName") String slaveTeamName,
                              @ModelAttribute("stringDate") String stringDate) throws DerffException
    // @ModelAttribute("isLegionary") String isLegionary,
    // @ModelAttribute("file") MultipartFile file)
    {

        validateGameInformation(game, masterTeamName, slaveTeamName, stringDate);
        try {
            gameService.save(game);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.newGame", new Object[]{game.getMasterTeam().getTeamName() + " - " +
                            game.getSlaveTeam().getTeamName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", game, new Object[]{e.getMessage()});
        }

        return "redirect:/administration/newGame";
    }

    @GetMapping(value = "/administration/editGame/{id}")
    public String getFormforEditGame(Model model, @PathVariable("id") long id) throws DerffException {
        Game game = new Game();
        try {
            game = gameService.findGameById(id);
        } catch (Exception e) {
            throw new DerffException("gameNotExists", game, new Object[]{id, e.getMessage()}, "/administration/calendar");
        }

        if (messageGenerator.isActive()) {
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator
                    .getTemporaryObjectForMessage().getClass().isInstance(new Game())) {
                game = (Game) messageGenerator.getTemporaryObjectForMessage();
            }
        }
        model.addAttribute("game", game);
        model.addAttribute("teams", teamService.findAllTeams());
        model.addAttribute("titlePage", messageSource.getMessage("page.title.game.editing", null, Locale.getDefault()));

        return "administration/editGame";
    }

    @PostMapping(value = "/administration/editGame/{id}")
    public String savePlayerAfterEdit(@ModelAttribute("game") Game game,
                                      @ModelAttribute("masterTeamName") String masterTeamName,
                                      @ModelAttribute("slaveTeamName") String slaveTeamName,
                                      @ModelAttribute("stringDate") String stringDate
                                      // @ModelAttribute("isLegionary") String isLegionary,
                                    //  @ModelAttribute("file") MultipartFile file
                                        ) throws DerffException {

        validateGameInformation(game, masterTeamName, slaveTeamName, stringDate);
        try {
            gameService.update(game);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.editGame", new Object[]{game.getMasterTeam().getTeamName() + " - " +
                            game.getSlaveTeam().getTeamName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", game, new Object[]{e.getMessage()});
        }

        return "redirect:/administration/calendar";
    }



    @GetMapping(value = "/administration/deleteGame/{id}")
    public String deleteGame(Model model, @PathVariable("id") long id) throws DerffException {
        Game game = new Game();
        try {
            game = gameService.findGameById(id);
        } catch (Exception e) {
            throw new DerffException("playerNotExists", game, new Object[]{id, e.getMessage()}, "/administration/calendar");
        }
        try {
            gameService.delete(game);
        } catch (Exception e) {
            throw new DerffException("database", game, new Object[]{e.getMessage()});
        }


        return "redirect:/administration/calendar";
    }


    @GetMapping(value="/administration/resultGame/{id}")
    public String firstStepResultsGoalsCount(Model model,@PathVariable("id") long id){
        context.clear();
    //    context.putToContext("countGoalsMasterTeam",0);
   //     context.putToContext("countGoalsSlaveTeam",0);
        Game game=gameService.findGameById(id);
        context.putToContext("game",game);
    //    model.addAttribute("game",game);
        model.addAttribute("masterTeamName",game.getMasterTeam().getTeamName());
        model.addAttribute("slaveTeamName",game.getSlaveTeam().getTeamName());
        model.addAttribute("countGoalsMasterTeam",0);
        model.addAttribute("countGoalsSlaveTeam",0);
        return "administration/resultGame";
    }

    @PostMapping(value = "/administration/resultGame")
    public String saveCountOfGoals(Model model,
                                   @ModelAttribute("step") String step,
                                   @ModelAttribute("countGoalsMasterTeam") String countGoalsMasterTeam,
                                   @ModelAttribute("countGoalsSlaveTeam") String countGoalsSlaveTeam) throws DerffException {
    switch (step){
        case "goalsCount":
            if(countGoalsMasterTeam.equals("")) countGoalsMasterTeam="0";
            if(countGoalsSlaveTeam.equals("")) countGoalsSlaveTeam="0";
            Game game=(Game)context.getFromContext("game");
            game.setMasterGoalsCount(Integer.valueOf(countGoalsMasterTeam));
            game.setSlaveGoalsCount(Integer.valueOf(countGoalsSlaveTeam));

            model.addAttribute("masterTeamName",game.getMasterTeam().getTeamName());
            model.addAttribute("slaveTeamName",game.getSlaveTeam().getTeamName());
            model.addAttribute("countMasterGoals",countGoalsMasterTeam);
            model.addAttribute("countSlaveGoals",countGoalsSlaveTeam);
            model.addAttribute("masterTeamPlayers", getFullNamePlayersList(playerService.findAllPlayersInTeam(game.getMasterTeam())));
            model.addAttribute("slaveTeamPlayers", getFullNamePlayersList(playerService.findAllPlayersInTeam(game.getSlaveTeam())));
    }

        return "administration/resultGameGoalsPlayers";
    }

    private List<String> getFullNamePlayersList(List<Player> players){
        List<String> result=new ArrayList<>();
        StringBuilder stringBuilder=new StringBuilder();
        for (Player player:players
             ) {
            result.add(stringBuilder.append(player.getLastName()).append(" ").append(player.getFirstName()).append(" ").append(player.getSecondName()).toString());
        }
        return result;
    }


    private void validatePlayerInformation(Player player, String teamName, MultipartFile file) throws DerffException {

        //Date validate
        if (player.getStringBirthday() == null) {
            throw new DerffException("date", player);
        }

        try {
            player.setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse(player.getStringBirthday()));
        } catch (ParseException e) {
            throw new DerffException("date", player);
        }


        //Team validation
        if (teamName == null || teamName.isEmpty() || teamName
                .equals(messageSource.getMessage("placeholder.team", null, Locale.getDefault()))) {
            throw new DerffException("notSelectedTeam", player);
        } else {
            try {
                player.setTeam(teamService.findTeamByName(teamName));
            } catch (Exception e) {
                throw new DerffException("database", player, new Object[]{e.getMessage()});
            }
        }

        // Id card validation
        if (player.getIdCard() != null && playerService.getPlayerByIdCard(player.getIdCard()) != null) {
            throw new DerffException("IdCardNotCorrect", player);
        }

        // File size validation
        if (file.getSize() > maxUploadFileSizePlayerPhoto)
            throw new DerffException("maxUploadFileSizePlayerPhoto", player, new Object[]{maxUploadFileSizePlayerPhoto, file.getSize()});

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
                throw new DerffException("notAvailableFileExtension", player, new Object[]{availableFileExtension});

            //Set byte[] to Player photo
            try {
                player.setPhoto(file.getBytes());
            } catch (IOException e) {
                throw new DerffException("fileGetBytes", player, new Object[]{e.getMessage()});
            }
        }

    }

    private void validateTeamInformation(Game newGame, String preDate, String masterTeamName, String slaveTeamName) throws DerffException {

        Team master;
        Team slave;

        //Date parse
        try {
            newGame.setDate(new SimpleDateFormat("dd/MM/yyyy").parse(preDate));
        } catch (Exception e) {
            throw new DerffException("date", newGame);
        }


        // validate teams
        try {
            master = teamService.findTeamByName(masterTeamName);
            slave = teamService.findTeamByName(slaveTeamName);
            newGame.setMasterTeam(master);
            newGame.setSlaveTeam(slave);
        } catch (Exception e) {
            throw new DerffException("game");
        }

        // validate not same teams
        if (master.getId() == slave.getId()) {
            throw new DerffException("sameTeams", newGame);
        }


    }

    private void validateGameInformation(Game game, String masterTeamName, String slaveTeamName, String stringDate) throws DerffException {
        //validate Teams
        if (masterTeamName.equals(slaveTeamName)) {
            throw new DerffException("sameTeam", game);
        }


        game.setMasterTeam(teamService.findTeamByName(masterTeamName));
        game.setSlaveTeam(teamService.findTeamByName(slaveTeamName));
        game.setStringDate(stringDate);


        try {
            game.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(stringDate));
        } catch (ParseException e) {
            throw new DerffException("date", game);
        }
    }

    private String dateToString(Date date) {
        Format formatter = new SimpleDateFormat("dd/MM/yyyy");
        return date == null ? null : formatter.format(date);
    }

}
