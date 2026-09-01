package com.example.armazem.repository;

import com.example.armazem.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long>{
    @Query("SELECT COALESCE(MAX(p.id), 0) + 1 FROM Product p")  // Método customizado
    Long findNextId();
}