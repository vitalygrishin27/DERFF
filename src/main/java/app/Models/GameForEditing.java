package app.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameForEditing {
    private long masterTeamId;
    private long slaveTeamId;
    private String masterTeamName;
    private String slaveTeamName;
    private boolean isResultSave;
}
