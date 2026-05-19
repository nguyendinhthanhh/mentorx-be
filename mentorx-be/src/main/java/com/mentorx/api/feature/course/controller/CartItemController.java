package com.mentorx.api.feature.course.controller;

import com.mentorx.api.feature.course.dto.request.CartItemCreateRequest;
import com.mentorx.api.feature.course.dto.response.CartItemResponse;
import com.mentorx.api.feature.course.service.CartItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartItemResponse> addToCart(
            @Valid @RequestBody CartItemCreateRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        CartItemResponse response = cartItemService.addToCart(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CartItemResponse>> getCartItems(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<CartItemResponse> responses = cartItemService.getCartItemsByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countCartItems(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        Long count = cartItemService.countCartItems(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/course/{courseId}/is-in-cart")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> isInCart(
            @PathVariable UUID courseId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        boolean isInCart = cartItemService.isInCart(userId, courseId);
        return ResponseEntity.ok(isInCart);
    }

    @DeleteMapping("/course/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeFromCart(
            @PathVariable UUID courseId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        cartItemService.removeFromCart(userId, courseId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        cartItemService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
