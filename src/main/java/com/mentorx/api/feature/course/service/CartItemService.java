package com.mentorx.api.feature.course.service;

import com.mentorx.api.feature.course.dto.request.CartItemCreateRequest;
import com.mentorx.api.feature.course.dto.response.CartItemResponse;

import java.util.List;
import java.util.UUID;

public interface CartItemService {
    
    CartItemResponse addToCart(CartItemCreateRequest request, UUID userId);
    
    List<CartItemResponse> getCartItemsByUserId(UUID userId);
    
    void removeFromCart(UUID userId, UUID courseId);
    
    void clearCart(UUID userId);
    
    Long countCartItems(UUID userId);
    
    boolean isInCart(UUID userId, UUID courseId);
}
