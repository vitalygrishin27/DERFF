package app.services;


import app.Models.Competition;
import java.util.List;

public interface CompetitionService {
    void save(Competition competition);

    Competition findCompetitionById(long id);

    List<Competition> findAllCompetition();

    void delete(Competition competition);

    Competition findCompetitionByName(String name);

}

