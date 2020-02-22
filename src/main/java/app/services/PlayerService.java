package app.services;

import app.Models.Player;
import app.Models.Team;

import java.util.List;

public interface PlayerService {
    void save(Player player);

    Player findPlayerById(long id);

    Player getPlayerByIdCard(int id);

    List<Player> findAllPlayers();

    List<Player> findAllPlayersInTeam(Team team);

    List<Player> findAllActivePlayersInTeam(Team team);

    Player findPlayerByRegistration(String registration);

    void update(Player player);

    void delete(Player player);
}
