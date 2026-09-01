package com.example.armazem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank
        @Size(max = 120)
        String name,

        @NotNull
        @PositiveOrZero
        Integer quantity,

        @NotNull
        @Positive
        BigDecimal price
){}