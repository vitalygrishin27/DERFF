package app.services;

import app.Models.Season;
import app.Models.Team;

import java.util.List;

public interface SeasonService {
    void save(Season season);

    Season findByYear(int year);

   // List<Team> findAllTeams();

    void delete(Season season);

  // List<Team> findBySeason(Season season);

   // List<Team> findAllTeamsByRegion(Region region);*/
}
