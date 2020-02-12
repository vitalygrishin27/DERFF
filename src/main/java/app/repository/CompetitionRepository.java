package app.repository;

import app.Models.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompetitionRepository extends JpaRepository<Competition,Long> {
    @Query("Select c from Competition c where c.name = :name")
    Competition findCompetitionByName(@Param("name") String name);

}

