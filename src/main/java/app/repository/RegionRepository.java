package app.repository;

import app.Models.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegionRepository extends JpaRepository<Region,Long> {
    @Query("Select r from Region r where r.name = :name")
    Region findRegionByName(@Param("name") String name);
}
