package com.mentorx.api.feature.user.repository;

import com.mentorx.api.feature.user.entity.UserRole;
import com.mentorx.api.feature.user.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findByUserId(UUID userId);

    List<UserRole> findByRoleId(Integer roleId);

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.role WHERE ur.userId = :userId")
    List<UserRole> findByUserIdWithRole(@Param("userId") UUID userId);

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.user WHERE ur.roleId = :roleId")
    List<UserRole> findByRoleIdWithUser(@Param("roleId") Integer roleId);

    boolean existsByUserIdAndRoleId(UUID userId, Integer roleId);

    void deleteByUserIdAndRoleId(UUID userId, Integer roleId);

    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.roleId = :roleId")
    long countUsersByRoleId(@Param("roleId") Integer roleId);
}