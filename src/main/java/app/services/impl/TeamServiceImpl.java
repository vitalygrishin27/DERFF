package app.services.impl;


import app.Models.Team;
import app.repository.TeamRepository;
import app.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl implements TeamService {
    @Autowired
    TeamRepository repository;

    @Override
    public void save(Team team) {
        repository.saveAndFlush(team);
    }

    @Override
    public Team findTeamById(long id) {
        return repository.findTeamById(id);
    }

    @Override
    public Team findTeamByName(String teamName) {
        return repository.findTeamByName(teamName);
    }

    @Override
    public List<Team> findAllTeams() {
        return repository.findAll().stream().filter(team -> !team.getTeamName().equals("AUTOGOAL")).collect(Collectors.toList());
    }


    @Override
    public void delete(Team team){
        repository.delete(team);
    }
/*    @Override
    public List<Team> findAllTeamsInCompetition(Competition competition) {
        return repository.findAllTeamsInCompetition(competition);
    }

    @Override
    public List<Team> findAllTeamsByRegion(Region region) {
        return repository.findAllTeamsByRegion(region);
    }*/
}
