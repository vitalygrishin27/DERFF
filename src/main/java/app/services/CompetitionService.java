package app.services;


import app.Models.Competition;
import app.Models.Region;

import java.util.List;

public interface CompetitionService {
    void save(Competition competition);

    Competition findCompetitionByName(String competitionName);

    List<Competition> findAllCompetitionsInRegion(Region region);

    Competition findCompetitionById(Long id);

    List<Competition> findAllCompetitions();

    void deleteById(long id);


}
