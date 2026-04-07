package com.ecommerce.api.service;

import com.ecommerce.api.exception.BadRequestException;
import com.ecommerce.api.model.*;
import com.ecommerce.api.repository.OrderRepository;
import com.ecommerce.api.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartService cartService;
    @Mock private PaymentService paymentService;
    @Mock private ProductRepository productRepository;

    private OrderService orderService;
    private User testUser;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, cartService, paymentService, productRepository);

        testUser = new User("test@test.com", "hashed", "Test");
        testUser.setId(1L);
    }

    @Test
    void checkout_withItems_createsOrderAndCharges() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Widget");
        product.setPrice(new BigDecimal("10.00"));
        product.setStockQuantity(5);

        Cart cart = new Cart(testUser);
        cart.setItems(new ArrayList<>(List.of(new CartItem(cart, product, 2))));

        when(cartService.getOrCreateCart(testUser)).thenReturn(cart);
        when(paymentService.createCharge(eq("tok_test"), any(BigDecimal.class), anyString()))
                .thenReturn("ch_123");
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        var response = orderService.checkout(testUser, "tok_test");

        assertEquals("PAID", response.getStatus());
        assertEquals(new BigDecimal("20.00"), response.getTotalAmount());
        assertEquals("ch_123", response.getStripePaymentId());
        verify(productRepository).save(product);
        assertEquals(3, product.getStockQuantity()); // 5 - 2
        verify(cartService).clearCart(testUser);
    }

    @Test
    void checkout_emptyCart_throwsBadRequest() {
        Cart cart = new Cart(testUser);
        cart.setItems(new ArrayList<>());
        when(cartService.getOrCreateCart(testUser)).thenReturn(cart);

        assertThrows(BadRequestException.class, () -> orderService.checkout(testUser, "tok_test"));
    }

    @Test
    void getOrders_returnsUserOrders() {
        Order order = new Order();
        order.setId(1L);
        order.setUser(testUser);
        order.setTotalAmount(new BigDecimal("20.00"));
        order.setStatus(Order.OrderStatus.PAID);
        order.setItems(new ArrayList<>());

        when(orderRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(List.of(order));

        var orders = orderService.getOrders(testUser);

        assertEquals(1, orders.size());
        assertEquals(new BigDecimal("20.00"), orders.get(0).getTotalAmount());
    }
}
