package com.ecommerce.api.repository;

import com.ecommerce.api.model.Cart;
import com.ecommerce.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
