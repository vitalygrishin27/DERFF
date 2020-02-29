package app.services.impl;

import app.Models.Goal;
import app.repository.GoalRepository;
import app.services.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoalServiceImpl implements GoalService {
    @Autowired
    GoalRepository repository;

    @Override
    public void save(Goal goal) {
        repository.saveAndFlush(goal);

    }

    @Override
    public void delete(Goal goal) {
        repository.delete(goal);
    }

    @Override
    public List<Goal> findAll() {
        return repository.findAll();
    }

}
