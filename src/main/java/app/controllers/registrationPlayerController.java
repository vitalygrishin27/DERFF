package app.controllers;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Controller
public class registrationPlayerController {

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    TeamServiceImpl teamService;

    @Autowired
    PlayerServiceImpl playerService;

    @Value("${maxUploadFileSizePlayerPhoto}")
    private Long maxUploadFileSizePlayerPhoto;

    @Value("${availableFileExtension}")
    private String availableFileExtension;

    @GetMapping(value = "/newPlayer")
    public String getDefaultForm(Model model) {
        model.addAttribute("titlePage", messageSource.getMessage("page.title.player.creating", null, Locale.getDefault()));
        Player player = new Player();
        if (messageGenerator.isActive()) {
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());
            if (messageGenerator.getTemporaryObjectForMessage() != null && messageGenerator.getTemporaryObjectForMessage().getClass().isInstance(new Player()))
                player = (Player) messageGenerator.getTemporaryObjectForMessageWithSetNull();
            model.addAttribute("preDate", dateToString(player.getBirthday()));
        } else {
            model.addAttribute("preDate", messageSource.getMessage("placeholder.DefaultDate", null, Locale.getDefault()));
        }
        model.addAttribute("teams", teamService.findAllTeams());
        model.addAttribute("player", player);
        return "regForms/regForm4Player";
    }

    @PostMapping(value = "/newPlayer")
    public String postdefault(@ModelAttribute("player") Player player,
                              // @ModelAttribute("team") Team team,
                              @ModelAttribute("preDate") String preDate,
                              @ModelAttribute("teamName") String teamName,
                              //  @ModelAttribute("isLegionary") Boolean isLegionary,
                              @ModelAttribute("file") MultipartFile file) throws DerffException {
        System.out.println(player);

        validatePlayerInformation(player, preDate, teamName, file);
        try {
            playerService.save(player);
            messageGenerator.setMessage((messageSource.getMessage("success.newPlayer", new Object[]{player.getFirstName()+" "+player.getLastName()}, Locale.getDefault())));
        } catch (Exception e) {
            throw new DerffException("database", player, new Object[]{e.getMessage()});
        }
        return "redirect:/newPlayer";
    }

    private void validatePlayerInformation(Player player, String preDate, String teamName, MultipartFile file) throws DerffException {

        //Date parse
        try {
            player.setBirthday(new SimpleDateFormat("dd/MM/yyyy").parse(preDate));
        } catch (Exception e) {
            throw new DerffException("date", player);
        }

        //Team validation
        if (teamName == null || teamName.isEmpty() || teamName.equals(messageSource.getMessage("placeholder.team", null, Locale.getDefault()))) {
            throw new DerffException("notSelectedTeam", player);
        } else {
            try {
                player.setTeam(teamService.findTeamByName(teamName));
            } catch (Exception e) {
                throw new DerffException("database", player, new Object[]{e.getMessage()});
            }
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

    private String dateToString(Date date) {
        Format formatter = new SimpleDateFormat("dd/MM/yyyy");
        return date == null ? null : formatter.format(date);
    }

  /*  private void prepareFileToSaveIntoDB(Team team){
            try {
                byte[] bytes = team.getSymbol().getBytes();
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(new File(name + "-uploaded")));
                stream.write(bytes);
                // byte[] en= Base64.encode(bytes);
                String encodedString = Base64.getEncoder().encodeToString(bytes);
                byte[] bytes2 = Base64.getDecoder().decode(encodedString);
                photoService.save(new Photo(1,bytes,null));
                stream.close();
                return "Вы удачно загрузили " + name + " в " + name + "-uploaded !";
            } catch (Exception e) {
                return "Вам не удалось загрузить " + name + " => " + e.getMessage();
            }
        }*/

}
