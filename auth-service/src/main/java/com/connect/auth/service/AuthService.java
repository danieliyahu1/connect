package com.connect.auth.service;

import com.connect.auth.dto.AuthResponseDTO;
import com.connect.auth.dto.LoginRequestDTO;
import com.connect.auth.dto.RegisterRequestDTO;
import com.connect.auth.enums.AuthProvider;
import com.connect.auth.exception.*;
import com.connect.auth.model.RefreshToken;
import com.connect.auth.model.User;
import com.connect.auth.repository.AuthRepository;
import com.connect.auth.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthRepository authRepository;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO registerRequest) throws UserExistException, PasswordNotMatchException {
        // Logic to handle user registration
        // This would typically involve saving the user details to a database
        // and generating an authentication token.
        if (UserExists(registerRequest.getEmail())) {
            throw new UserExistException("User with this email already exists");
        }
        checkPasswordsMatch(registerRequest.getPassword(), registerRequest.getConfirmedPassword());
        User user = new User(registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()), AuthProvider.LOCAL);

        return createAuthResponse(userService.save(user));
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginRequestDTO) throws UnauthorizedException {
        Optional<User> userOpt = userService.findByEmail(loginRequestDTO.getEmail())
                .filter(user -> passwordEncoder.matches(loginRequestDTO.getPassword(), user.getEncodedPassword()));

        if (userOpt.isEmpty()) {
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userOpt.get();

        return createAuthResponse(user);
    }

    public AuthResponseDTO refresh(String refreshToken) throws InvalidRefreshTokenException {
        // Logic to handle token refresh
        // This would typically involve validating the refresh token
        // and generating a new authentication token.
        jwtUtil.validateRefreshToken(refreshToken);
        User user = getUserByRefreshToken(refreshToken);

        return createAuthResponse(user);
    }

    public void logout(String accessToken) throws UserNotFoundException, InvalidAccessTokenException {
        // Logic to handle user logout
        // This would typically involve invalidating the user's session or token.
        UUID userId = jwtUtil.getUserIdFromAccessToken(accessToken);
        authRepository.deleteByUser_Id(userService.getUserByUserId(userId).orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId)).getId());
    }

    @Transactional
    public void deleteUserByUserId(String userId) {
        // Logic to delete a user by ID
        // This would typically involve removing the user from the database.
        Optional<User> userOpt = userService.getUserByUserId(UUID.fromString(userId));
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();
        authRepository.deleteByUser_Id(user.getId());
        userService.deleteByUserId(UUID.fromString(userId));
    }

    @Transactional
    private AuthResponseDTO createAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getUserId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        authRepository.deleteByUser_Id(user.getId());
        storeRefreshToken(refreshToken, user);

        return new AuthResponseDTO(accessToken, refreshToken);
    }

    private void storeRefreshToken(String refreshToken, User user) {
        authRepository.save(new RefreshToken(refreshToken, user,
                jwtUtil.getIssuedAt(refreshToken), jwtUtil.getExpiration(refreshToken)));;
    }

    private User getUserByRefreshToken(String refreshToken) throws InvalidRefreshTokenException {
        return authRepository.findByToken(refreshToken)
                .map(RefreshToken::getUser).orElseThrow( () -> new InvalidRefreshTokenException("Invalid Refresh token"));
    }

    private boolean UserExists(String email) {
        return userService.findByEmail(email).isPresent();
    }

    private void checkPasswordsMatch(String password, String confirmedPassword) throws PasswordNotMatchException {
        if (!password.equals(confirmedPassword)) {
            throw new PasswordNotMatchException("Passwords do not match");
        }
    }
}
