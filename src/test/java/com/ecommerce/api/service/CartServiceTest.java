package com.ecommerce.api.service;

import com.ecommerce.api.dto.CartItemRequest;
import com.ecommerce.api.dto.CartResponse;
import com.ecommerce.api.model.*;
import com.ecommerce.api.repository.CartRepository;
import com.ecommerce.api.repository.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductService productService;

    private CartService cartService;
    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, cartItemRepository, productService);

        testUser = new User("test@test.com", "hashed", "Test");
        testUser.setId(1L);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Widget");
        testProduct.setPrice(new BigDecimal("9.99"));
        testProduct.setStockQuantity(10);
    }

    @Test
    void getCart_noExistingCart_createsNewCart() {
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());
        Cart newCart = new Cart(testUser);
        newCart.setItems(new ArrayList<>());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        CartResponse response = cartService.getCart(testUser);

        assertNotNull(response);
        assertEquals(0, response.getItems().size());
        assertEquals(BigDecimal.ZERO, response.getTotalPrice());
    }

    @Test
    void addToCart_newItem_addsSuccessfully() {
        Cart cart = new Cart(testUser);
        cart.setItems(new ArrayList<>());
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(cart));
        when(productService.findProductById(1L)).thenReturn(testProduct);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartItemRequest req = new CartItemRequest();
        req.setProductId(1L);
        req.setQuantity(2);

        CartResponse response = cartService.addToCart(testUser, req);

        assertNotNull(response);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void removeFromCart_existingItem_removesSuccessfully() {
        Cart cart = new Cart(testUser);
        cart.setItems(new ArrayList<>());
        CartItem item = new CartItem(cart, testProduct, 2);
        item.setId(1L);
        cart.getItems().add(item);

        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.removeFromCart(testUser, 1L);

        verify(cartRepository).save(cart);
        assertEquals(0, cart.getItems().size());
    }
}
