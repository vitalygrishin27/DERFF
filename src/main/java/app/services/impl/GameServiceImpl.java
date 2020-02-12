package app.services.impl;

import app.Models.Competition;
import app.Models.Game;
import app.Models.Team;
import app.repository.GameRepository;
import app.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
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
    public List<Game> findAllGamesByCompetition(Competition competition) {
        return repository.findAllGamesByCompetition(competition);
    }

    @Override
    public List<Game> findGamesByDateAndCompetition(Date date, Competition competition) {
        return repository.findGamesByDateAndCompetition(date, competition);
    }

    @Override
    public List<Game> findGamesBetweenDatesAndCompetition(Date from, Date to, Competition competition) {
        return repository.findGamesBetweenDatesAndCompetition(from, to, competition);
    }

    @Override
    public void delete(Game game) {
        repository.delete(game);
    }

    @Override
    public List<Game> findGameWithTeam(Team team) {
        return repository.findGameWithTeam(team);
    }

    @Override
    public List<Game> findGamesByDate(Date date) {
        return repository.findGamesByDate(date);
    }

    @Override
    public List<Game> findGamesBetweenDates(Date from, Date to) {
        return repository.findGamesBetweenDates(from, to);
    }

    @Override
    public void update(Game game) {
        repository.update(game.getId(), game.getMasterTeam(), game.getSlaveTeam(), game.getDate(), game.getStringDate());
    }

    @Override
    public List<Game> findGamesWithResultByTeam(Team team, boolean isResultSave) {
        return repository.findGamesWithResultByTeam(team, isResultSave);
    }
}
