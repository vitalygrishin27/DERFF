package app.repository;

import app.Models.Goal;
import app.Models.Offense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface OffenseRepository extends JpaRepository<Offense, Long> {

    @Query("Select o from Offense o where o.type='YELLOW'")
    List<Offense> getAllYellowCards();

    /*
    @Query("Select g from Game g where g.masterTeam =:team or g.slaveTeam =:team")
    List<Game> findGameWithTeam(@Param("team") Team team);

    @Query("Select g from Game g where g.date =:date")
    List<Game> findGamesByDate(@Param("date") Date date);

    @Query("Select g from Game g where g.date >=:from and g.date <=:to")
    List<Game> findGamesBetweenDates(@Param("from") Date from, @Param("to") Date to);

    @Transactional
    @Modifying
    @Query("UPDATE Game g SET g.masterTeam = ?2, g.slaveTeam = ?3, g.date = ?4, g.stringDate = ?5 WHERE g.id = ?1")
    void update(Long id, Team masterTeam, Team slaveTeam, Date date, String stringDate);
*/
}
