package app.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "season")
public class Season {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id", nullable = false, unique = true)
    private long id;

    @Column(name = "year",unique = true)
    private Integer year;

    @OneToMany(mappedBy = "season", fetch = FetchType.LAZY)
    private Collection<Team> teams;

    @OneToMany(mappedBy = "season", fetch = FetchType.LAZY)
    private Collection<Player> players;

    @Override
    public String toString() {
        return "Season{" +
                "id=" + id +
                ", year='" + year + '\'' +
                '}';
    }
}
