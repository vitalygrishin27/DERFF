package app.services;

import app.Models.Player;
import app.Models.PlayerRole;
import app.Models.Team;

import java.util.List;

public interface PlayerService {
    void save(Player player);

    Player findPlayerById(long id);

    Player getPlayerByIdCard(int id);

    List<Player> findAllPlayers();

    List<Player> findAllPlayersInTeam(Team team);

    List<Player> findAllActivePlayersInTeam(Team team);

    List<Player> findAllActivePlayersInTeamByRole(Team team, PlayerRole playerRole);

    Player findPlayerByRegistration(String registration);

    void update(Player player);

    void delete(Player player);
}
