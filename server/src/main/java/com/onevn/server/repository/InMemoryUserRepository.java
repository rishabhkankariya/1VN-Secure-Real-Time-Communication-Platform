package com.onevn.server.repository;

import com.onevn.server.model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> users =
            new ConcurrentHashMap<>();

    private final AtomicLong nextId = new AtomicLong();

    @Override
    public AddResult add(User user) {

        if (user.getId() == 0) {
            user = new User(
                    nextId.incrementAndGet(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPasswordHash()
            );
        }

        return users.putIfAbsent(
                user.getUsername(),
                user
        ) == null ? AddResult.SUCCESS : AddResult.DUPLICATE;
    }

    @Override
    public boolean exists(String username) {

        return users.containsKey(username);
    }

    @Override
    public User findByUsername(String username) {

        return users.get(username);
    }

    @Override
    public void remove(String username) {

        users.remove(username);
    }
}
