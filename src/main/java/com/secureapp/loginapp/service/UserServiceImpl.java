package com.secureapp.loginapp.service;

import com.secureapp.loginapp.model.User;
import com.secureapp.loginapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    public UserServiceImpl(UserRepository userRepository) { this.userRepository = userRepository; }

    @Override
    public User save(User user) { return userRepository.save(user); }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean emailExists(String email) { return userRepository.existsByEmail(email); }

    @Override
    public boolean usernameExists(String username) { return userRepository.existsByUsername(username); }

    @Override
    public void updateLastLoginIp(String username, String ip) {
        userRepository.findByUsername(username).ifPresent(u -> {
            u.setLastLoginIp(ip);
            userRepository.save(u);
        });
    }
}
