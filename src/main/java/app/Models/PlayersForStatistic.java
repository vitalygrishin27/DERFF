package app.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PlayersForStatistic {

    private String playerName;
    private String photoString;
    private String teamName;
    private String symbolString;
    private Integer value;
    private String details;
    private String stringDate;
}
