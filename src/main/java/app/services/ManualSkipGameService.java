package app.services;

import app.Models.Game;
import app.Models.ManualSkipGame;
import app.Models.Player;

import java.util.List;

public interface ManualSkipGameService {
    void save(ManualSkipGame manualSkipGame);

    List<Player> findPlayersWhichManualSkipGame(Game game);

    List<ManualSkipGame> findByGame(Game game);

    List<ManualSkipGame> findAll();

    ManualSkipGame findById(long id);

    void delete(ManualSkipGame manualSkipGame);
}

