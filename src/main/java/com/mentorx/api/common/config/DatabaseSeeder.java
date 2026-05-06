package com.mentorx.api.common.config;

import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.enums.SupportedLanguage;
import com.mentorx.api.common.enums.UserStatus;
import com.mentorx.api.common.enums.WalletAccountType;
import com.mentorx.api.feature.system.entity.*;
import com.mentorx.api.feature.system.repository.*;
import com.mentorx.api.feature.user.entity.Role;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.entity.UserRole;
import com.mentorx.api.feature.user.repository.RoleRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.repository.UserRoleRepository;
import com.mentorx.api.feature.wallet.entity.Wallet;
import com.mentorx.api.feature.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final CategoryRepository categoryRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PlatformSettingRepository platformSettingRepository;
    private final SkillRepository skillRepository;
    private final WalletRepository walletRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
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

        log.info("Checking database state for seeding...");

        if (roleRepository.count() == 0) {
            log.info("Seeding roles and permissions...");
            seedRoles();
            seedPermissions();
            seedRolePermissions();
        }

        if (platformSettingRepository.count() == 0) {
            log.info("Seeding platform settings...");
            seedPlatformSettings();
        }

        if (skillRepository.count() == 0) {
            log.info("Seeding skills...");
            seedSkills();
        }

        if (categoryRepository.count() == 0) {
            log.info("Seeding categories...");
            seedCategories();
        }

        if (walletRepository.count() == 0) {
            log.info("Seeding system wallets...");
            seedSystemWallets();
        }

        if (userRepository.count() == 0) {
            log.info("Seeding sample users...");
            seedUsers();
        }

        log.info("Database seeding check completed.");
    }

    private void seedRoles() {
        log.info("Seeding roles...");
        List<Role> roles = Arrays.asList(
                Role.builder().roleName("ADMIN").description("Full system access and control").createdAt(LocalDateTime.now()).build(),
                Role.builder().roleName("MODERATOR").description("Content moderation and user management").createdAt(LocalDateTime.now()).build(),
                Role.builder().roleName("USER").description("Standard platform user").createdAt(LocalDateTime.now()).build(),
                Role.builder().roleName("MENTOR").description("Approved mentor with course and job posting rights").createdAt(LocalDateTime.now()).build()
        );
        roleRepository.saveAll(roles);
    }

    private void seedPermissions() {
        log.info("Seeding permissions...");
        List<Permission> permissions = Arrays.asList(
                Permission.builder().permissionKey("user:view:any").description("View any user profile").createdAt(LocalDateTime.now()).build(),
                Permission.builder().permissionKey("user:ban").description("Ban a user").createdAt(LocalDateTime.now()).build(),
                Permission.builder().permissionKey("user:suspend").description("Suspend a user account").createdAt(LocalDateTime.now()).build(),
                Permission.builder().permissionKey("user:delete").description("Delete a user account").createdAt(LocalDateTime.now()).build(),
                Permission.builder().permissionKey("user:change_role").description("Change user roles").createdAt(LocalDateTime.now()).build(),
                Permission.builder().permissionKey("mentor:approve").description("Approve mentor applications").createdAt(LocalDateTime.now()).build(),
                Permission.builder().permissionKey("mentor:reject").description("Reject mentor applications").createdAt(LocalDateTime.now()).build(),
                Permission.builder().permissionKey("job:view:any").description("View any job").createdAt(LocalDateTime.now()).build(),
                Permission.builder().permissionKey("job:force_close").description("Force close a job").createdAt(LocalDateTime.now()).build(),
                Permission.builder().permissionKey("course:approve").description("Approve a course for publishing").createdAt(LocalDateTime.now()).build(),
                Permission.builder().permissionKey("course:reject").description("Reject a course").createdAt(LocalDateTime.now()).build(),
                Permission.builder().permissionKey("payment:view:all").description("View all transactions").createdAt(LocalDateTime.now()).build(),
                Permission.builder().permissionKey("payment:approve_withdraw").description("Approve withdrawal requests").createdAt(LocalDateTime.now()).build(),
                Permission.builder().permissionKey("dispute:review").description("Review disputes").createdAt(LocalDateTime.now()).build(),
                Permission.builder().permissionKey("dispute:resolve").description("Resolve disputes and force payouts").createdAt(LocalDateTime.now()).build(),
                Permission.builder().permissionKey("report:review").description("Review user reports").createdAt(LocalDateTime.now()).build(),
                Permission.builder().permissionKey("settings:view").description("View platform settings").createdAt(LocalDateTime.now()).build(),
                Permission.builder().permissionKey("settings:edit").description("Edit platform settings").createdAt(LocalDateTime.now()).build()
        );
        permissionRepository.saveAll(permissions);
    }

    private void seedRolePermissions() {
        log.info("Assigning permissions to roles...");
        Role adminRole = roleRepository.findByRoleName("ADMIN").orElseThrow();
        Role modRole = roleRepository.findByRoleName("MODERATOR").orElseThrow();
        List<Permission> allPermissions = permissionRepository.findAll();

        // Assign all to ADMIN
        allPermissions.forEach(p -> rolePermissionRepository.save(
                RolePermission.builder().roleId(adminRole.getId()).permissionId(p.getId()).build()
        ));

        // Assign some to MODERATOR
        List<String> modPermKeys = Arrays.asList(
                "user:view:any", "user:suspend", "mentor:approve", "mentor:reject",
                "job:view:any", "job:force_close", "course:approve", "course:reject",
                "dispute:review", "report:review"
        );
        allPermissions.stream()
                .filter(p -> modPermKeys.contains(p.getPermissionKey()))
                .forEach(p -> rolePermissionRepository.save(
                        RolePermission.builder().roleId(modRole.getId()).permissionId(p.getId()).build()
                ));
    }

    private void seedPlatformSettings() {
        log.info("Seeding platform settings...");
        List<PlatformSetting> settings = Arrays.asList(
                PlatformSetting.builder().key("platform_fee_percent").value("10.00").description("Platform commission % deducted from each job payment").updatedAt(LocalDateTime.now()).build(),
                PlatformSetting.builder().key("withdrawal_fee_percent").value("2.00").description("Fee % charged on each withdrawal").updatedAt(LocalDateTime.now()).build(),
                PlatformSetting.builder().key("min_withdrawal_mxc").value("100.00").description("Minimum MXC amount to request withdrawal").updatedAt(LocalDateTime.now()).build(),
                PlatformSetting.builder().key("max_withdrawal_mxc_per_day").value("10000.00").description("Maximum MXC withdrawable per day per user").updatedAt(LocalDateTime.now()).build(),
                PlatformSetting.builder().key("mxc_to_vnd_rate").value("1000.00").description("Conversion: 1 MXC = X VND").updatedAt(LocalDateTime.now()).build()
        );
        platformSettingRepository.saveAll(settings);
    }

    private void seedSkills() {
        log.info("Seeding skills...");
        List<Skill> skills = Arrays.asList(
                Skill.builder().slug("java").labelVi("Java").labelEn("Java").createdAt(LocalDateTime.now()).build(),
                Skill.builder().slug("spring-boot").labelVi("Spring Boot").labelEn("Spring Boot").createdAt(LocalDateTime.now()).build(),
                Skill.builder().slug("python").labelVi("Python").labelEn("Python").createdAt(LocalDateTime.now()).build(),
                Skill.builder().slug("react").labelVi("React.js").labelEn("React.js").createdAt(LocalDateTime.now()).build(),
                Skill.builder().slug("nodejs").labelVi("Node.js").labelEn("Node.js").createdAt(LocalDateTime.now()).build(),
                Skill.builder().slug("ui-ux").labelVi("Thiết kế UI/UX").labelEn("UI/UX Design").createdAt(LocalDateTime.now()).build()
        );
        skillRepository.saveAll(skills);
    }

    private void seedCategories() {
        log.info("Seeding categories...");
        List<Category> categories = Arrays.asList(
                Category.builder().slug("software-dev").labelVi("Lập trình phần mềm").labelEn("Software Development").displayOrder((short) 1).createdAt(LocalDateTime.now()).build(),
                Category.builder().slug("data-ai").labelVi("Dữ liệu & AI").labelEn("Data & AI").displayOrder((short) 2).createdAt(LocalDateTime.now()).build(),
                Category.builder().slug("design").labelVi("Thiết kế").labelEn("Design").displayOrder((short) 3).createdAt(LocalDateTime.now()).build(),
                Category.builder().slug("business-finance").labelVi("Kinh doanh & Tài chính").labelEn("Business & Finance").displayOrder((short) 4).createdAt(LocalDateTime.now()).build()
        );
        categoryRepository.saveAll(categories);
    }

    private void seedSystemWallets() {
        log.info("Seeding system wallets...");
        walletRepository.save(Wallet.builder().accountType(WalletAccountType.PLATFORM_REVENUE).balanceMxc(BigDecimal.ZERO).build());
        walletRepository.save(Wallet.builder().accountType(WalletAccountType.PLATFORM_FLOAT).balanceMxc(BigDecimal.ZERO).build());
        walletRepository.save(Wallet.builder().accountType(WalletAccountType.ESCROW).balanceMxc(BigDecimal.ZERO).build());
    }

    private void seedUsers() {
        log.info("Seeding users...");

        // 1. Admin
        User admin = createUser("admin@mentorx.demo", "System Administrator", "Admin", UserStatus.ACTIVE, false, MentorStatus.NONE);
        assignRoleToUser(admin, "ADMIN");
        assignRoleToUser(admin, "USER");
        setupUserAccount(admin);

        // 2. Mentors
        User mentor1 = createUser("mentor1@mentorx.demo", "Nguyễn Văn An", "An Nguyen", UserStatus.ACTIVE, true, MentorStatus.APPROVED);
        assignRoleToUser(mentor1, "MENTOR");
        assignRoleToUser(mentor1, "USER");
        setupUserAccount(mentor1);

        User mentor2 = createUser("mentor2@mentorx.demo", "Trần Thị Bình", "Binh Tran", UserStatus.ACTIVE, true, MentorStatus.APPROVED);
        assignRoleToUser(mentor2, "MENTOR");
        assignRoleToUser(mentor2, "USER");
        setupUserAccount(mentor2);

        // 3. Clients
        User client1 = createUser("client1@mentorx.demo", "Công ty ABC", "ABC Company", UserStatus.ACTIVE, false, MentorStatus.NONE);
        assignRoleToUser(client1, "USER");
        setupUserAccount(client1);
    }

    private User createUser(String email, String fullName, String displayName, UserStatus status, boolean isMentor, MentorStatus mentorStatus) {
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("password"))
                .fullName(fullName)
                .displayName(displayName)
                .status(status)
                .isEmailVerified(true)
                .isMentor(isMentor)
                .mentorStatus(mentorStatus)
                .preferredLanguage(SupportedLanguage.vi)
                .profileIsPublic(true)
                .build();
        return userRepository.save(user);
    }

    private void assignRoleToUser(User user, String roleName) {
        Role role = roleRepository.findByRoleName(roleName).orElseThrow();
        userRoleRepository.save(UserRole.builder()
                .userId(user.getId())
                .roleId(role.getId())
                .grantedAt(LocalDateTime.now())
                .build());
    }

    private void setupUserAccount(User user) {
        // Wallets
        walletRepository.save(Wallet.builder().user(user).accountType(WalletAccountType.USER_AVAILABLE).balanceMxc(new BigDecimal("1000.00")).build());
        walletRepository.save(Wallet.builder().user(user).accountType(WalletAccountType.USER_PENDING).balanceMxc(BigDecimal.ZERO).build());

        // Notification Preferences
        notificationPreferenceRepository.save(NotificationPreference.builder()
                .userId(user.getId())
                .emailEnabled(true)
                .pushEnabled(true)
                .inAppEnabled(true)
                .emailTypeSettings("{}")
                .pushTypeSettings("{}")
                .updatedAt(LocalDateTime.now())
                .build());
    }
}
