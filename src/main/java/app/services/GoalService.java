package app.services;

import app.Models.Goal;

import java.util.List;

public interface GoalService {
    void save(Goal goal);

    void delete(Goal goal);

    List<Goal> findAll();
}

