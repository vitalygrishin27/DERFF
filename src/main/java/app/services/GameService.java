package app.services;


import app.Models.Game;

import java.util.List;

public interface GameService {
    void save(Game game);

    Game findGameById(long id);

    List<Game> findAllGames();

    void delete(Game game);

}
