package com.mentorx.api.common.config;

import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.enums.UserStatus;
import com.mentorx.api.feature.system.entity.Category;
import com.mentorx.api.feature.system.repository.CategoryRepository;
import com.mentorx.api.feature.user.entity.Role;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.entity.UserRole;
import com.mentorx.api.feature.user.repository.RoleRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.database.seed-data:false}")
    private boolean seedData;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedData) {
            log.info("Database seeding is disabled by configuration (app.database.seed-data=false).");
            return;
        }

        log.info("Starting database seeding...");

        seedRoles();
        seedUsers();
        seedCategories();

        log.info("Database seeding completed successfully.");
    }

    private void seedRoles() {
        if (roleRepository.count() > 0) {
            log.info("Roles already exist. Skipping role seeding.");
            return;
        }

        log.info("Seeding roles...");
        List<String> roleNames = Arrays.asList("ROLE_ADMIN", "ROLE_USER", "ROLE_MENTOR");
        
        for (String roleName : roleNames) {
            Role role = Role.builder()
                    .roleName(roleName)
                    .description("Default " + roleName)
                    .createdAt(LocalDateTime.now())
                    .build();
            roleRepository.save(role);
        }
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            log.info("Users already exist. Skipping user seeding.");
            return;
        }

        log.info("Seeding users...");

        // 1. Create Admin
        User admin = User.builder()
                .email("admin@mentorx.local")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("System Administrator")
                .displayName("Admin")
                .status(UserStatus.ACTIVE)
                .isEmailVerified(true)
                .profileIsPublic(false)
                .build();
        userRepository.save(admin);
        assignRole(admin, "ROLE_ADMIN");

        // 2. Create Mentor
        User mentor = User.builder()
                .email("mentor@mentorx.local")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("Sample Mentor")
                .displayName("MentorPro")
                .status(UserStatus.ACTIVE)
                .isEmailVerified(true)
                .isMentor(true)
                .mentorStatus(MentorStatus.APPROVED)
                .bio("I am an experienced mentor in Software Development.")
                .build();
        userRepository.save(mentor);
        assignRole(mentor, "ROLE_MENTOR");
        assignRole(mentor, "ROLE_USER");

        // 3. Create Student
        User student = User.builder()
                .email("student@mentorx.local")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("Sample Student")
                .displayName("StudentZero")
                .status(UserStatus.ACTIVE)
                .isEmailVerified(true)
                .bio("I am eager to learn and grow.")
                .build();
        userRepository.save(student);
        assignRole(student, "ROLE_USER");
    }

    private void assignRole(User user, String roleName) {
        Role role = roleRepository.findAll().stream()
                .filter(r -> r.getRoleName().equals(roleName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        UserRole userRole = UserRole.builder()
                .userId(user.getId())
                .roleId(role.getId())
                .user(user)
                .role(role)
                .grantedAt(LocalDateTime.now())
                .build();
        
        userRoleRepository.save(userRole);
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) {
            log.info("Categories already exist. Skipping category seeding.");
            return;
        }

        log.info("Seeding categories...");

        List<Category> categories = Arrays.asList(
                createCategory("software-development", "Phát triển phần mềm", "Software Development", 1),
                createCategory("design", "Thiết kế", "Design", 2),
                createCategory("business", "Kinh doanh", "Business", 3),
                createCategory("data-science", "Khoa học dữ liệu", "Data Science", 4),
                createCategory("languages", "Ngoại ngữ", "Languages", 5)
        );

        categoryRepository.saveAll(categories);
    }

    private Category createCategory(String slug, String labelVi, String labelEn, int order) {
        return Category.builder()
                .slug(slug)
                .labelVi(labelVi)
                .labelEn(labelEn)
                .isActive(true)
                .displayOrder((short) order)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
