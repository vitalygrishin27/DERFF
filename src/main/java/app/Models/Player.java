package app.Models;

import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "player")
public class Player implements Comparable{
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

    @Column(name = "stringBirthday")
    private String stringBirthday;

    @Column(name = "id_card", unique = true)
    private Integer idCard;

    @Column(name = "is_legionary")
    private Boolean isLegionary;

    @Column(name = "is_not_active")
    private Boolean isNotActive;

    @Column(name = "registration", nullable = false)
    private String registration;

    @Column(name = "inn")
    private String inn;

    @Column(name = "role")
    private String role;

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

  /*  @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "competition_player",
            joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "competition_id"))
    @Fetch(FetchMode.SUBSELECT)
    private List<Competition> competitions = new ArrayList<>();*/

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", secondName='" + secondName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        if (this.getLastName().startsWith("І")) this.setLastName(this.getLastName().replace("І", "И"));
        if (this.getLastName().startsWith("Є")) this.setLastName(this.getLastName().replace("Є", "Э"));
        int result = this.getLastName().compareTo(((Player) o).getLastName());
        if (this.getLastName().startsWith("И")) this.setLastName(this.getLastName().replace("И", "І"));
        if (this.getLastName().startsWith("Э")) this.setLastName(this.getLastName().replace("Э", "Є"));
        return result;
    }
}
