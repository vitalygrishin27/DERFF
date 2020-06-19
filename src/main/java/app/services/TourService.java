package app.services;

import app.Models.Tour;
import java.util.List;

public interface TourService {
    void save(Tour tour);

    void delete(Tour tour);

    List<Tour> findAll();

    Tour findByTourName(String tourName);
}

