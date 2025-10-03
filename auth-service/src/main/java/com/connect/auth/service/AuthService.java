package com.akatsuki.auth.service;

import com.akatsuki.auth.common.exception.AuthCommonInvalidRefreshTokenException;
import com.akatsuki.auth.common.exception.AuthCommonInvalidTokenException;
import com.akatsuki.auth.common.exception.AuthCommonSignatureMismatchException;
import com.akatsuki.auth.common.exception.AuthCommonUnauthorizedException;
import com.akatsuki.auth.common.util.AsymmetricJwtUtil;
import com.akatsuki.auth.dto.AuthResponseDTO;
import com.akatsuki.auth.dto.LoginRequestDTO;
import com.akatsuki.auth.dto.RegisterRequestDTO;
import com.akatsuki.auth.enums.AuthProvider;
import com.akatsuki.auth.exception.*;
import com.akatsuki.auth.model.RefreshToken;
import com.akatsuki.auth.model.User;
import com.akatsuki.auth.repository.AuthRepository;
import com.akatsuki.auth.service.token.JwtGenerator;
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
    private final AsymmetricJwtUtil asymmetricJwtUtil;
    private final JwtGenerator jwtGenerator;
    private final AuthRepository authRepository;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO registerRequest) throws UserExistException, PasswordNotMatchException, AuthCommonSignatureMismatchException, AuthCommonInvalidTokenException {
        if (UserExists(registerRequest.getEmail())) {
            throw new UserExistException("User with this email already exists");
        }
        checkPasswordsMatch(registerRequest.getPassword(), registerRequest.getConfirmedPassword());
        User user = new User(registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()), AuthProvider.LOCAL);

        return createAuthResponse(userService.save(user));
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginRequestDTO) throws AuthCommonUnauthorizedException, AuthCommonSignatureMismatchException, AuthCommonInvalidTokenException {
        Optional<User> userOpt = userService.findByEmail(loginRequestDTO.getEmail())
                .filter(user -> passwordEncoder.matches(loginRequestDTO.getPassword(), user.getEncodedPassword()));

        if (userOpt.isEmpty()) {
            throw new AuthCommonUnauthorizedException("Invalid email or password");
        }

        User user = userOpt.get();

        return createAuthResponse(user);
    }

    public AuthResponseDTO refresh(String refreshToken) throws AuthCommonInvalidRefreshTokenException, AuthCommonSignatureMismatchException, AuthCommonInvalidTokenException {
        // Logic to handle token refresh
        // This would typically involve validating the refresh token
        // and generating a new authentication token.
        asymmetricJwtUtil.validateRefreshToken(refreshToken);
        User user = getUserByRefreshToken(refreshToken);

        return createAuthResponse(user);
    }

    public void logout(String userId) throws AuthCommonUnauthorizedException {
        authRepository.deleteByUser_Id(userService.getUserByUserId(UUID.fromString(userId)).orElseThrow(() -> new AuthCommonUnauthorizedException("No user with this userId")).getId());
    }

    @Transactional
    public void deleteUserByUserId(String userId) throws AuthCommonUnauthorizedException {
        Optional<User> userOpt = userService.getUserByUserId(UUID.fromString(userId));
        if (userOpt.isEmpty()) {
            throw new AuthCommonUnauthorizedException("No user with this userId");
        }
        User user = userOpt.get();
        authRepository.deleteByUser_Id(user.getId());
        userService.deleteByUserId(UUID.fromString(userId));
    }

    @Transactional
    private AuthResponseDTO createAuthResponse(User user) throws AuthCommonSignatureMismatchException, AuthCommonInvalidTokenException {
        String accessToken = jwtGenerator.generateAccessToken(user.getUserId());
        String refreshToken = jwtGenerator.generateRefreshToken(user.getUserId());

        authRepository.deleteByUser_Id(user.getId());
        storeRefreshToken(refreshToken, user);

        return new AuthResponseDTO(accessToken, refreshToken);
    }

    private void storeRefreshToken(String refreshToken, User user) throws AuthCommonSignatureMismatchException, AuthCommonInvalidTokenException {
        authRepository.save(new RefreshToken(refreshToken, user,
                asymmetricJwtUtil.getIssuedAt(refreshToken), asymmetricJwtUtil.getExpiration(refreshToken)));;
    }

    private User getUserByRefreshToken(String refreshToken) throws AuthCommonInvalidRefreshTokenException {
        return authRepository.findByToken(refreshToken)
                .map(RefreshToken::getUser).orElseThrow( () -> new AuthCommonInvalidRefreshTokenException("Invalid Refresh token"));
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
