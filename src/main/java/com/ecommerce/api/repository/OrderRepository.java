package com.ecommerce.api.repository;

import com.ecommerce.api.model.Order;
import com.ecommerce.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
}
