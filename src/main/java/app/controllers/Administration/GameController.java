package app.controllers.Administration;

import app.Models.Competition;
import app.Models.Game;
import app.Utils.MessageGenerator;
import app.exceptions.DerffException;
import app.services.impl.CompetitionServiceImpl;
import app.services.impl.ConfigurationImpl;
import app.services.impl.GameServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static app.Utils.ConfigurationKey.*;
import static app.Utils.ConfigurationKey.SECOND_ROUND_END;

@Controller
public class GameController {
    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    GameServiceImpl gameService;

    @Autowired
    ConfigurationImpl configurationService;

    @Autowired
    CompetitionServiceImpl competitionService;

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @GetMapping(value = "/administration/calendar")
    public String getCalendar(Model model) {
        if (messageGenerator.isActive()) {
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
        }
        List<Competition>competitions=competitionService.findAllCompetition();
        competitions.add(new Competition(-1,messageSource.getMessage("label.competitions.all",null, Locale.getDefault()),null));
        model.addAttribute("competitions",competitions);
        return "administration/game/calendar";
    }

    @PostMapping(value = "/administration/gameListByDate")
    public String getGamesByDate(Model model, @ModelAttribute("date") String stringDate, @ModelAttribute("round") String round, @ModelAttribute("competition") Competition competition) throws DerffException {
        List<Game> games = new ArrayList<>();
        try {
            switch (round) {
                case "date":
                    Date date;
                    try {
                        date = new SimpleDateFormat("yyyy-MM-dd").parse(stringDate);
                    } catch (ParseException e) {
                        break;
                    }
                    if (competition.getName().equals("All")) {
                        games = gameService.findGamesByDate(date);
                    }else{
                        games = gameService.findGamesByDateAndCompetition(date, competition);
                    }
                    break;
                case "first":
                    if (competition.getName().equals("All")) {
                        games = gameService.findGamesBetweenDates(new SimpleDateFormat("yyyy-MM-dd")
                                .parse(configurationService.getValue(FIRST_ROUND_BEGIN)), new SimpleDateFormat("yyyy-MM-dd")
                                .parse(configurationService.getValue(FIRST_ROUND_END)));
                    }else{
                        games = gameService.findGamesBetweenDatesAndCompetition(new SimpleDateFormat("yyyy-MM-dd")
                                .parse(configurationService.getValue(FIRST_ROUND_BEGIN)), new SimpleDateFormat("yyyy-MM-dd")
                                .parse(configurationService.getValue(FIRST_ROUND_END)), competition);
                    }
                    break;
                case "second":
                    if (competition.getName().equals("All")) {
                        games = gameService.findGamesBetweenDates(new SimpleDateFormat("yyyy-MM-dd")
                                .parse(configurationService
                                        .getValue(SECOND_ROUND_BEGIN)), new SimpleDateFormat("yyyy-MM-dd")
                                .parse(configurationService.getValue(SECOND_ROUND_END)));
                    }else{
                        games = gameService.findGamesBetweenDatesAndCompetition(new SimpleDateFormat("yyyy-MM-dd")
                                .parse(configurationService
                                        .getValue(SECOND_ROUND_BEGIN)), new SimpleDateFormat("yyyy-MM-dd")
                                .parse(configurationService.getValue(SECOND_ROUND_END)), competition);
                    }
                    break;
                case "all":
                    if (competition.getName().equals("All")) {
                        games = gameService.findAllGames();
                    }else {
                        games = gameService.findAllGamesByCompetition(competition);
                    }
                    break;
            }
        } catch (Exception e) {
            throw new DerffException("database");
        }
        model.addAttribute("games", games);
        return "administration/game/gamesByDate";
    }


}
