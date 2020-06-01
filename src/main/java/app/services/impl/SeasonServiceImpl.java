package app.services.impl;


import app.Models.Season;
import app.Models.Team;
import app.repository.SeasonRepository;
import app.repository.TeamRepository;
import app.services.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeasonServiceImpl implements SeasonService {
    @Autowired
    SeasonRepository repository;


    @Override
    public void save(Season season) {
        repository.saveAndFlush(season);
    }

    @Override
    public Season findByYear(int year) {
        return repository.findByYear(year);
    }

    @Override
    public void delete(Season season) {
        repository.delete(season);
    }
}
