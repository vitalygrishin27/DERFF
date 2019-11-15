package app.repository;

import app.Models.Competition;
import app.Models.Player;
import app.Models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player,Long> {

    @Query("Select p from Player p, IN (p.competitions) comp where comp in(:competition) and p.team =:team")
    List<Player> findAllPlayersInTeamForCurrentCompetition(@Param("competition")Competition competition, @Param("team") Team team);
}
