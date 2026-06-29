package com.app.service.impl;

import com.app.dto.request.LoginRequest;
import com.app.dto.request.RegisterRequest;
import com.app.dto.response.AuthResponse;
import com.app.entity.AppUser;
import com.app.entity.Role;
import com.app.entity.Siege;
import com.app.exception.BadRequestException;
import com.app.exception.ConflictException;
import com.app.exception.ResourceNotFoundException;
import com.app.repository.RoleRepository;
import com.app.repository.SiegeRepository;
import com.app.repository.UserRepository;
import com.app.security.jwt.CookieUtil;
import com.app.security.jwt.JwtService;
import com.app.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SiegeRepository siegeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CookieUtil cookieUtil;
    private final UserDetailsService userDetailsService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already in use: " + request.email());
        }

        Siege siege = siegeRepository.findById(request.siegeId())
                .orElseThrow(() -> new ResourceNotFoundException("Siege not found: " + request.siegeId()));

        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));

        AppUser user = AppUser.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .siege(siege)
                .roles(Set.of(defaultRole))
                .build();

        userRepository.save(user);

        issueTokens(user, response);

        return buildAuthResponse(user, "Registration successful");
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        AppUser user = (AppUser) userDetailsService.loadUserByUsername(request.username());

        issueTokens(user, response);

        return buildAuthResponse(user, "Login successful");
    }

    @Override
    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.extractTokenFromCookie(request, CookieUtil.REFRESH_TOKEN_COOKIE)
                .orElseThrow(() -> new BadRequestException("Refresh token not found"));

        if (!jwtService.isTokenValidForRefresh(refreshToken)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        // Blacklist old refresh token (rotation)
        jwtService.blacklistToken(refreshToken);

        String username = jwtService.extractUsername(refreshToken);
        AppUser user = (AppUser) userDetailsService.loadUserByUsername(username);

        issueTokens(user, response);

        return buildAuthResponse(user, "Token refreshed");
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // Blacklist access token
        cookieUtil.extractTokenFromCookie(request, CookieUtil.ACCESS_TOKEN_COOKIE)
                .ifPresent(jwtService::blacklistToken);

        // Blacklist refresh token
        cookieUtil.extractTokenFromCookie(request, CookieUtil.REFRESH_TOKEN_COOKIE)
                .ifPresent(jwtService::blacklistToken);

        cookieUtil.clearAuthCookies(response);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private void issueTokens(UserDetails user, HttpServletResponse response) {
        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);
    }

    private AuthResponse buildAuthResponse(AppUser user, String message) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        return new AuthResponse(user.getUsername(), user.getEmail(), roles, message);
    }
}
