package com.mentorx.api.feature.course.service.impl;

import com.mentorx.api.common.exception.ErrorCode;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.feature.course.dto.request.CartItemCreateRequest;
import com.mentorx.api.feature.course.dto.response.CartItemResponse;
import com.mentorx.api.feature.course.entity.CartItem;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.mapper.CourseMapper;
import com.mentorx.api.feature.course.repository.CartItemRepository;
import com.mentorx.api.feature.course.repository.CourseRepository;
import com.mentorx.api.feature.course.service.CartItemService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseMapper mapper;

    @Override
    @Transactional
    public CartItemResponse addToCart(CartItemCreateRequest request, UUID userId) {
        log.info("Adding course: {} to cart for user: {}", request.getCourseId(), userId);

        if (cartItemRepository.existsByUserIdAndCourseId(userId, request.getCourseId())) {
            throw new AppException(ErrorCode.COURSE_ALREADY_IN_CART);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        CartItem cartItem = mapper.toEntity(request);
        cartItem.setUser(user);
        cartItem.setCourse(course);

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        log.info("Course added to cart successfully");

        return mapper.toResponse(savedCartItem);
    }

    @Override
    public List<CartItemResponse> getCartItemsByUserId(UUID userId) {
        log.debug("Fetching cart items for user: {}", userId);
        
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        return mapper.toCartItemResponseList(cartItems);
    }

    @Override
    @Transactional
    public void removeFromCart(UUID userId, UUID courseId) {
        log.info("Removing course: {} from cart for user: {}", courseId, userId);

        if (!cartItemRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        cartItemRepository.deleteByUserIdAndCourseId(userId, courseId);
        log.info("Course removed from cart successfully");
    }

    @Override
    @Transactional
    public void clearCart(UUID userId) {
        log.info("Clearing cart for user: {}", userId);
        
        cartItemRepository.deleteByUserId(userId);
        log.info("Cart cleared successfully");
    }

    @Override
    public Long countCartItems(UUID userId) {
        log.debug("Counting cart items for user: {}", userId);
        return cartItemRepository.countByUserId(userId);
    }

    @Override
    public boolean isInCart(UUID userId, UUID courseId) {
        log.debug("Checking if course: {} is in cart for user: {}", courseId, userId);
        return cartItemRepository.existsByUserIdAndCourseId(userId, courseId);
    }
}
