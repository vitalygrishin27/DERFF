package app.Models;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum UserRole {
    ADMINISTRATOR("ADMINISTRATOR"),
    MANAGER("MANAGER"),
    GUEST("GUEST");

    private String role;

    public String getRole() {
        return role;
    }
}
