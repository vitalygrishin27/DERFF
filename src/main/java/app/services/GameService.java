package app.services;


import app.Models.Competition;
import app.Models.Game;
import app.Models.Team;

import java.util.Date;
import java.util.List;

public interface GameService {
    void save(Game game);

    Game findGameById(long id);

    List<Game> findAllGames();

    List<Game> findAllGamesByCompetition(Competition competition);

    void delete(Game game);

    List<Game> findGameWithTeam(Team team);

    List<Game> findGamesByDate(Date date);

    List<Game> findGamesByDateAndCompetition(Date date, Competition competition);

    List<Game> findGamesBetweenDates(Date from, Date to);

    List<Game> findGamesBetweenDatesAndCompetition(Date from, Date to, Competition competition);

    List<Game> findGamesWithResultByTeam(Team team, boolean isResultSave);

    List<Game> findGamesWithResultByTeamAndCompetition(Team team, Competition competition, boolean isResultSave);

    void update(Game game);

}

