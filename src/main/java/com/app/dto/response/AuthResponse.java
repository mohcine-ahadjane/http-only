package com.app.dto.response;

import java.util.Set;

public record AuthResponse(
        String username,
        String email,
        Set<String> roles,
        String message
) {}
