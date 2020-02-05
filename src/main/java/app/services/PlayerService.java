package app.services;

import app.Models.Competition;
import app.Models.Player;
import app.Models.Team;

import java.util.List;

public interface PlayerService {
    void save(Player player);

    Player getPlayerById(long id);

    Player getPlayerByIdCard(int id);

    List<Player> findAllPlayers();

    List<Player> findAllPlayersInTeamForCurrentCompetition(Competition competition, Team team);

    List<Player> findAllPlayersInTeam(Team team);

    List<Player> findAllActivePlayersInTeam(Team team);

    void update(Player player);

    void delete(Player player);
}
