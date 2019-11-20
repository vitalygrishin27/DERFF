package app.services;

import app.Models.Competition;
import app.Models.Player;
import app.Models.Team;

import java.util.List;

public interface PlayerService {
    void save(Player player);

    Player getPlayerById(long id);

    List<Player> findAllPlayers();

    List<Player> findAllPlayersInTeamForCurrentCompetition(Competition competition, Team team);
}