package com.example.armazem.repository;

import com.example.armazem.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

// Interface de acesso a dados relacionados a product (contrato de operações para a tabela products)

public interface ProductRepository extends JpaRepository<Product, Long>{
    @Query("SELECT COALESCE(MAX(p.id), 0) + 1 FROM Product p")  // Método customizado
    Long findNextId();
}