package app.repository;

import app.Models.Competition;
import app.Models.Player;
import app.Models.Region;
import app.Models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegionRepository extends JpaRepository<Region,Long> {
    @Query("Select r from Region r where r.name = :name")
    Region findRegionByName(@Param("name") String name);
}
