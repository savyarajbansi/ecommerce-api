package com.ecommerce.api.service;

import com.ecommerce.api.dto.ProductRequest;
import com.ecommerce.api.dto.ProductResponse;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.model.Product;
import com.ecommerce.api.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

    @Test
    void createProduct_returnsProductResponse() {
        ProductRequest req = new ProductRequest();
        req.setName("Widget");
        req.setDescription("A widget");
        req.setPrice(new BigDecimal("9.99"));
        req.setStockQuantity(100);

        Product saved = new Product();
        saved.setId(1L);
        saved.setName("Widget");
        saved.setDescription("A widget");
        saved.setPrice(new BigDecimal("9.99"));
        saved.setStockQuantity(100);

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.createProduct(req);

        assertEquals(1L, response.getId());
        assertEquals("Widget", response.getName());
    }

    @Test
    void getProduct_existingId_returnsProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Widget");
        product.setPrice(new BigDecimal("9.99"));
        product.setStockQuantity(10);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProduct(1L);
        assertEquals("Widget", response.getName());
    }

    @Test
    void getProduct_missingId_throwsNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProduct(999L));
    }

    @Test
    void searchProducts_returnsMatchingProducts() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Widget");
        p.setPrice(new BigDecimal("9.99"));
        p.setStockQuantity(10);

        when(productRepository.findByNameContainingIgnoreCase("wid")).thenReturn(List.of(p));

        List<ProductResponse> results = productService.searchProducts("wid");
        assertEquals(1, results.size());
        assertEquals("Widget", results.get(0).getName());
    }
}
