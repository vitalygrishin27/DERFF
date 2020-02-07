package app.Models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "referee")
public class Referee {
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
    private int idCard;

    @Lob
    @Column(name="photo")
    private byte[] photo;

    @ManyToMany (fetch = FetchType.LAZY)
    @JoinTable (name="referee_game",
            joinColumns = @JoinColumn(name = "referee_id"),
            inverseJoinColumns = @JoinColumn(name="game_id"))
    @Fetch(FetchMode.SUBSELECT)
    private List<Game> games = new ArrayList<>();

}
