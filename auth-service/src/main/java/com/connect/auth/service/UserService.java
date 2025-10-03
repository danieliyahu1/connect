package com.akatsuki.auth.service;


import com.akatsuki.auth.model.User;
import com.akatsuki.auth.repository.UserRepository;
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

    public void deleteByUserId(UUID userId) { userRepository.deleteByUserId(userId); }

    public Optional<User> getUserByUserId(UUID id) { return userRepository.getUserByUserId(id); }
}
