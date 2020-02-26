package app.services.impl;

import app.Models.User;
import app.repository.UserRepository;
import app.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository repository;

    @Override
    public User findUserByLogin(String login) {
        return repository.findUserByLogin(login);
    }
}
