package com.example.armazem.service;

import com.example.armazem.dto.ProductRequest;
import com.example.armazem.dto.ProductResponse;
import com.example.armazem.dto.QuantityPatchRequest;
import com.example.armazem.entity.Product;
import com.example.armazem.exception.BusinessException;
import com.example.armazem.exception.NotFoundException;
import com.example.armazem.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductService{
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> listAll(){
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse getById(Long id){
        Product product = findProductOrThrow(id);
        return ProductResponse.from(product);
    }

    @Transactional //Garantia de transação atômica: garante rollback automático em caso de erro
    public ProductResponse create(ProductRequest request){
        Product product = new Product(request.name(), request.quantity(), request.price());
        Product saved = productRepository.save(product);
        return ProductResponse.from(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request){
        Product product = findProductOrThrow(id);
        product.setName(request.name());
        product.setQuantity(request.quantity());
        product.setPrice(request.price());
        Product saved = productRepository.save(product);
        return ProductResponse.from(saved);
    }

    @Transactional
    public ProductResponse updateQuantity(Long id, QuantityPatchRequest request){
        Product product = findProductOrThrow(id);

        switch(request.op()){
            case "add" -> product.setQuantity(product.getQuantity() + request.quantity());
            case "remove" -> {
                if(request.quantity() > product.getQuantity()){
                    throw new BusinessException("Quantidade insuficiente em estoque! Estoque atual: "+ product.getQuantity());
                }
                product.setQuantity(product.getQuantity() - request.quantity());
            }
            default -> throw new BusinessException("Operação inválida: " + request.op());
        }

        Product saved = productRepository.save(product);
        return ProductResponse.from(saved);
    }

    public Long getLastId() {
        return productRepository.findNextId();
    }

    @Transactional
    public void delete(Long id){
        Product product = findProductOrThrow(id);
        productRepository.delete(product);
    }

    private Product findProductOrThrow(Long id){
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado com id: " + id));
    }
}