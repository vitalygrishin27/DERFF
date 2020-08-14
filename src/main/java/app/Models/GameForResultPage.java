package app.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameForResultPage {
    private long id;
    private Date date;
    private String stringDate;
    private String masterTeamName;
    private String slaveTeamName;
    private String masterTeamSymbolString;
    private String slaveTeamSymbolString;
    private Integer masterGoalsCount;
    private Integer slaveGoalsCount;
    private Integer masterYellowCardsCount;
    private Integer slaveYellowCardsCount;
    private Integer masterRedCardsCount;
    private Integer slaveRedCardsCount;
    private boolean technicalMasterTeamWin;
    private boolean technicalSlaveTeamWin;
    private boolean isResultSave;
    private List<Long> idsMasterPlayersGoals;
    private List<Long> idsSlavePlayersGoals;
    private List<Long> idsMasterPlayersYellowCards;
    private List<Long> idsSlavePlayersYellowCards;
    private List<Long> idsMasterPlayersRedCards;
    private List<Long> idsSlavePlayersRedCards;
}
