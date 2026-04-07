package com.ecommerce.api.dto;

import com.ecommerce.api.model.Product;
import java.math.BigDecimal;

public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;

    public static ProductResponse from(Product product) {
        ProductResponse r = new ProductResponse();
        r.id = product.getId();
        r.name = product.getName();
        r.description = product.getDescription();
        r.price = product.getPrice();
        r.stockQuantity = product.getStockQuantity();
        r.imageUrl = product.getImageUrl();
        return r;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public String getImageUrl() { return imageUrl; }
}
