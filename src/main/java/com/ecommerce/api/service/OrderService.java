package com.ecommerce.api.service;

import com.ecommerce.api.dto.OrderResponse;
import com.ecommerce.api.exception.BadRequestException;
import com.ecommerce.api.model.*;
import com.ecommerce.api.repository.OrderRepository;
import com.ecommerce.api.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final PaymentService paymentService;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, CartService cartService,
                        PaymentService paymentService, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.paymentService = paymentService;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse checkout(User user, String stripeToken) {
        Cart cart = cartService.getOrCreateCart(user);

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        // Calculate total
        BigDecimal total = cart.getItems().stream()
                .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Charge via Stripe
        String chargeId = paymentService.createCharge(stripeToken, total,
                "Order for " + user.getEmail());

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(total);
        order.setStatus(Order.OrderStatus.PAID);
        order.setStripePaymentId(chargeId);

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem(order, cartItem.getProduct(),
                    cartItem.getQuantity(), cartItem.getProduct().getPrice());
            order.getItems().add(orderItem);

            // Decrease stock
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order = orderRepository.save(order);

        // Clear cart
        cartService.clearCart(user);

        return OrderResponse.from(order);
    }

    public List<OrderResponse> getOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(OrderResponse::from).toList();
    }

    public OrderResponse getOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BadRequestException("Order not found"));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Order not found");
        }
        return OrderResponse.from(order);
    }
}
