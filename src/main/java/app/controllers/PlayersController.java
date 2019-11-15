package app.controllers;

import app.Models.Competition;
import app.Models.Context;
import app.Models.Player;
import app.Models.Team;
import app.Utils.MessageGenerator;
import app.exceptions.DerffException;
import app.services.impl.PlayerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@Controller
public class PlayersController {

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    PlayerServiceImpl playerService;

    @Autowired
    Context context;

    @GetMapping(value = "/players")
    public String getListOfPlayers(Model model) throws DerffException {
        if (context.getFromContext("competition") == null) {
            throw new DerffException("notSelectedCompetition", null, null, "/competitions");
        } else if (context.getFromContext("team") == null) {
            throw new DerffException("notSelectedTeam", null, null, "/selectTeamForCompetition");
        } else {
            model.addAttribute("competition", context.getFromContext("competition"));
            model.addAttribute("team", context.getFromContext("team"));
        }
        List<Player> players = playerService.findAllPlayersInTeamForCurrentCompetition((Competition) context.getFromContext("competition"), (Team) context.getFromContext("team"));
        Collections.reverse(players);
        if (messageGenerator.isActive())
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());

        model.addAttribute("players", players);
        return "player/players";
    }

}
