package app.repository;

import app.Models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team,Long> {

    @Query("Select t from Team t where t.teamName = :teamName")
    Team findTeamByName(@Param("teamName") String teamName);
}
