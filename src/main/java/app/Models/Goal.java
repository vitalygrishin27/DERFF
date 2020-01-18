package app.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "goal")
public class Goal {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id",nullable = false,unique = true)
    private long id;

    @ManyToOne (optional = false)
    @JoinColumn(name = "id_team")
    private Team team;

    @ManyToOne (optional = false)
    @JoinColumn(name = "id_player")
    private Player player;

  /*  @ManyToOne (optional = false)
    @JoinColumn(name = "id_game")
    private Game game;
*/

}
