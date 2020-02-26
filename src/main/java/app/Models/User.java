package app.Models;

import app.services.impl.TeamServiceImpl;
import app.services.impl.UserServiceImpl;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id", nullable = false, unique = true)
    private long id;

    @Column(name = "login", nullable = false, unique = true)
    private String login;

    @Column(name = "password", nullable = false)
    private String encryptedPassword;

    @Column(name = "role", nullable = false)
    private String role;

    @ManyToMany (fetch = FetchType.LAZY)
    @JoinTable (name="responsibility",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name="team_id"))
    @Fetch(FetchMode.SUBSELECT)
    private List<Team> responsibility = new ArrayList<>();

    @Transient
    private String password;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                '}';
    }

    public boolean hasResponsibility(Team team){
       return responsibility.contains(team);

    /*    if(username==null) return false;
        List<Team> responsibiblity=userService.findUserByLogin(username).getResponsibility();
        Team team=teamService.findTeamById(teamId);
        if(re)
        return false;*/
    }

}
