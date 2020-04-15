package app.services.impl;

import app.Models.Game;
import app.Models.ManualSkipGame;
import app.Models.Player;
import app.repository.ManualSkipGameRepository;
import app.services.ManualSkipGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ManualSkipGameServiceImpl implements ManualSkipGameService {
    @Autowired
    ManualSkipGameRepository repository;

    @Override
    public void save(ManualSkipGame manualSkipGame) {
        repository.saveAndFlush(manualSkipGame);
    }

    @Override
    public List<Player> findPlayersWhichManualSkipGame(Game game) {
        Date date=game.getDate();
        List<Player> pl=repository.findPlayersWhichManualSkipGame(game.getDate());
        return pl;
    }

    @Override
    public List<ManualSkipGame> findByGame(Game game) {
        return repository.findByDate(game.getDate());
    }
}
