package app.repository;

import app.Models.Competition;
import app.Models.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompetitionRepository extends JpaRepository<Competition,Long> {
    @Query("Select s from Competition s where s.name = :name")
    Competition findCompetitionByName(@Param("name") String competitionName);

    @Query("Select c from Competition c where c.region =:region")
    List<Competition> findAllCompetitionsInRegion(@Param("region") Region region);

}
