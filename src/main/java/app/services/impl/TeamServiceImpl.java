package app.services.impl;

import app.Models.Team;
import app.repository.TeamRepository;
import app.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return repository.findById(id).get();
    }

    @Override
    public Team findTeamByName(String teamName) {
        return repository.findTeamByName(teamName);
    }

    @Override
    public List<Team> findAllTeams() {
        return repository.findAll();
    }
}
