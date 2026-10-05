package com.onevn.server.repository;

import com.onevn.server.model.User;

public interface UserRepository {

    AddResult add(User user);

    boolean exists(String username);

    User findByUsername(String username);

    void remove(String username);
}
