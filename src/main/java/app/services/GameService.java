package app.services;


import app.Models.Game;
import app.Models.Team;

import java.util.Date;
import java.util.List;

public interface GameService {
    void save(Game game);

    Game findGameById(long id);

    List<Game> findAllGames();

    void delete(Game game);

    List<Game> findGameWithTeam(Team team);

    List<Game> findGamesByDate(Date date);

    List<Game> findGamesBetweenDates(Date from, Date to);


}

