package app.services.impl;

import app.Models.Competition;
import app.Models.Region;
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
    public Competition findCompetitionByName(String competitionName) {
        return repository.findCompetitionByName(competitionName);
    }

    @Override
    public Competition findCompetitionById(Long id) {
        return repository.getOne(id);
    }

    @Override
    public List<Competition> findAllCompetitions() {
        return  repository.findAll();
    }

    @Override
    public void deleteById(long id) {
        repository.deleteById(id);
    }

    @Override
    public List<Competition> findAllCompetitionsInRegion(Region region) {
        return repository.findAllCompetitionsInRegion(region);
    }
}
