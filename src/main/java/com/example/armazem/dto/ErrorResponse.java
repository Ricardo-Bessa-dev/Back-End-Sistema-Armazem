package com.example.armazem.dto;

// Formato único de erro da API: {"message": "..."}

public record ErrorResponse(String message) {}