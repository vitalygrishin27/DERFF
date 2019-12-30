package app.services.impl;

import app.Models.Game;
import app.Models.Team;
import app.repository.GameRepository;
import app.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameServiceImpl implements GameService {
    @Autowired
    GameRepository repository;

    @Override
    public void save(Game game) {
        repository.saveAndFlush(game);

    }

    @Override
    public Game findGameById(long id) {
        return repository.getOne(id);
    }

    @Override
    public List<Game> findAllGames() {
        return repository.findAll();
    }

    @Override
    public void delete(Game game) {
    repository.delete(game);
    }
    @Override
    public List<Game> findGameWithTeam(Team team){
        return repository.findGameWithTeam(team);
    }
}
