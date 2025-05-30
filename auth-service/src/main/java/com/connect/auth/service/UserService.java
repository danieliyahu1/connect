package com.connect.auth.service;


import com.connect.auth.model.User;
import com.connect.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User save(User user) {
        return userRepository.save(user);
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() { return userRepository.findAll(); }

    public void deleteUserById(String id) {
        userRepository.deleteById(UUID.fromString(id));
    }

    public void deleteByUserId(UUID userId) {
        userRepository.deleteByUserId(userId);
    }

    public User getUserByUserId(UUID id) {
        return userRepository.getUserByUserId(id);
    }
}
