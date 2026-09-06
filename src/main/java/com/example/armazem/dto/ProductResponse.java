package com.example.armazem.dto;

import com.example.armazem.entity.Product;
import java.math.BigDecimal;

// DTO resposta relacionada ao produto (o que o servidor devolve)

public record ProductResponse(Long id, String name, Integer quantity, BigDecimal price) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getQuantity(),
                product.getPrice()
        );
    }
}
