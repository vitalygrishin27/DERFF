package app.services;


import app.Models.Team;

public interface TeamService {
    void save(Team team);

    Team getTeamById(long id);

    Team findTeamByName(String teamName);
}
