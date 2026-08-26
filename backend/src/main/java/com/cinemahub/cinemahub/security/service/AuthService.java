package com.cinemahub.cinemahub.security.service;

import com.cinemahub.cinemahub.security.dto.AuthResponse;
import com.cinemahub.cinemahub.security.entity.User;
import com.cinemahub.cinemahub.security.entity.UserRole;
import com.cinemahub.cinemahub.security.jwt.JwtService;
import com.cinemahub.cinemahub.security.repository.UserRepository;
import com.cinemahub.cinemahub.security.repository.UserRoleRepository;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, UserRoleRepository userRoleRepository,
                        PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Email o contraseña incorrectos");
        }

        return buildResponse(user);
    }

    public AuthResponse buildResponse(User user) {
        List<String> roles = userRoleRepository.findById_UserId(user.getId()).stream()
                .map(userRole -> userRole.getRole().getName())
                .toList();

        String token = jwtService.generateToken(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), roles);

        return new AuthResponse(token, user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), roles);
    }
}