package app.repository;

import app.Models.Tour;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourRepository extends JpaRepository<Tour, Long> {

    Tour findByTourName(String tourName);
}
