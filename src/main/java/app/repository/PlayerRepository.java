package app.repository;

import app.Models.Player;
import app.Models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface PlayerRepository extends JpaRepository<Player,Long> {

    @Query("Select p from Player p where p.team =:team")
    List<Player> findAllPlayersInTeam(@Param("team") Team team);

    @Query("Select p from Player p where p.team =:team and p.isNotActive =:isNotActive")
    List<Player> findAllActivePlayersInTeam(@Param("team") Team team,@Param("isNotActive") Boolean isNotActive);

    @Query("Select p from Player p where p.idCard =:idCard")
    Player findByIdCard(@Param("idCard") int idCard);

    @Transactional
    @Modifying
    @Query("UPDATE Player p SET p.lastName = ?2, p.firstName = ?3, p.secondName = ?4, p.birthday = ?5, p.stringBirthday = ?6, p.idCard = ?7, p.isLegionary = ?8, p.registration = ?9,  p.photo = ?10,  p.team = ?11, p.inn = ?12, p.role = ?13 WHERE p.id = ?1")
    void update(Long id, String lastName, String firstName, String secondName, Date birthday, String stringBirthday, Integer idCard,Boolean isLegionary,String registration,  byte[] photo,Team team, String inn, String role);
   // void update(@Param("player") Player player);


}
