package app.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "game")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id", nullable = false, unique = true)
    private long id;


    @Column(name = "date")
    private Date date;

    @Column(name="string_date")
    private String stringDate;

    @OneToMany(mappedBy = "game", fetch = FetchType.LAZY)
    private ArrayList<Goal> goals;

   /* @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "game_team",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "team_id"))
    @Fetch(FetchMode.SUBSELECT)
    private List<Team> teams = new ArrayList<>();*/

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_team_master")
    private Team masterTeam;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_team_slave")
    private Team slaveTeam;

    private String masterGoalsCount;

    private String slaveGoalsCount;

    public void addGoal(Goal goal){
        goals.add(goal);
    }
  /*  public String getStringDate() {
        SimpleDateFormat dateFormat=new SimpleDateFormat("dd.MM");
        return dateFormat.format(this.getDate());
    }*/
}
