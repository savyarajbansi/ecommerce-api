package com.ecommerce.api.dto;

import com.ecommerce.api.model.Order;
import com.ecommerce.api.model.OrderItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    private Long id;
    private BigDecimal totalAmount;
    private String status;
    private String stripePaymentId;
    private LocalDateTime createdAt;
    private List<Item> items;

    public static OrderResponse from(Order order) {
        OrderResponse r = new OrderResponse();
        r.id = order.getId();
        r.totalAmount = order.getTotalAmount();
        r.status = order.getStatus().name();
        r.stripePaymentId = order.getStripePaymentId();
        r.createdAt = order.getCreatedAt();
        r.items = order.getItems().stream().map(Item::from).toList();
        return r;
    }

    public Long getId() { return id; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getStripePaymentId() { return stripePaymentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<Item> getItems() { return items; }

    public static class Item {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal priceAtPurchase;

        public static Item from(OrderItem oi) {
            Item item = new Item();
            item.productId = oi.getProduct().getId();
            item.productName = oi.getProduct().getName();
            item.quantity = oi.getQuantity();
            item.priceAtPurchase = oi.getPriceAtPurchase();
            return item;
        }

        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getPriceAtPurchase() { return priceAtPurchase; }
    }
}
