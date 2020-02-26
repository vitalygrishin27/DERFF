package app.services;

import app.Models.User;

public interface UserService {
    User findUserByLogin(String login);
}
