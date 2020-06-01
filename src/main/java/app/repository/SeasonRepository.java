package app.repository;


import app.Models.Season;
import app.Models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeasonRepository extends JpaRepository<Season,Long> {

    Season findByYear(Integer year);
 /*   @Query("Select t from Team t where t.teamName = :teamName")
    Team findTeamByName(@Param("teamName") String teamName);

    @Query("Select t from Team t where t.id = :id")
    Team findTeamById(@Param("id") Long id);*/

 /*  @Query("Select t from Team t where t.season =:season")
    List<Team> findBySeason(@Param("season") Season season);
/*
    @Query("Select t from Team t where t.region =:region")
    List<Team> findAllTeamsByRegion(@Param("region") Region region);*/


}
