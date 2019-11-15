package app.services;


import app.Models.Team;

import java.util.List;

public interface TeamService {
    void save(Team team);

    Team findTeamById(long id);

    Team findTeamByName(String teamName);

    List<Team> findAllTeams();
}
