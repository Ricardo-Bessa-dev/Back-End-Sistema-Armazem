package com.example.armazem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

// DTO de ajuste de estoque (apenas quantity e op, sem precisar de nome e preço)

public record QuantityPatchRequest(
        @NotBlank
        String op,

        @Positive
        Integer quantity
){}