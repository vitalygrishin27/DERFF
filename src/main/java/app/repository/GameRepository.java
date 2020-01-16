package app.repository;

import app.Models.Game;
import app.Models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface GameRepository extends JpaRepository<Game,Long> {

 @Query("Select g from Game g where g.masterTeam =:team or g.slaveTeam =:team")
    List<Game> findGameWithTeam(@Param("team") Team team);

 @Query("Select g from Game g where g.date =:date")
    List<Game> findGamesByDate(@Param("date") Date date);
}
