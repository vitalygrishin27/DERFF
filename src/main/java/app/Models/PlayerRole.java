package app.Models;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@AllArgsConstructor
public enum PlayerRole {
    UNDEFINED("UNDEFINED"),
    GOALKEEPER("GOALKEEPER"),
    DEFENDER("DEFENDER"),
    MIDFIELDER("MIDFIELDER"),
    FORWARD("FORWARD");

    private String role;

    public String getRole() {
        return role;
    }
}

