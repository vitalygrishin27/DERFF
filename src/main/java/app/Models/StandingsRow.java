package app.Models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StandingsRow {

    private int number;
    private String teamName;
    private int games;
    private int wins;
    private int draws;
    private int losses;
    private int scoredGoals;
    private int concededGoals;
    private int ratioGoals;
    private int points;
}
