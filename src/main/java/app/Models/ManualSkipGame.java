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
@Table(name = "manual_skip_games")
public class ManualSkipGame {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id",nullable = false,unique = true)
    private long id;

    @ManyToOne (optional = false)
    @JoinColumn(name = "id_player")
    private Player player;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name="string_start_date")
    private String stringStartDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name="string_end_date")
    private String stringEndDate;

    @Column(name="description")
    private String description;
}
