package com.example.armazem.controller;

import com.example.armazem.dto.ProductRequest;
import com.example.armazem.dto.ProductResponse;
import com.example.armazem.dto.QuantityPatchRequest;
import com.example.armazem.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> listAll() {
        return productService.listAll();
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @PatchMapping("/{id}")
    public ProductResponse updateQuantity(@PathVariable Long id, @Valid @RequestBody QuantityPatchRequest request) {
        return productService.updateQuantity(id, request);
    }

    @GetMapping("/lastId")
    public Map<String, Long> getLastId(){
        return Map.of("id", productService.getLastId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}