package com.ecommerce.api.dto;

import com.ecommerce.api.model.Cart;
import com.ecommerce.api.model.CartItem;
import java.math.BigDecimal;
import java.util.List;

public class CartResponse {
    private List<Item> items;
    private BigDecimal totalPrice;

    public static CartResponse from(Cart cart) {
        CartResponse r = new CartResponse();
        r.items = cart.getItems().stream().map(Item::from).toList();
        r.totalPrice = cart.getItems().stream()
                .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return r;
    }

    public List<Item> getItems() { return items; }
    public BigDecimal getTotalPrice() { return totalPrice; }

    public static class Item {
        private Long productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;

        public static Item from(CartItem ci) {
            Item item = new Item();
            item.productId = ci.getProduct().getId();
            item.productName = ci.getProduct().getName();
            item.price = ci.getProduct().getPrice();
            item.quantity = ci.getQuantity();
            item.subtotal = ci.getProduct().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            return item;
        }

        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public BigDecimal getPrice() { return price; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getSubtotal() { return subtotal; }
    }
}
