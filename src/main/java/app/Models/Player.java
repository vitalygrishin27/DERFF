package app.Models;

import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "player")
public class Player {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id",nullable = false,unique = true)
    private long id;

    @Column(name = "first_name",nullable = false)
    private String firstName;

    @Column(name = "second_name")
    private String secondName;

    @Column(name = "last_name",nullable = false)
    private String lastName;

    @Column(name = "birthday")
    private Date birthday;

    @Column(name = "id_card", unique = true)
    private Integer idCard;

    @Column(name = "is_legionary")
    private Boolean isLegionary;

    @Column(name = "registration", nullable = false)
    private String registration;

    @Lob
    @Column(name="photo")
    private byte[] photo;

    @ManyToOne (optional = false)
    @JoinColumn(name = "id_team")
    private Team team;

    @OneToMany(mappedBy = "player", fetch = FetchType.LAZY)
    private Collection<Goal> goals;

    @OneToMany(mappedBy = "player", fetch = FetchType.LAZY)
    private Collection<Offense> offenses;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "competition_player",
            joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "competition_id"))
    @Fetch(FetchMode.SUBSELECT)
    private List<Competition> competitions = new ArrayList<>();

}
