package app.services.impl;

import app.Models.Competition;
import app.repository.CompetitionRepository;
import app.services.CompetitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompetitionServiceImpl implements CompetitionService {

    @Autowired
    CompetitionRepository repository;

    @Override
    public void save(Competition competition) {
        repository.saveAndFlush(competition);
    }

    @Override
    public Competition findCompetitionById(long id) {
        return repository.getOne(id);
    }

    @Override
    public List<Competition> findAllCompetition() {
        return repository.findAll();
    }

    @Override
    public void delete(Competition competition) {
        repository.delete(competition);
    }

    @Override
    public Competition findCompetitionByName(String name) {
        return repository.findCompetitionByName(name);
    }
}
