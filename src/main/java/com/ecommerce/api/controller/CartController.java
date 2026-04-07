package com.ecommerce.api.controller;

import com.ecommerce.api.dto.CartItemRequest;
import com.ecommerce.api.dto.CartResponse;
import com.ecommerce.api.model.User;
import com.ecommerce.api.repository.UserRepository;
import com.ecommerce.api.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(@AuthenticationPrincipal UserDetails userDetails,
                                                  @Valid @RequestBody CartItemRequest request) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(cartService.addToCart(user, request));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateCartItem(@AuthenticationPrincipal UserDetails userDetails,
                                                       @PathVariable Long productId,
                                                       @RequestParam Integer quantity) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(cartService.updateCartItem(user, productId, quantity));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeFromCart(@AuthenticationPrincipal UserDetails userDetails,
                                               @PathVariable Long productId) {
        User user = getUser(userDetails);
        cartService.removeFromCart(user, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        cartService.clearCart(user);
        return ResponseEntity.noContent().build();
    }

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
