package com.connect.auth.service;

import com.connect.auth.dto.AuthResponseDTO;
import com.connect.auth.dto.LoginRequestDTO;
import com.connect.auth.dto.RegisterRequestDTO;
import com.connect.auth.enums.AuthProvider;
import com.connect.auth.exception.RefreshTokenNotFoundException;
import com.connect.auth.exception.UnauthorizedException;
import com.connect.auth.exception.UserExistException;
import com.connect.auth.model.RefreshToken;
import com.connect.auth.model.User;
import com.connect.auth.repository.AuthRepository;
import com.connect.auth.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    public AuthResponseDTO register(RegisterRequestDTO registerRequest) throws UserExistException {
        // Logic to handle user registration
        // This would typically involve saving the user details to a database
        // and generating an authentication token.
        if (UserExists(registerRequest.getEmail())) {
            throw new UserExistException("User with this email already exists");
        }
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

    public AuthResponseDTO refresh(String refreshToken) throws RefreshTokenNotFoundException {
        // Logic to handle token refresh
        // This would typically involve validating the refresh token
        // and generating a new authentication token.
        jwtUtil.validateRefreshToken(refreshToken);
        User user = getUserByRefreshToken(refreshToken);

        return createAuthResponse(user);
    }

    public void logout(UUID userId) throws RefreshTokenNotFoundException {
        // Logic to handle user logout
        // This would typically involve invalidating the user's session or token.
        authRepository.deleteByUser_Id(userService.getUserByUserId(userId).getId());
    }

    @Transactional
    public void deleteUserByUserId(String userId) {
        // Logic to delete a user by ID
        // This would typically involve removing the user from the database.
        User user = userService.getUserByUserId(UUID.fromString(userId));
        if (user == null) {
            return;
        }
        authRepository.deleteByUser_Id(user.getId());
        userService.deleteByUserId(UUID.fromString(userId));
    }

    public List<User> getAllUsers() {
        // Logic to retrieve all users
        // This would typically involve fetching all user records from the database.
        return userService.getAllUsers();
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

    private User getUserByRefreshToken(String refreshToken) throws RefreshTokenNotFoundException {
        return authRepository.findByToken(refreshToken)
                .map(RefreshToken::getUser).orElseThrow( () -> new RefreshTokenNotFoundException("Invalid Refresh token"));
    }

    private boolean UserExists(String email) {
        return userService.findByEmail(email).isPresent();
    }

    public Map<UUID, String> getRefreshTokenMap() {
        return authRepository.findAll().stream()
                .collect(Collectors.toMap(
                        refreshToken -> refreshToken.getUser().getUserId(),
                        RefreshToken::getToken
                ));
    }
}
