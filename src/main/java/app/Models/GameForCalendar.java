package app.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameForCalendar {
    private long id;
    private Date date;
    private String stringDate;
    private String masterTeamName;
    private String slaveTeamName;
    private String masterTeamSymbolString;
    private String slaveTeamSymbolString;
    private Integer masterGoalsCount;
    private Integer slaveGoalsCount;
    private boolean technicalMasterTeamWin;
    private boolean technicalSlaveTeamWin;
    private boolean isResultSave;
}
