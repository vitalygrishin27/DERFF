package app.repository;

import app.Models.Game;
import app.Models.ManualSkipGame;
import app.Models.Player;
import app.Models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface ManualSkipGameRepository extends JpaRepository<ManualSkipGame, Long> {

    @Query("Select player from ManualSkipGame m where :date between m.startDate and m.endDate")
    List<Player> findPlayersWhichManualSkipGame(@Param("date") Date date);

    @Query("Select m from ManualSkipGame m where :date between m.startDate and m.endDate")
    List<ManualSkipGame>findByDate(@Param("date") Date date);
}
