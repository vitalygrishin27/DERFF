package app.services;


import app.Models.Competition;

import java.util.List;

public interface CompetitionService {
    void save(Competition competition);

    Competition findCompetitionByName(String competitionName);

    Competition findCompetitionById(Long id);

    List<Competition> findAllCompetitions();

    void deleteById(long id);
}
