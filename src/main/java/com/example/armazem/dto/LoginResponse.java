package com.example.armazem.dto;

// DTO resposta do login: o token que o front guarda e reenvia a cada requisicao

public record LoginResponse(String token) {}