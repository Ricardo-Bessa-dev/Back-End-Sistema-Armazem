package com.example.armazem.dto;

import jakarta.validation.constraints.NotBlank;

// DTO input do login (POST /auth/login)

public record LoginRequest(
        @NotBlank
        String login,

        @NotBlank
        String password
){}