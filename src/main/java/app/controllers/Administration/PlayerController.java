package app.controllers.Administration;

import app.Models.Player;
import app.Models.Team;
import app.Utils.MessageGenerator;
import app.exceptions.DerffException;
import app.services.impl.PlayerServiceImpl;
import app.services.impl.TeamServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Controller
public class PlayerController {

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
    TeamServiceImpl teamService;

    @Autowired
    PlayerServiceImpl playerService;

    @GetMapping(value = "/administration/players/{id}")
    public String getListContainerWithId(Model model, @PathVariable("id") long id) {
        if (messageGenerator.isActive())
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
        model.addAttribute("teams", teamService.findAllTeams());
        model.addAttribute("activeTeamId", id);
        return "administration/player/players";
    }
    @GetMapping(value = "/administration/players")
    public String getListContainer() {
        List<Team> teams =teamService.findAllTeams();
        if(teams.isEmpty()){
            return "redirect:/administration/players/-1";
        }
        return "redirect:/administration/players/"+teams.get(0).getId();
    }

    @PostMapping(value = "/administration/playerListByTeam/{id}")
    public String getPlayersByTeam(Model model, @PathVariable("id") long id) {
        List players = playerService.findAllActivePlayersInTeam(teamService.findTeamById(id));
        Collections.sort(players);
        model.addAttribute("players", players);
        return "administration/player/playersByTeam";
    }

    @GetMapping(value = "/administration/newPlayer/{id}")
    public String getFormforNewPlayer(Model model, @PathVariable("id") long id) {
        Player player = new Player();
        if (messageGenerator.isActive()) {
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator
                    .getTemporaryObjectForMessage().getClass().isInstance(new Player())) {
                player = (Player) messageGenerator.getTemporaryObjectForMessageWithSetNull();
                player.setBirthday(null);
            }
        }
        model.addAttribute("titleNewPlayer", messageSource
                .getMessage("label.newPlayer", new Object[]{teamService.findTeamById(id).getTeamName()}, Locale.getDefault()));
        model.addAttribute("player", player);
        return "administration/player/newPlayer";
    }

    @PostMapping(value = "/administration/newPlayer/{teamId}")
    public String saveNewPlayer(@ModelAttribute("player") Player player,
                                @ModelAttribute("file") MultipartFile file,
                                @PathVariable("teamId") long teamId) throws DerffException {
        validatePlayerInformation(player, teamId, file);
        try {
            playerService.save(player);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.newPlayer", new Object[]{player.getFirstName() + " " + player
                            .getLastName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", player, new Object[]{e.getMessage()});
        }
        return "redirect:/administration/newPlayer/" + teamId;
    }

    @GetMapping(value = "/administration/editPlayer/{teamId}/{id}")
    public String getFormforEditPlayer(Model model, @PathVariable("id") long id) throws DerffException {
        Player player = new Player();
        try {
            player = playerService.getPlayerById(id);
        } catch (Exception e) {
            throw new DerffException("playerNotExists", player, new Object[]{id, e.getMessage()}, "/administration/players");
        }
        if (messageGenerator.isActive()) {
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator
                    .getTemporaryObjectForMessage().getClass().isInstance(new Player())) {
                player = (Player) messageGenerator.getTemporaryObjectForMessage();
                player.setBirthday(null);
            }
        }
        model.addAttribute("player", player);
        model.addAttribute("teams", teamService.findAllTeams());
        return "administration/player/editPlayer";
    }

    @PostMapping(value = "/administration/editPlayer/{teamId}/{id}")
    public String savePlayerAfterEdit(@ModelAttribute("player") Player player,
                                      @ModelAttribute("teamName") String teamName,
                                      @ModelAttribute("file") MultipartFile file,
                                      @PathVariable("teamId") long teamId) throws DerffException {
        validatePlayerInformation(player, teamService.findTeamByName(teamName).getId(), file);
        try {
            playerService.update(player);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.updatePlayer", new Object[]{player.getFirstName() + " " + player
                            .getLastName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", player, new Object[]{e.getMessage()});
        }
        return "redirect:/administration/players/"+teamId;
    }

    @GetMapping(value = "/administration/deletePlayer/{teamId}/{id}")
    public String deletePlayer(@PathVariable("id") long id,@PathVariable("teamId") long teamId) throws DerffException {
        Player player = new Player();
        try {
            player = playerService.getPlayerById(id);
        } catch (Exception e) {
            throw new DerffException("playerNotExists", player, new Object[]{id, e.getMessage()}, "/administration/players");
        }
        try {
            playerService.delete(player);
            messageGenerator.setMessage((messageSource
                    .getMessage("success.deletePlayer", new Object[]{player.getFirstName() + " " + player
                            .getLastName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", player, new Object[]{e.getMessage()});
        }
        return "redirect:/administration/players/"+teamId;
    }

    private void validatePlayerInformation(Player player, long id, MultipartFile file) throws DerffException {
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
        try {
            player.setTeam(teamService.findTeamById(id));
        } catch (Exception e) {
            throw new DerffException("database", player, new Object[]{e.getMessage()});
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
}