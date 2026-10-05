package com.onevn.server.service;

import com.onevn.server.model.User;
import com.onevn.server.repository.AddResult;
import com.onevn.server.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AddResult register(
            String username,
            String email,
            String password) {

        if (username == null || username.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return AddResult.ERROR;
        }

        String passwordHash =
                BCrypt.hashpw(password, BCrypt.gensalt(12));

        User user = new User(0L, username, email, passwordHash);

        return userRepository.add(user);
    }

    public LoginResult login(String username, String password) {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return LoginResult.INVALID_CREDENTIALS;
        }

        User user = userRepository.findByUsername(username);

        if (user == null) {
            return LoginResult.INVALID_CREDENTIALS;
        }

        try {
            if (BCrypt.checkpw(password, user.getPasswordHash())) {
                return LoginResult.SUCCESS;
            }

            return LoginResult.INVALID_CREDENTIALS;

        } catch (IllegalArgumentException e) {
            return LoginResult.ERROR;
        }
    }

    public User findUser(String username) {

        return userRepository.findByUsername(username);
    }

    public void removeUser(String username) {

        userRepository.remove(username);
    }
}
