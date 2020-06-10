package app.Models;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthenticatedUser {
    private String userName;
    private String token;
    private List<Integer> teamsIds = new ArrayList<>();
    private String role;
}

