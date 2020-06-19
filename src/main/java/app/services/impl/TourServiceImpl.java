package app.services.impl;

import app.Models.Tour;
import app.repository.TourRepository;
import app.services.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TourServiceImpl implements TourService {
    @Autowired
    TourRepository repository;

    @Override
    public void save(Tour tour) {
        repository.saveAndFlush(tour);
    }

    @Override
    public void delete(Tour tour) {
        repository.delete(tour);
    }

    @Override
    public List<Tour> findAll() {
        return repository.findAll();
    }

    @Override
    public Tour findByTourName(String tourName) {
        return repository.findByTourName(tourName);
    }

}
