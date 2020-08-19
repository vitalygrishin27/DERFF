package app.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "tour")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id", nullable = false, unique = true)
    private long id;

    @Column(name = "name", nullable = false, unique = true)
    private String tourName;

    @Column(name = "date")
    private Date date;

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY)
    private List<Game> games;

    @JsonIgnore
    @ManyToOne (optional = false)
    @JoinColumn(name = "id_competition")
    private Competition competition;

    @Override
    public String toString() {
        return "Tour{" +
                "id=" + id +
                ", tourName='" + tourName + '\'' +
                ", date=" + date +
                '}';
    }
}
