package app.repository;

import app.Models.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SettingsRepository extends JpaRepository<Settings, Long> {

  //  @Query("Select g from Game g order by g.date")
 //   List<Game> findAllGames();

    @Query("Select s from Settings s where s.key =:key")
    Settings findSettingsByKey(@Param("key") String key);
}
