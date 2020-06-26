package app.Models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import java.util.Comparator;
import java.util.LinkedList;

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
    private String stringRatioGoals;
    private LinkedList<String> gameResults = new LinkedList<>();

    public String getStringRatioGoals() {
        return scoredGoals+" : "+concededGoals;
    }

    public static final Comparator<StandingsRow> COMPARE_BY_POINTS = new Comparator<StandingsRow>() {
        @Override
        public int compare(StandingsRow first, StandingsRow second) {
            if (first.getPoints() == second.getPoints()) {
                if (first.getRatioGoals() == second.getRatioGoals()) {
                    if (first.getConcededGoals() > second.getConcededGoals()) {
                        return -1;
                    } else if (first.getConcededGoals() < second.getConcededGoals()) {
                        return 1;
                    } else {
                        return 0;
                    }
                } else if (first.getRatioGoals() > second.getRatioGoals()) {
                    return 1;
                } else {
                    return -1;
                }

            } else if (first.getPoints() > second.getPoints()) {
                return 1;
            } else {
                return -1;
            }
        }
    };

}

