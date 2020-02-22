package app.services.impl;

import app.Models.Player;
import app.Models.PlayerRole;
import app.Models.Team;
import app.repository.PlayerRepository;
import app.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class PlayerServiceImpl implements PlayerService {
    @Autowired
    PlayerRepository repository;

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Override
    public void save(Player player) {
        repository.saveAndFlush(player);
    }

    @Override
    public Player findPlayerById(long id) {
        return repository.findPlayerById(id);
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
    public List<Player> findAllPlayersInTeam(Team team) {
        return repository.findAllPlayersInTeam(team);
    }

    @Override
    public List<Player> findAllActivePlayersInTeam(Team team){
        return repository.findAllActivePlayersInTeam(team, Boolean.FALSE);
    }

    @Override
    public List<Player> findAllActivePlayersInTeamByRole(Team team, PlayerRole playerRole){
        return repository.findAllActiveInTeamByRole(team, messageSource.getMessage(playerRole.getRole(),null, Locale.getDefault()),Boolean.FALSE);
    }

    @Override
    public Player findPlayerByRegistration(String registration) {
        return repository.findPlayerByRegistration(registration);
    }

    @Override
    public void update(Player player) {
        //repository.update(player.getId(),player.getLastName(),player.getFirstName(),player.getSecondName(),player.getBirthday(),player.getStringBirthday(),player.getIdCard(),player.getIsLegionary(),player.getRegistration(),player.getPhoto(),player.getTeam(),player.getInn(),player.getRole());
        repository.saveAndFlush(player);
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
