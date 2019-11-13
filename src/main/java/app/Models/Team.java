package app.Models;


import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "team")
public class Team {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id",nullable = false,unique = true)
    private long id;

    @Column(name = "name",nullable = false,unique = true)
    private String teamName;

    @Column(name = "date")
    private Date date;

    @Column(name = "region")
    private String region;

    @Column(name = "boss")
    private String boss;

    @Column(name = "phone")
    private String phone;

    @Lob
    @Column(name="symbol")
    private byte[] symbol;

}
