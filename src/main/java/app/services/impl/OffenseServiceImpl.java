package app.services.impl;

import app.Models.Goal;
import app.Models.Offense;
import app.repository.OffenseRepository;
import app.services.OffenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OffenseServiceImpl implements OffenseService {
    @Autowired
    OffenseRepository repository;

    @Override
    public void save(Offense offense) {
        repository.saveAndFlush(offense);

    }

    @Override
    public void delete(Offense offense) {
        repository.delete(offense);
    }

    @Override
    public List<Offense> getAllYellowCards() {
        return repository.getAllYellowCards();
    }

}
