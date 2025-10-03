package com.akatsuki.auth.service;

import com.akatsuki.auth.model.User;
import com.akatsuki.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Test
    void save_ShouldReturnSavedUser() {
        User user = mock(User.class);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.save(user);

        assertEquals(user, result);
        verify(userRepository).save(user);
    }

    @Test
    void findByEmail_ShouldReturnUserOptional() {
        String email = "test@example.com";
        User user = mock(User.class);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void deleteByUserId_ShouldCallRepositoryDeleteByUserId() {
        UUID userId = UUID.randomUUID();

        userService.deleteByUserId(userId);

        verify(userRepository).deleteByUserId(userId);
    }

    @Test
    void getUserByUserId_ShouldReturnUserOptional() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(userRepository.getUserByUserId(userId)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByUserId(userId);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository).getUserByUserId(userId);
    }
}