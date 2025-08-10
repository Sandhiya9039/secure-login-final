package com.secureapp.loginapp.service;

import com.secureapp.loginapp.model.User;
import java.util.Optional;

public interface UserService {
    User save(User user);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean emailExists(String email);
    boolean usernameExists(String username);
    void updateLastLoginIp(String username, String ip);
}
