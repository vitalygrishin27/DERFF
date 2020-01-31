package app.services;


import app.Models.Offense;

public interface OffenseService {
    void save(Offense offense);

    void delete(Offense offense);
}

