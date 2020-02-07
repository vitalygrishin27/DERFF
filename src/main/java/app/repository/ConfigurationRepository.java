package app.repository;

import app.Models.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConfigurationRepository extends JpaRepository<Configuration,Long> {

    @Query("Select c.value from Configuration c where c.key = :key")
    String getValue(@Param("key") String key);

}
