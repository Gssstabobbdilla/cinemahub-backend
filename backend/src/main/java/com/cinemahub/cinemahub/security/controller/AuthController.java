package com.cinemahub.cinemahub.security.controller;

import jakarta.validation.Valid;
import com.cinemahub.cinemahub.security.dto.AuthResponse;
import com.cinemahub.cinemahub.security.dto.LoginRequest;
import com.cinemahub.cinemahub.security.dto.RegisterUserRequest;
import com.cinemahub.cinemahub.security.entity.User;
import com.cinemahub.cinemahub.security.service.AuthService;
import com.cinemahub.cinemahub.security.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;


@RestController
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/api/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password());
    }

    // Registro + login automático: el frontend arranca la sesión con una sola llamada.
    @PostMapping("/api/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterUserRequest request) {
        User user = userService.register(request.firstName(), request.lastName(), request.email(), request.password());
        return authService.buildResponse(user);
    }

    private ResponseEntity<Map<String, Object>> build(
        HttpStatus status,
        String message) {

            return ResponseEntity
                    .status(status)
                    .body(Map.of("message", message));
        }


    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            org.springframework.security.authentication.BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
}
}