package app.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "db_log")
public class DBLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id", nullable = false, unique = true)
    private long id;

    @Column(name = "userName", nullable = false)
    private String userName;

    @Column(name = "localDate", nullable = false)
    private LocalDate localDate;

    @Column(name = "operation", nullable = false)
    private String operation;

    @Column(name = "description", nullable = false)
    private String description;

}
