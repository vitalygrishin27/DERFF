package app.repository;

import app.Models.Competition;
import app.Models.Region;
import app.Models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team,Long> {

    @Query("Select t from Team t where t.teamName = :teamName")
    Team findTeamByName(@Param("teamName") String teamName);

/*    @Query("Select t from Team t where t.competition =:competition")
    List<Team> findAllTeamsInCompetition(@Param("competition") Competition competition);

    @Query("Select t from Team t where t.region =:region")
    List<Team> findAllTeamsByRegion(@Param("region") Region region);*/


}
