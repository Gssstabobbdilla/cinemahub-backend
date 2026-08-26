package com.cinemahub.cinemahub.security.dto;

import java.util.List;

public record AuthResponse(
        String token, Long userId, String firstName, String lastName, String email, List<String> roles) {
}