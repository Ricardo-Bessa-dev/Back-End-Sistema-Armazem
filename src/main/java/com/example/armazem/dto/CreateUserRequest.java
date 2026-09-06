package com.example.armazem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//DTO input cadastro de usuário (POST /users)

public record CreateUserRequest(
        @NotBlank
        @Size(max = 120)
        String name,

        @NotBlank
        @Size(max = 60)

        @NotBlank
        String password //Senha antes de passar pelo BCrypt e virar hash
) {
}