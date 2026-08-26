package com.cinemahub.cinemahub.security.service;

import com.cinemahub.cinemahub.security.dto.AuthResponse;
import com.cinemahub.cinemahub.security.entity.Role;
import com.cinemahub.cinemahub.security.entity.User;
import com.cinemahub.cinemahub.security.entity.UserRole;
import com.cinemahub.cinemahub.security.jwt.JwtService;
import com.cinemahub.cinemahub.security.repository.UserRepository;
import com.cinemahub.cinemahub.security.repository.UserRoleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.cinemahub.cinemahub.security.jwt.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, userRoleRepository, passwordEncoder, jwtService);
    }

    private User user() {
        User user = new User("Ana", "Test", "ana@cinemahub.local", "hashed-password");
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    @Test
    void loginReturnsTokenWhenCredentialsAreValid() {
        User user = user();
        Role role = new Role("ROLE_USER", "Usuario estándar");
        UserRole userRole = new UserRole(user, role);

        when(userRepository.findByEmail("ana@cinemahub.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("clave12345", "hashed-password")).thenReturn(true);
        when(userRoleRepository.findById_UserId(1L)).thenReturn(List.of(userRole));
        when(jwtService.generateToken(1L, "ana@cinemahub.local", "Ana", "Test", List.of("ROLE_USER")))
                .thenReturn("fake.jwt.token");

        AuthResponse response = authService.login("ana@cinemahub.local", "clave12345");

        assertThat(response.token()).isEqualTo("fake.jwt.token");
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.roles()).containsExactly("ROLE_USER");
    }

    @Test
    void loginThrowsBadCredentialsWhenUserDoesNotExist() {
        when(userRepository.findByEmail("nadie@cinemahub.local")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("nadie@cinemahub.local", "clave12345"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void loginThrowsBadCredentialsWhenPasswordDoesNotMatch() {
        User user = user();
        when(userRepository.findByEmail("ana@cinemahub.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("clave-mala", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("ana@cinemahub.local", "clave-mala"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void buildResponseIncludesAllRolesOfTheUser() {
        User user = user();
        Role userRole = new Role("ROLE_USER", null);
        Role adminRole = new Role("ROLE_ADMIN", null);

        when(userRoleRepository.findById_UserId(1L)).thenReturn(List.of(
                new UserRole(user, userRole), new UserRole(user, adminRole)));
        when(jwtService.generateToken(any(), any(), any(), any(), anyList())).thenReturn("fake.jwt.token");

        AuthResponse response = authService.buildResponse(user);

        assertThat(response.roles()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
}