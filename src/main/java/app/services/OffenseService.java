package app.services;


import app.Models.Offense;

import java.util.List;

public interface OffenseService {
    void save(Offense offense);

    void delete(Offense offense);

    List<Offense> getAllYellowCards();
}

