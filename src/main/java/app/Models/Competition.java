package app.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "competition")
public class Competition {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id",nullable = false,unique = true)
    private long id;

    @Column(name = "name",nullable = false,unique = true)
    private String name;

 /*   @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "competition_team",
            joinColumns = @JoinColumn(name = "competition_id"),
            inverseJoinColumns = @JoinColumn(name = "team_id"))
    @Fetch(FetchMode.SUBSELECT)
    private List<Team> teams = new ArrayList<>();*/

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "competition_player",
            joinColumns = @JoinColumn(name = "competition_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id"))
    @Fetch(FetchMode.SUBSELECT)
    private List<Player> players = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "competition_referee",
            joinColumns = @JoinColumn(name = "competition_id"),
            inverseJoinColumns = @JoinColumn(name = "referee_id"))
    @Fetch(FetchMode.SUBSELECT)
    private List<Referee>referees = new ArrayList<>();

  /*     @OneToMany(mappedBy = "competition", fetch = FetchType.LAZY)
    private Collection<Game> games;
*/
    @ManyToOne (optional = false)
    @JoinColumn(name = "id_region")
    private Region region;

    @Override
    public String toString() {
        return "Competition{" +
                "name='" + name + '\'' +
                '}';
    }
}
