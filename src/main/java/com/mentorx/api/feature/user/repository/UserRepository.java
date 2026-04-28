package com.mentorx.api.feature.user.repository;

import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.enums.UserStatus;
import com.mentorx.api.feature.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    List<User> findByStatus(UserStatus status);

    List<User> findByMentorStatus(MentorStatus mentorStatus);

    Page<User> findByStatusAndDeletedAtIsNull(UserStatus status, Pageable pageable);

    Page<User> findByMentorStatusAndDeletedAtIsNull(MentorStatus mentorStatus, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:mentorStatus IS NULL OR u.mentorStatus = :mentorStatus) AND " +
           "(:searchTerm IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> findUsersWithFilters(@Param("status") UserStatus status,
                                   @Param("mentorStatus") MentorStatus mentorStatus,
                                   @Param("searchTerm") String searchTerm,
                                   Pageable pageable);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.id = :userId AND u.deletedAt IS NULL")
    Optional<User> findByIdWithRoles(@Param("userId") UUID userId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.mentorProfile WHERE u.id = :userId AND u.deletedAt IS NULL")
    Optional<User> findByIdWithMentorProfile(@Param("userId") UUID userId);

    @Query("SELECT u FROM User u WHERE u.lastSeenAt < :cutoffDate AND u.status = 'ACTIVE'")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status AND u.deletedAt IS NULL")
    long countByStatusAndDeletedAtIsNull(@Param("status") UserStatus status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.mentorStatus = :mentorStatus AND u.deletedAt IS NULL")
    long countByMentorStatusAndDeletedAtIsNull(@Param("mentorStatus") MentorStatus mentorStatus);

    @Query("SELECT u FROM User u WHERE u.createdAt >= :startDate AND u.deletedAt IS NULL")
    List<User> findUsersCreatedAfter(@Param("startDate") LocalDateTime startDate);

    // Full-text search on user profiles
    @Query(value = "SELECT * FROM users u WHERE u.deleted_at IS NULL AND " +
                   "to_tsvector('simple', COALESCE(u.full_name, '') || ' ' || COALESCE(u.bio, '')) " +
                   "@@ plainto_tsquery('simple', :searchQuery)",
           nativeQuery = true)
    List<User> findByFullTextSearch(@Param("searchQuery") String searchQuery);
}