package app.services.impl;

import app.Models.Competition;
import app.Models.Player;
import app.Models.Team;
import app.repository.PlayerRepository;
import app.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService {
    @Autowired
    PlayerRepository repository;

    @Override
    public void save(Player player) {
        repository.saveAndFlush(player);
    }

    @Override
    public Player getPlayerById(long id) {
        return repository.getOne(id);
    }

    @Override
    public Player getPlayerByIdCard(int id) {
        return repository.findByIdCard(id);
    }

    @Override
    public List<Player> findAllPlayers() {
        return repository.findAll();
    }

    @Override
    public List<Player> findAllPlayersInTeamForCurrentCompetition(Competition competition, Team team) {
        return repository.findAllPlayersInTeamForCurrentCompetition(competition,team);
    }

    @Override
    public void update(Player player) {
        repository.update(player.getId(),player.getLastName(),player.getFirstName(),player.getSecondName(),player.getBirthday(),player.getStringBirthday(),player.getIdCard(),player.getIsLegionary(),player.getRegistration(),player.getPhoto(),player.getTeam());
    }

    @Override
    public void delete(Player player) {
        repository.delete(player);
    }

    //  @Transactional
  //  @Modifying
  //  @Query("update User set login = :login, password = :password, role = :role where id = :id")
  //  void update(@Param("login") String login, @Param("password") String password, @Param("role") String role, @Param("id") long id);
}
