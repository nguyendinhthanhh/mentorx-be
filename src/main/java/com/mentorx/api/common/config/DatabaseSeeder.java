package com.mentorx.api.common.config;

import com.mentorx.api.common.enums.BudgetType;
import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.common.enums.JobType;
import com.mentorx.api.common.enums.LessonType;
import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.enums.PackageType;
import com.mentorx.api.common.enums.PayoutMethod;
import com.mentorx.api.common.enums.SupportedLanguage;
import com.mentorx.api.common.enums.UserStatus;
import com.mentorx.api.common.enums.VerificationStatus;
import com.mentorx.api.common.enums.WalletAccountType;
import com.mentorx.api.feature.chat.entity.ChatRoom;
import com.mentorx.api.feature.chat.entity.ChatRoomMember;
import com.mentorx.api.feature.chat.entity.Message;
import com.mentorx.api.feature.chat.enums.ChatRoomType;
import com.mentorx.api.feature.chat.repository.ChatRoomMemberRepository;
import com.mentorx.api.feature.chat.repository.ChatRoomRepository;
import com.mentorx.api.feature.chat.repository.MessageRepository;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.entity.CourseLesson;
import com.mentorx.api.feature.course.entity.CourseSection;
import com.mentorx.api.feature.course.repository.CourseLessonRepository;
import com.mentorx.api.feature.course.repository.CourseRepository;
import com.mentorx.api.feature.course.repository.CourseSectionRepository;
import com.mentorx.api.feature.job.entity.Job;
import com.mentorx.api.feature.job.entity.Proposal;
import com.mentorx.api.feature.job.enums.ProposalStatus;
import com.mentorx.api.feature.job.repository.JobRepository;
import com.mentorx.api.feature.job.repository.ProposalRepository;
import com.mentorx.api.feature.mentor.entity.MentorAvailability;
import com.mentorx.api.feature.mentor.entity.MentorPackage;
import com.mentorx.api.feature.mentor.repository.MentorAvailabilityRepository;
import com.mentorx.api.feature.mentor.repository.MentorPackageRepository;
import com.mentorx.api.feature.notification.entity.Notification;
import com.mentorx.api.feature.notification.enums.NotificationType;
import com.mentorx.api.feature.notification.repository.NotificationRepository;
import com.mentorx.api.feature.review.entity.Review;
import com.mentorx.api.feature.review.enums.ReviewTargetType;
import com.mentorx.api.feature.review.repository.ReviewRepository;
import com.mentorx.api.feature.system.config.FileStorageProperties;
import com.mentorx.api.feature.system.entity.*;
import com.mentorx.api.feature.system.repository.*;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.user.entity.Role;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.entity.UserRole;
import com.mentorx.api.feature.user.repository.MentorProfileRepository;
import com.mentorx.api.feature.user.repository.RoleRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.repository.UserRoleRepository;
import com.mentorx.api.feature.wallet.entity.Wallet;
import com.mentorx.api.feature.wallet.entity.ExchangeRate;
import com.mentorx.api.feature.wallet.repository.ExchangeRateRepository;
import com.mentorx.api.feature.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final CategoryRepository categoryRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PlatformSettingRepository platformSettingRepository;
    private final SkillRepository skillRepository;
    private final WalletRepository walletRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final JobRepository jobRepository;
    private final ProposalRepository proposalRepository;
    private final CourseRepository courseRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final MentorPackageRepository mentorPackageRepository;
    private final MentorAvailabilityRepository mentorAvailabilityRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;
    private final FileStorageProperties fileStorageProperties;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.database.seed-data:false}")
    private boolean seedData;

    @Override
    public void run(String... args) {
        if (!seedData) {
            log.info("Database seeding is disabled by configuration (app.database.seed-data=false).");
            return;
        }

        log.info("Checking database state for seeding...");

        safeSeed("roles and permissions", () -> {
            if (roleRepository.count() == 0) {
                seedRoles();
                seedPermissions();
                seedRolePermissions();
            }
        });

        safeSeed("platform settings", () -> {
            if (platformSettingRepository.count() == 0) {
                seedPlatformSettings();
            }
        });

        safeSeed("skills", () -> {
            if (skillRepository.count() == 0) {
                seedSkills();
            }
        });

        safeSeed("categories", () -> {
            if (categoryRepository.count() == 0) {
                seedCategories();
            }
        });

        safeSeed("system wallets", () -> {
            if (walletRepository.count() == 0) {
                seedSystemWallets();
            }
        });

        safeSeed("exchange rates", this::seedExchangeRates);

        safeSeed("sample users", this::seedUsers);

        safeSeed("mentor profiles", this::seedMentorProfiles);

        safeSeed("courses", this::seedCourses);

        safeSeed("jobs", this::seedJobs);

        safeSeed("proposals", this::seedProposals);

        safeSeed("mentor packages", this::seedMentorPackages);

        safeSeed("mentor availability", this::seedMentorAvailability);

        safeSeed("reviews", this::seedReviews);

        safeSeed("notifications", () -> {
            if (notificationRepository.count() == 0) {
                seedNotifications();
            }
        });

        safeSeed("chat rooms and messages", () -> {
            if (chatRoomRepository.count() == 0) {
                seedChatData();
            }
        });

        log.info("Database seeding check completed.");
    }

    private void safeSeed(String name, Runnable block) {
        try {
            log.info("Seeding {}...", name);
            block.run();
        } catch (Exception e) {
            log.error("Failed to seed {}: {}", name, e.getMessage());
        }
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

    private void seedExchangeRates() {
        log.info("Ensuring baseline exchange rates exist...");
        upsertExchangeRate("VND", "VND", "1.000000", "system-default");
    }

    private void upsertExchangeRate(String fromCurrency, String toCurrency, String rate, String source) {
        boolean exists = exchangeRateRepository
                .findTopByFromCurrencyIgnoreCaseAndToCurrencyIgnoreCaseOrderByEffectiveAtDescCreatedAtDesc(fromCurrency, toCurrency)
                .isPresent();
        if (exists) {
            return;
        }

        exchangeRateRepository.save(ExchangeRate.builder()
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .rate(new BigDecimal(rate))
                .source(source)
                .effectiveAt(LocalDateTime.now())
                .build());
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

        User client2 = createUser("client2@mentorx.demo", "Startup XYZ", "XYZ Startup", UserStatus.ACTIVE, false, MentorStatus.NONE);
        assignRoleToUser(client2, "USER");
        setupUserAccount(client2);

        User client3 = createUser("client3@mentorx.demo", "Doanh nghiệp DEF", "DEF Enterprise", UserStatus.ACTIVE, false, MentorStatus.NONE);
        assignRoleToUser(client3, "USER");
        setupUserAccount(client3);

        User learner1 = createUser("user1@mentorx.demo", "Lê Hà Anh", "Ha Anh", UserStatus.ACTIVE, false, MentorStatus.NONE);
        assignRoleToUser(learner1, "USER");
        setupUserAccount(learner1);

        ensureSeedUser(new DemoUserSeed("moderator@mentorx.demo", "Platform Moderator", "Moderator", false, MentorStatus.NONE, List.of("USER", "MODERATOR")));
        ensureSeedUser(new DemoUserSeed("mentor3@mentorx.demo", "Le Minh Khoa", "Khoa Le", true, MentorStatus.APPROVED, List.of("USER", "MENTOR")));
        ensureSeedUser(new DemoUserSeed("mentor4@mentorx.demo", "Pham Gia Linh", "Linh Pham", true, MentorStatus.APPROVED, List.of("USER", "MENTOR")));
        ensureSeedUser(new DemoUserSeed("mentor5@mentorx.demo", "Do Hoang Nam", "Nam Do", true, MentorStatus.APPROVED, List.of("USER", "MENTOR")));
        ensureSeedUser(new DemoUserSeed("mentor6@mentorx.demo", "Vu Thu Ha", "Ha Vu", true, MentorStatus.APPROVED, List.of("USER", "MENTOR")));
        ensureSeedUser(new DemoUserSeed("client4@mentorx.demo", "Northwind Labs", "Northwind Labs", false, MentorStatus.NONE, List.of("USER")));
        ensureSeedUser(new DemoUserSeed("client5@mentorx.demo", "BluePeak Studio", "BluePeak Studio", false, MentorStatus.NONE, List.of("USER")));
        ensureSeedUser(new DemoUserSeed("client6@mentorx.demo", "Mekong Analytics", "Mekong Analytics", false, MentorStatus.NONE, List.of("USER")));
        ensureSeedUser(new DemoUserSeed("user2@mentorx.demo", "Ngoc Mai", "Ngoc Mai", false, MentorStatus.NONE, List.of("USER")));
        ensureSeedUser(new DemoUserSeed("user3@mentorx.demo", "Quang Huy", "Quang Huy", false, MentorStatus.NONE, List.of("USER")));
        ensureSeedUser(new DemoUserSeed("user4@mentorx.demo", "Bao Chau", "Bao Chau", false, MentorStatus.NONE, List.of("USER")));
    }

    private User createUser(String email, String fullName, String displayName, UserStatus status, boolean isMentor, MentorStatus mentorStatus) {
        String avatarSeed = (displayName != null && !displayName.isBlank() ? displayName : fullName).trim().replace(" ", "+");
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user = existingUser.orElseGet(User::new);
        user.setEmail(email);
        user.setPasswordHash(existingUser.map(User::getPasswordHash).orElseGet(() -> passwordEncoder.encode("password")));
        user.setFullName(fullName);
        user.setDisplayName(displayName);
        user.setAvatarUrl("https://ui-avatars.com/api/?name=" + avatarSeed + "&background=random");
        user.setBio("Demo account seeded for MentorX development.");
        user.setStatus(status);
        user.setIsEmailVerified(true);
        user.setIsMentor(isMentor);
        user.setMentorStatus(mentorStatus);
        user.setPreferredLanguage(SupportedLanguage.vi);
        user.setProfileIsPublic(true);
        user.setIsOnboarded(true);
        return userRepository.save(user);
    }

    private void seedMentorProfiles() {
        User approver = userRepository.findByEmail("admin@mentorx.demo").orElse(null);

        seedMentorProfile(new MentorProfileSeed(
                "mentor1@mentorx.demo",
                "Nguyen Van An",
                "An Nguyen",
                "Senior Java and Spring Boot mentor",
                "FULL_TIME",
                new BigDecimal("450.00"),
                (short) 8,
                new BigDecimal("4.90"),
                42,
                true
        ), approver);

        seedMentorProfile(new MentorProfileSeed(
                "mentor2@mentorx.demo",
                "Tran Thi Binh",
                "Binh Tran",
                "Product design and UI/UX mentor",
                "PART_TIME",
                new BigDecimal("380.00"),
                (short) 6,
                new BigDecimal("4.80"),
                31,
                true
        ), approver);

        seedMentorProfile(new MentorProfileSeed(
                "mentor3@mentorx.demo",
                "Le Minh Khoa",
                "Khoa Le",
                "Data science and machine learning mentor",
                "FLEXIBLE",
                new BigDecimal("520.00"),
                (short) 7,
                new BigDecimal("4.70"),
                27,
                false
        ), approver);

        seedMentorProfile(new MentorProfileSeed(
                "mentor4@mentorx.demo",
                "Pham Gia Linh",
                "Linh Pham",
                "Frontend architecture and React performance mentor",
                "PART_TIME",
                new BigDecimal("410.00"),
                (short) 7,
                new BigDecimal("4.85"),
                25,
                true
        ), approver);

        seedMentorProfile(new MentorProfileSeed(
                "mentor5@mentorx.demo",
                "Do Hoang Nam",
                "Nam Do",
                "Product analytics and experimentation mentor",
                "FLEXIBLE",
                new BigDecimal("430.00"),
                (short) 9,
                new BigDecimal("4.75"),
                29,
                false
        ), approver);

        seedMentorProfile(new MentorProfileSeed(
                "mentor6@mentorx.demo",
                "Vu Thu Ha",
                "Ha Vu",
                "Career coaching and communication mentor for tech teams",
                "FULL_TIME",
                new BigDecimal("360.00"),
                (short) 5,
                new BigDecimal("4.88"),
                22,
                true
        ), approver);
    }

    private void seedMentorProfile(MentorProfileSeed seed, User approver) {
        User user = userRepository.findByEmail(seed.email())
                .orElseGet(() -> createUser(seed.email(), seed.fullName(), seed.displayName(), UserStatus.ACTIVE, true, MentorStatus.APPROVED));

        user.setStatus(UserStatus.ACTIVE);
        user.setFullName(seed.fullName());
        user.setDisplayName(seed.displayName());
        user.setIsEmailVerified(true);
        user.setIsMentor(true);
        user.setMentorStatus(MentorStatus.APPROVED);
        user.setProfileIsPublic(true);
        user.setIsOnboarded(true);
        userRepository.save(user);

        assignRoleToUserIfMissing(user, "MENTOR");
        assignRoleToUserIfMissing(user, "USER");
        setupUserAccountIfMissing(user);

        if (mentorProfileRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }

        mentorProfileRepository.save(MentorProfile.builder()
                .user(user)
                .headline(seed.headline())
                .availability(seed.availability())
                .hourlyRateMxc(seed.hourlyRateMxc())
                .yearsOfExperience(seed.yearsOfExperience())
                .responseTimeHours((short) 12)
                .totalJobsDone(seed.totalReviews())
                .successRate(new BigDecimal("98.00"))
                .averageRating(seed.averageRating())
                .totalReviews(seed.totalReviews())
                .isFeatured(seed.featured())
                .location("Vietnam")
                .languages(List.of("vi", "en"))
                .currentTitle(seed.headline())
                .currentCompany("MentorX Demo Network")
                .primaryDomain(seed.headline())
                .skills(List.of(seed.headline(), "mentoring", "delivery"))
                .professionalBio("Seeded mentor profile for development and QA scenarios.")
                .helpDescription("Supports practical mentoring, reviews, and structured feedback.")
                .mentorAgreementAccepted(true)
                .disputePolicyAccepted(true)
                .submittedAt(LocalDateTime.now().minusDays(14))
                .expertiseStatus(VerificationStatus.APPROVED)
                .identityStatus(VerificationStatus.APPROVED)
                .identityRequired(true)
                .phoneVerified(true)
                .identityVerifiedAt(LocalDateTime.now().minusDays(10))
                .identityVerifiedBy(approver)
                .verificationProvider("seeded-dev-data")
                .payoutStatus(VerificationStatus.APPROVED)
                .payoutCountry("VN")
                .payoutMethod(PayoutMethod.LOCAL_BANK)
                .payoutReviewedBy(approver)
                .payoutReviewedAt(LocalDateTime.now().minusDays(9))
                .portfolioUrl("https://mentorx.local/portfolio/" + seed.displayName().toLowerCase().replace(" ", "-"))
                .approvedBy(approver)
                .approvedAt(LocalDateTime.now())
                .build());
    }

    private void seedCourses() {
        User mentor1 = userRepository.findByEmail("mentor1@mentorx.demo").orElse(null);
        User mentor2 = userRepository.findByEmail("mentor2@mentorx.demo").orElse(null);
        User reviewer = userRepository.findByEmail("admin@mentorx.demo").orElse(null);

        if (mentor1 == null || mentor2 == null) {
            log.warn("Mentor users not found. Skipping course seeding.");
            return;
        }

        Integer devCategoryId = categoryRepository.findBySlug("software-dev").map(Category::getId).orElse(null);
        Integer dataCategoryId = categoryRepository.findBySlug("data-ai").map(Category::getId).orElse(null);
        Integer designCategoryId = categoryRepository.findBySlug("design").map(Category::getId).orElse(null);
        Integer businessCategoryId = categoryRepository.findBySlug("business-finance").map(Category::getId).orElse(null);
        String sampleDocumentUrl = ensureSampleDocument("mentorx-sample-document", "MentorX Sample Document");

        if (courseRepository.findBySlugAndDeletedAtIsNull("spring-boot-foundations").isEmpty()) {
        Course course1 = Course.builder()
                .instructor(mentor1)
                .categoryId(devCategoryId)
                .title("Spring Boot Foundations for Real Projects")
                .slug("spring-boot-foundations")
                .description("Build production-ready REST APIs with Spring Boot, JPA, and testing best practices.")
                .thumbnailUrl("https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=1200&q=80")
                .priceMxc(new BigDecimal("199.00"))
                .status(CourseStatus.PUBLISHED)
                .language(SupportedLanguage.vi)
                .level("Beginner")
                .isCertificate(true)
                .previewVideoUrl("https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4")
                .averageRating(new BigDecimal("4.80"))
                .totalReviews(28)
                .totalEnrollments(152)
                .publishedAt(LocalDateTime.now().minusDays(7))
                .reviewedBy(reviewer)
                .build();
        course1 = courseRepository.save(course1);

        sampleDocumentUrl = ensureSampleDocument("mentorx-sample-document", "MentorX Sample Document");

        CourseSection course1Section1 = createSection(course1, 1, "Khởi động dự án", "Cài đặt môi trường và hiểu cấu trúc dự án.");
        createLesson(course1Section1, 1, "Giới thiệu khóa học", "Tổng quan lộ trình và kết quả đạt được.", LessonType.LESSON, 8, true,
                "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", null, null);
        createLesson(course1Section1, 2, "Tài liệu setup môi trường", "Checklist cài đặt JDK, IDE, và Postgres.", LessonType.LESSON, 12, false,
                null, "Hướng dẫn chi tiết cài đặt môi trường phát triển Spring Boot.", sampleDocumentUrl);
        updateSectionDuration(course1Section1);

        CourseSection course1Section2 = createSection(course1, 2, "Xây dựng REST API", "Thiết kế API và triển khai CRUD chuẩn.");
        createLesson(course1Section2, 1, "Thiết kế data model", "Xây entity và mapping JPA hiệu quả.", LessonType.LESSON, 18, false,
                "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", null, null);
        createLesson(course1Section2, 2, "Tài liệu API mẫu", "Swagger template và checklist kiểm thử.", LessonType.LESSON, 10, false,
                null, "Template Swagger và checklist kiểm thử cơ bản.", sampleDocumentUrl);
        updateSectionDuration(course1Section2);

        updateCourseTotals(course1);
        }

        if (courseRepository.findBySlugAndDeletedAtIsNull("ux-design-sprint").isEmpty()) {
        Course course2 = Course.builder()
                .instructor(mentor2)
                .categoryId(designCategoryId)
                .title("UI/UX Design Sprint for Product Teams")
                .slug("ux-design-sprint")
                .description("From discovery to prototype: master the design sprint process with practical exercises.")
                .thumbnailUrl("https://images.unsplash.com/photo-1521737604893-d14cc237f11d?auto=format&fit=crop&w=1200&q=80")
                .priceMxc(new BigDecimal("149.00"))
                .status(CourseStatus.PUBLISHED)
                .language(SupportedLanguage.vi)
                .level("Intermediate")
                .isCertificate(true)
                .previewVideoUrl("https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4")
                .averageRating(new BigDecimal("4.70"))
                .totalReviews(19)
                .totalEnrollments(97)
                .publishedAt(LocalDateTime.now().minusDays(3))
                .reviewedBy(reviewer)
                .build();
        course2 = courseRepository.save(course2);

        CourseSection course2Section1 = createSection(course2, 1, "Discovery & Research", "Hiểu người dùng và xác định vấn đề.");
        createLesson(course2Section1, 1, "Research plan", "Cách xây dựng kế hoạch research nhanh.", LessonType.LESSON, 14, true,
                "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", null, null);
        createLesson(course2Section1, 2, "Research template", "Mẫu interview script và summary.", LessonType.LESSON, 9, false,
                null, "Mẫu câu hỏi interview và template tổng hợp insight.", sampleDocumentUrl);
        updateSectionDuration(course2Section1);

        CourseSection course2Section2 = createSection(course2, 2, "Prototype & Handoff", "Thiết kế prototype và bàn giao dev.");
        createLesson(course2Section2, 1, "Prototype nhanh với Figma", "Thực hành flow và component.", LessonType.LESSON, 16, false,
                "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", null, null);
        createLesson(course2Section2, 2, "Checklist handoff", "Đảm bảo dev hiểu spec.", LessonType.LESSON, 7, false,
                null, "Checklist bàn giao thiết kế và QA UI.", sampleDocumentUrl);
        updateSectionDuration(course2Section2);

        updateCourseTotals(course2);
        }

        if (courseRepository.findBySlugAndDeletedAtIsNull("api-checklist-pack").isEmpty()) {
        Course course3 = Course.builder()
                .instructor(mentor1)
                .categoryId(devCategoryId)
                .title("API Checklist Pack for Backend Teams")
                .slug("api-checklist-pack")
                .description("Bộ tài liệu checklist, template và guideline để ship API chất lượng nhanh hơn.")
                .thumbnailUrl("https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=1200&q=80")
                .priceMxc(new BigDecimal("79.00"))
                .status(CourseStatus.PUBLISHED)
                .language(SupportedLanguage.vi)
                .level("Resource")
                .isCertificate(false)
                .averageRating(new BigDecimal("4.60"))
                .totalReviews(11)
                .totalEnrollments(64)
                .publishedAt(LocalDateTime.now().minusDays(1))
                .reviewedBy(reviewer)
                .build();
        course3 = courseRepository.save(course3);

        CourseSection course3Section1 = createSection(course3, 1, "Tài liệu cốt lõi", "Bộ file và guideline đi kèm.");
        createLesson(course3Section1, 1, "API design guideline", "Nguyên tắc thiết kế endpoint và naming.", LessonType.LESSON, 6, true,
                null, "Bộ guideline thiết kế API nhất quán cho team backend.", sampleDocumentUrl);
        createLesson(course3Section1, 2, "Checklist release", "Checklist trước khi release API.", LessonType.LESSON, 5, false,
                null, "Checklist kiểm tra security, logging, monitoring.", sampleDocumentUrl);
        updateSectionDuration(course3Section1);

        updateCourseTotals(course3);
        }

        if (courseRepository.findBySlugAndDeletedAtIsNull("data-storytelling-lab").isEmpty()) {
            Course course4 = Course.builder()
                    .instructor(userRepository.findByEmail("mentor3@mentorx.demo").orElseThrow())
                    .categoryId(dataCategoryId)
                    .title("Data Storytelling Lab for Product Dashboards")
                    .slug("data-storytelling-lab")
                    .description("Turn product metrics into clear decisions with dashboard framing and analytics communication.")
                    .thumbnailUrl("https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=1200&q=80")
                    .priceMxc(new BigDecimal("169.00"))
                    .status(CourseStatus.PUBLISHED)
                    .language(SupportedLanguage.en)
                    .level("Intermediate")
                    .isCertificate(true)
                    .averageRating(new BigDecimal("4.75"))
                    .totalReviews(17)
                    .totalEnrollments(88)
                    .publishedAt(LocalDateTime.now().minusDays(5))
                    .reviewedBy(reviewer)
                    .build();
            course4 = courseRepository.save(course4);

            CourseSection course4Section1 = createSection(course4, 1, "Metrics that matter", "Pick metrics, frame decisions, and avoid vanity reporting.");
            createLesson(course4Section1, 1, "Decision-first dashboards", "Build dashboard narratives that drive action.", LessonType.VIDEO, 15, true,
                    "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", null, null);
            createLesson(course4Section1, 2, "Dashboard note template", "Use a structured summary for stakeholders.", LessonType.ARTICLE, 11, false,
                    null, "Reusable summary template for dashboard insights and action notes.", sampleDocumentUrl);
            updateSectionDuration(course4Section1);
            updateCourseTotals(course4);
        }

        if (courseRepository.findBySlugAndDeletedAtIsNull("react-performance-clinic").isEmpty()) {
            Course course5 = Course.builder()
                    .instructor(userRepository.findByEmail("mentor4@mentorx.demo").orElseThrow())
                    .categoryId(devCategoryId)
                    .title("React Performance Clinic")
                    .slug("react-performance-clinic")
                    .description("Profile, trim, and restructure React apps for reliable production performance.")
                    .thumbnailUrl("https://images.unsplash.com/photo-1515879218367-8466d910aaa4?auto=format&fit=crop&w=1200&q=80")
                    .priceMxc(new BigDecimal("189.00"))
                    .status(CourseStatus.PUBLISHED)
                    .language(SupportedLanguage.en)
                    .level("Advanced")
                    .isCertificate(true)
                    .averageRating(new BigDecimal("4.82"))
                    .totalReviews(13)
                    .totalEnrollments(54)
                    .publishedAt(LocalDateTime.now().minusDays(6))
                    .reviewedBy(reviewer)
                    .build();
            course5 = courseRepository.save(course5);

            CourseSection course5Section1 = createSection(course5, 1, "Find real bottlenecks", "Use measurement before optimization.");
            createLesson(course5Section1, 1, "Profiler workflow", "Measure render cost and interaction lag.", LessonType.VIDEO, 17, true,
                    "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", null, null);
            createLesson(course5Section1, 2, "Performance checklist", "A repeatable list for state shape, rendering, and async UI coordination.", LessonType.ARTICLE, 8, false,
                    null, "Checklist for state shape, network coordination, and render hot paths.", sampleDocumentUrl);
            updateSectionDuration(course5Section1);
            updateCourseTotals(course5);
        }

        if (courseRepository.findBySlugAndDeletedAtIsNull("career-growth-toolkit").isEmpty()) {
            Course course6 = Course.builder()
                    .instructor(userRepository.findByEmail("mentor6@mentorx.demo").orElseThrow())
                    .categoryId(businessCategoryId)
                    .title("Career Growth Toolkit for Tech Professionals")
                    .slug("career-growth-toolkit")
                    .description("Improve communication, interview readiness, and promotion planning with concrete weekly exercises.")
                    .thumbnailUrl("https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=1200&q=80")
                    .priceMxc(new BigDecimal("129.00"))
                    .status(CourseStatus.PUBLISHED)
                    .language(SupportedLanguage.vi)
                    .level("Beginner")
                    .isCertificate(false)
                    .averageRating(new BigDecimal("4.90"))
                    .totalReviews(21)
                    .totalEnrollments(120)
                    .publishedAt(LocalDateTime.now().minusDays(2))
                    .reviewedBy(reviewer)
                    .build();
            course6 = courseRepository.save(course6);

            CourseSection course6Section1 = createSection(course6, 1, "Communicate clearly", "Write and speak with structure in team settings.");
            createLesson(course6Section1, 1, "Status update structure", "Keep updates concrete and easy to act on.", LessonType.VIDEO, 12, true,
                    "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", null, null);
            createLesson(course6Section1, 2, "Interview answer worksheet", "Document stories for interviews and promotion packets.", LessonType.ARTICLE, 9, false,
                    null, "Worksheet for project stories, tradeoffs, and measurable impact.", sampleDocumentUrl);
            updateSectionDuration(course6Section1);
            updateCourseTotals(course6);
        }
    }

    private CourseSection createSection(Course course, int order, String title, String description) {
        CourseSection section = new CourseSection();
        section.setCourse(course);
        section.setTitle(title);
        section.setDescription(description);
        section.setSectionOrder(order);
        section.setIsPublished(true);
        return courseSectionRepository.save(section);
    }

    private CourseLesson createLesson(
            CourseSection section,
            int order,
            String title,
            String description,
            LessonType lessonType,
            Integer durationMinutes,
            boolean isFreePreview,
            String videoUrl,
            String articleContent,
            String resourceUrl
    ) {
        CourseLesson lesson = CourseLesson.builder()
                .section(section)
                .title(title)
                .description(description)
                .lessonType(normalizeLessonTypeForWrite(lessonType))
                .lessonOrder(order)
                .durationMinutes(durationMinutes)
                .videoUrl(videoUrl)
                .articleContent(articleContent)
                .resourceUrl(resourceUrl)
                .isFreePreview(isFreePreview)
                .isPublished(true)
                .isMandatory(true)
                .build();
        return courseLessonRepository.save(lesson);
    }

    private LessonType normalizeLessonTypeForWrite(LessonType lessonType) {
        return LessonType.QUIZ.equals(lessonType) ? LessonType.QUIZ : LessonType.LESSON;
    }

    private void updateSectionDuration(CourseSection section) {
        List<CourseLesson> lessons = courseLessonRepository.findBySectionIdOrderByLessonOrderAsc(section.getId());
        int duration = lessons.stream()
                .mapToInt(lesson -> lesson.getDurationMinutes() != null ? lesson.getDurationMinutes() : 0)
                .sum();
        section.setDurationMinutes(duration);
        courseSectionRepository.save(section);
    }

    private void updateCourseTotals(Course course) {
        List<CourseLesson> lessons = courseLessonRepository.findAllByCourseId(course.getId());
        int totalDuration = lessons.stream()
                .mapToInt(lesson -> lesson.getDurationMinutes() != null ? lesson.getDurationMinutes() : 0)
                .sum();
        course.setTotalDurationMin(totalDuration);
        course.setTotalLessons((short) lessons.size());
        courseRepository.save(course);
    }

    private String ensureSampleDocument(String baseName, String title) {
        String fileName = baseName + ".pdf";
        try {
            Path uploadDir = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            Path filePath = uploadDir.resolve(fileName).normalize();
            if (!Files.exists(filePath)) {
                createSamplePdf(filePath, title);
            }
        } catch (Exception ex) {
            log.warn("Failed to create sample document", ex);
        }

        return "/uploads/" + fileName;
    }

    private void createSamplePdf(Path filePath, String title) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.beginText();
                contentStream.newLineAtOffset(72, 720);
                contentStream.showText(title);
                contentStream.endText();

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(72, 680);
                contentStream.showText("This is a sample PDF generated for MentorX demo content.");
                contentStream.endText();
            }

            document.save(filePath.toFile());
        }
    }

    private void assignRoleToUser(User user, String roleName) {
        Role role = roleRepository.findByRoleName(roleName).orElseThrow();
        userRoleRepository.save(UserRole.builder()
                .userId(user.getId())
                .roleId(role.getId())
                .grantedAt(LocalDateTime.now())
                .build());
    }

    private void assignRoleToUserIfMissing(User user, String roleName) {
        Role role = roleRepository.findByRoleName(roleName).orElseThrow();
        if (userRoleRepository.existsByUserIdAndRoleId(user.getId(), role.getId())) {
            return;
        }

        userRoleRepository.save(UserRole.builder()
                .userId(user.getId())
                .roleId(role.getId())
                .grantedAt(LocalDateTime.now())
                .build());
    }

    private void setupUserAccount(User user) {
        ensureSingleWalletAccount(user, WalletAccountType.USER_AVAILABLE, new BigDecimal("1000.00"));
        ensureSingleWalletAccount(user, WalletAccountType.USER_PENDING, BigDecimal.ZERO);

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

    private void setupUserAccountIfMissing(User user) {
        ensureSingleWalletAccount(user, WalletAccountType.USER_AVAILABLE, new BigDecimal("1000.00"));
        ensureSingleWalletAccount(user, WalletAccountType.USER_PENDING, BigDecimal.ZERO);

        if (!notificationPreferenceRepository.existsByUserId(user.getId())) {
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

    private void ensureSingleWalletAccount(User user, WalletAccountType accountType, BigDecimal initialBalance) {
        List<Wallet> wallets = new ArrayList<>(walletRepository.findAllByUserIdAndAccountType(user.getId(), accountType));
        if (wallets.isEmpty()) {
            walletRepository.save(Wallet.builder()
                    .user(user)
                    .accountType(accountType)
                    .balanceMxc(initialBalance)
                    .build());
            return;
        }

        Wallet primary = wallets.get(0);
        primary.setUser(user);
        primary.setAccountType(accountType);
        if (primary.getBalanceMxc() == null) {
            primary.setBalanceMxc(initialBalance);
        }
        walletRepository.save(primary);

        if (wallets.size() > 1) {
            walletRepository.deleteAll(wallets.subList(1, wallets.size()));
        }
    }

    private void seedJobs() {
        if (jobRepository.count() > 0) {
            return;
        }

        User client = userRepository.findByEmail("client1@mentorx.demo")
                .orElseGet(() -> {
                    User user = createUser("client1@mentorx.demo", "Công ty ABC", "ABC Company", UserStatus.ACTIVE, false, MentorStatus.NONE);
                    assignRoleToUserIfMissing(user, "USER");
                    setupUserAccountIfMissing(user);
                    return user;
                });

        Integer softwareCategoryId = categoryRepository.findBySlug("software-dev").map(Category::getId).orElse(null);
        Integer dataCategoryId = categoryRepository.findBySlug("data-ai").map(Category::getId).orElse(null);
        Integer designCategoryId = categoryRepository.findBySlug("design").map(Category::getId).orElse(null);
        Integer businessCategoryId = categoryRepository.findBySlug("business-finance").map(Category::getId).orElse(null);
        LocalDateTime now = LocalDateTime.now();

        jobRepository.saveAll(List.of(
                Job.builder()
                        .client(client)
                        .categoryId(dataCategoryId)
                        .jobType(JobType.FREELANCE_PROJECT)
                        .title("Cần giúp làm bài tập LAB môn Học Máy - Classification ảnh")
                        .description("Mình đang học môn Học Máy (Machine Learning) ngành AI. Thầy giao bài tập LAB yêu cầu xây dựng mô hình classification ảnh trên tập dữ liệu CIFAR-10. Mình đã code được phần load dữ liệu nhưng đang bị overfitting, accuracy chỉ đạt ~60%. Cần mentor hướng dẫn cách cải thiện model (data augmentation, điều chỉnh architecture, regularization) và giải thích lý thuyết để mình hiểu bản chất.")
                        .budgetType(BudgetType.FIXED)
                        .budgetMinMxc(new BigDecimal("300.00"))
                        .budgetMaxMxc(new BigDecimal("600.00"))
                        .deadlineAt(now.plusDays(7))
                        .status(JobStatus.OPEN)
                        .isFeatured(true)
                        .proposalCount(6)
                        .publishedAt(now.minusDays(2))
                        .learningGoals("Hiểu về CNN architecture, data augmentation, regularization techniques")
                        .successCriteria("Accuracy trên test set đạt >80%, code chạy được, hiểu và giải thích được từng bước")
                        .requiredSkills(List.of("Python", "TensorFlow/PyTorch", "Deep Learning", "Xử lý ảnh"))
                        .experienceLevel("INTERMEDIATE")
                        .build(),
                Job.builder()
                        .client(client)
                        .categoryId(softwareCategoryId)
                        .jobType(JobType.QUICK_FIX)
                        .title("Lỗi HikariCP Connection Pool trong Spring Boot khi deploy lên server")
                        .description("Mình deploy Spring Boot app lên VPS, sau một thời gian hoạt động thì bị lỗi 'HikariPool-1 - Connection is not available, request timed out after 30000ms'. Mình đã thử tăng maximum-pool-size nhưng vẫn bị. Cần mentor giúp debug nguyên nhân gốc rễ: kiểm tra cấu hình datasource, phát hiện connection leak, tối ưu pool settings và hướng dẫn cách monitor connection pool trong production.")
                        .budgetType(BudgetType.HOURLY)
                        .hourlyRateMxc(new BigDecimal("150.00"))
                        .estimatedHours(new BigDecimal("4.00"))
                        .deadlineAt(now.plusDays(3))
                        .status(JobStatus.OPEN)
                        .isFeatured(true)
                        .proposalCount(4)
                        .publishedAt(now.minusDays(1))
                        .requiredSkills(List.of("Java", "Spring Boot", "HikariCP", "PostgreSQL"))
                        .learningGoals("Hiểu cách HikariCP hoạt động, biết cách debug connection leak, cấu hình production-ready")
                        .successCriteria("Server chạy ổn định >48h không lỗi, có script monitor pool")
                        .build(),
                Job.builder()
                        .client(client)
                        .categoryId(designCategoryId)
                        .jobType(JobType.LONG_TERM_MENTORING)
                        .title("Cần mentor thiết kế UI/UX cho web bán hàng (dự án cá nhân)")
                        .description("Mình đang tự làm một web bán hàng thời trang để hoàn thiện portfolio. Mình tự học code nên UI rất xấu, bố cục lộn xộn, không biết sắp xếp thông tin sản phẩm thế nào cho hợp lý. Cần mentor hướng dẫn về UX research, wireframing, design system, màu sắc, typography. Mỗi tuần 1-2 buổi online để review design và đưa ra góp ý cải thiện.")
                        .budgetType(BudgetType.FIXED)
                        .budgetMinMxc(new BigDecimal("500.00"))
                        .budgetMaxMxc(new BigDecimal("1200.00"))
                        .deadlineAt(now.plusDays(30))
                        .status(JobStatus.OPEN)
                        .isFeatured(false)
                        .proposalCount(3)
                        .publishedAt(now.minusDays(3))
                        .learningGoals("Nắm được quy trình thiết kế UI/UX cơ bản, biết dùng Figma, hiểu về design system")
                        .successCriteria("Hoàn thiện được bộ mockup 5 trang chính: Home, Product List, Product Detail, Cart, Checkout")
                        .requiredSkills(List.of("UI/UX Design", "Figma", "Thiết kế Web", "Design System"))
                        .experienceLevel("BEGINNER")
                        .currentLevel(com.mentorx.api.common.enums.UserLevel.BEGINNER)
                        .build(),
                Job.builder()
                        .client(client)
                        .categoryId(softwareCategoryId)
                        .jobType(JobType.QUICK_FIX)
                        .title("Cần ai đó giải thích thuật toán Binary Search Tree - bài tập Cấu trúc dữ liệu")
                        .description("Mình học môn Cấu trúc dữ liệu & Giải thuật, đang làm assignment về Binary Search Tree (BST) với các thao tác insert, delete, search, và duyệt cây theo inorder/preorder/postorder. Code của mình chạy được insert và search nhưng delete bị sai, với cả mình không hiểu cách duyệt cây đệ quy hoạt động thế nào. Cần mentor giải thích và debug giúp.")
                        .budgetType(BudgetType.HOURLY)
                        .hourlyRateMxc(new BigDecimal("120.00"))
                        .estimatedHours(new BigDecimal("3.00"))
                        .deadlineAt(now.plusDays(5))
                        .status(JobStatus.OPEN)
                        .isFeatured(true)
                        .proposalCount(5)
                        .publishedAt(now.minusDays(4))
                        .requiredSkills(List.of("Java/C++", "Cấu trúc dữ liệu", "Binary Search Tree"))
                        .learningGoals("Hiểu sâu về BST, đệ quy, các thuật toán duyệt cây")
                        .successCriteria("Code delete hoạt động đúng, giải thích được bằng lời từng bước duyệt cây")
                        .build()
        ));

        createJobIfMissing(ensureDemoUser("client2@mentorx.demo", "XYZ Startup", "XYZ Startup", false, MentorStatus.NONE),
                dataCategoryId, JobType.QUICK_FIX, "Truy vấn SQL báo cáo doanh thu chạy rất chậm - cần tối ưu",
                "Mình làm báo cáo doanh thu theo tháng, query JOIN 5 bảng với hơn 1 triệu records. Mỗi lần chạy mất 30-40 giây. Cần mentor giúp phân tích EXPLAIN, tối ưu index, viết lại query cho hiệu quả hơn. Mình biết SQL cơ bản nhưng chưa rành về query optimization.",
                BudgetType.HOURLY, null, null, new BigDecimal("180.00"), new BigDecimal("5.00"),
                now.plusDays(4), JobStatus.OPEN, true, now.minusDays(2));
        createJobIfMissing(ensureDemoUser("client2@mentorx.demo", "XYZ Startup", "XYZ Startup", false, MentorStatus.NONE),
                softwareCategoryId, JobType.FREELANCE_PROJECT, "Cần mentor React Redux Toolkit - state management phức tạp",
                "Mình đang làm web dashboard với React, dữ liệu có nhiều dependency chéo, các component cần share state phức tạp. Mình dùng Context API đang bị re-render nhiều quá, app chậm dần. Cần mentor hướng dẫn chuyển sang Redux Toolkit: thiết kế store, slices, async thunks, và best practices.",
                BudgetType.FIXED, new BigDecimal("400.00"), new BigDecimal("900.00"), null, null,
                now.plusDays(14), JobStatus.OPEN, false, now.minusDays(5));
        createJobIfMissing(ensureDemoUser("client3@mentorx.demo", "DEF Enterprise", "DEF Enterprise", false, MentorStatus.NONE),
                dataCategoryId, JobType.LONG_TERM_MENTORING, "Cần mentor học Machine Learning từ cơ bản đến ứng dụng",
                "Mình là sinh viên năm 3 ngành CNTT, muốn theo hướng AI/ML. Đã biết Python cơ bản. Cần lộ trình học bài bản: từ linear regression, decision trees, neural networks đến các thư viện scikit-learn, TensorFlow. Mong mentor hướng dẫn hàng tuần, giao bài tập và review code.",
                BudgetType.FIXED, new BigDecimal("600.00"), new BigDecimal("1500.00"), null, null,
                now.plusDays(45), JobStatus.OPEN, false, now.minusDays(3));
        createJobIfMissing(ensureDemoUser("client3@mentorx.demo", "DEF Enterprise", "DEF Enterprise", false, MentorStatus.NONE),
                dataCategoryId, JobType.QUICK_FIX, "Cần giúp deploy model ML lên production với FastAPI",
                "Mình đã train xong model NLP classification, muốn deploy thành API để app mobile gọi. Chưa biết cách dùng FastAPI để serve model, optimize inference speed, và xử lý request lớn. Cần mentor hướng dẫn từ A-Z: tạo API endpoint, load model, preprocessing input, testing với Locust.",
                BudgetType.HOURLY, null, null, new BigDecimal("250.00"), new BigDecimal("8.00"),
                now.plusDays(7), JobStatus.OPEN, false, now.minusDays(1));
        createJobIfMissing(ensureDemoUser("client4@mentorx.demo", "Northwind Labs", "Northwind Labs", false, MentorStatus.NONE),
                softwareCategoryId, JobType.LONG_TERM_MENTORING, "Mentoring lộ trình học React cho người đã biết JavaScript",
                "Mình đã làm việc với JavaScript được 2 năm (chủ yếu jQuery và một ít Vue). Giờ muốn chuyển sang React để có cơ hội việc tốt hơn. Cần mentor xây dựng lộ trình, hướng dẫn từ React basics, hooks, routing, đến testing và deployment. Mỗi tuần 1-2 buổi.",
                BudgetType.FIXED, new BigDecimal("800.00"), new BigDecimal("1800.00"), null, null,
                now.plusDays(35), JobStatus.OPEN, true, now.minusDays(6));
        createJobIfMissing(ensureDemoUser("client4@mentorx.demo", "Northwind Labs", "Northwind Labs", false, MentorStatus.NONE),
                softwareCategoryId, JobType.FREELANCE_PROJECT, "Viết Unit Test cho code Java Spring Boot - Test Coverage < 10%",
                "Dự án Spring Boot của team mình không có test, sắp tới cần release major update. Sếp yêu cầu tăng test coverage lên >70%. Cần mentor hướng dẫn: viết JUnit + Mockito cho Controller/Service/Repository, integration test với TestContainers, và thiết lập CI pipeline chạy test tự động.",
                BudgetType.FIXED, new BigDecimal("500.00"), new BigDecimal("1100.00"), null, null,
                now.plusDays(20), JobStatus.OPEN, false, now.minusDays(2));
        createJobIfMissing(ensureDemoUser("client5@mentorx.demo", "BluePeak Studio", "BluePeak Studio", false, MentorStatus.NONE),
                softwareCategoryId, JobType.QUICK_FIX, "Lỗi CORS và REST API Spring Boot không trả về JSON đúng format",
                "Frontend React của mình gọi API Spring Boot bị lỗi CORS. Mình đã config @CrossOrigin nhưng vẫn bị. Ngoài ra response JSON trả về có field null bị thiếu, frontend báo lỗi. Cần mentor giúp cấu hình CORS đúng, dùng Jackson annotations để kiểm soát JSON output.",
                BudgetType.FIXED, new BigDecimal("150.00"), new BigDecimal("350.00"), null, null,
                now.plusDays(2), JobStatus.OPEN, true, now.minusDays(4));
        createJobIfMissing(ensureDemoUser("client5@mentorx.demo", "BluePeak Studio", "BluePeak Studio", false, MentorStatus.NONE),
                softwareCategoryId, JobType.FREELANCE_PROJECT, "Tối ưu performance website React bị lag khi render danh sách lớn",
                "Web dashboard của mình hiển thị bảng 5000+ dòng dữ liệu, kéo xuống bị giật, filter/sort chậm. Đã thử React.memo nhưng không cải thiện nhiều. Cần mentor phân tích nguyên nhân (bottleneck ở component nào?) và đề xuất giải pháp: virtualization, useMemo, code splitting phù hợp.",
                BudgetType.FIXED, new BigDecimal("350.00"), new BigDecimal("750.00"), null, null,
                now.plusDays(10), JobStatus.OPEN, false, now.minusDays(3));
        createJobIfMissing(ensureDemoUser("client6@mentorx.demo", "Mekong Analytics", "Mekong Analytics", false, MentorStatus.NONE),
                softwareCategoryId, JobType.QUICK_FIX, "Cần review code Python ETL pipeline bị lỗi khi xử lý file lớn",
                "Mình viết Python script đọc file CSV 2GB, transform dữ liệu rồi ghi vào database. Script chạy được với file nhỏ nhưng với file lớn bị memory error và timeout. Cần mentor review code, đề xuất xử lý theo từng chunk, dùng pandas hiệu quả hơn và logging lỗi đúng cách.",
                BudgetType.HOURLY, null, null, new BigDecimal("160.00"), new BigDecimal("4.00"),
                now.plusDays(5), JobStatus.OPEN, false, now.minusDays(3));
        createJobIfMissing(ensureDemoUser("client6@mentorx.demo", "Mekong Analytics", "Mekong Analytics", false, MentorStatus.NONE),
                softwareCategoryId, JobType.LONG_TERM_MENTORING, "Mentoring Docker & Docker Compose cho microservices",
                "Team mình chuyển sang microservices, có 6 services cần chạy cùng lúc. Chưa ai biết Docker. Cần mentor hướng dẫn: viết Dockerfile cho từng service, docker-compose orchestration, networking, volumes, environment variables, và CI/CD tích hợp Docker.",
                BudgetType.FIXED, new BigDecimal("700.00"), new BigDecimal("1600.00"), null, null,
                now.plusDays(21), JobStatus.OPEN, false, now.minusDays(3));
    }

    private void seedProposals() {
        List<Job> openJobs = jobRepository.findOpen(PageRequest.of(0, 50)).getContent();
        Optional<Job> mlLabJob = openJobs.stream()
                .filter(job -> "Cần giúp làm bài tập LAB môn Học Máy - Classification ảnh".equals(job.getTitle()))
                .findFirst();
        Optional<Job> hikariCpJob = openJobs.stream()
                .filter(job -> "Lỗi HikariCP Connection Pool trong Spring Boot khi deploy lên server".equals(job.getTitle()))
                .findFirst();
        Optional<Job> uiUxMentorJob = openJobs.stream()
                .filter(job -> "Cần mentor thiết kế UI/UX cho web bán hàng (dự án cá nhân)".equals(job.getTitle()))
                .findFirst();
        Optional<Job> bstJob = openJobs.stream()
                .filter(job -> "Cần ai đó giải thích thuật toán Binary Search Tree - bài tập Cấu trúc dữ liệu".equals(job.getTitle()))
                .findFirst();
        Optional<Job> sqlOptimizeJob = openJobs.stream()
                .filter(job -> "Truy vấn SQL báo cáo doanh thu chạy rất chậm - cần tối ưu".equals(job.getTitle()))
                .findFirst();
        Optional<Job> reduxJob = openJobs.stream()
                .filter(job -> "Cần mentor React Redux Toolkit - state management phức tạp".equals(job.getTitle()))
                .findFirst();
        Optional<Job> mlMentorJob = openJobs.stream()
                .filter(job -> "Cần mentor học Machine Learning từ cơ bản đến ứng dụng".equals(job.getTitle()))
                .findFirst();
        Optional<Job> fastapiJob = openJobs.stream()
                .filter(job -> "Cần giúp deploy model ML lên production với FastAPI".equals(job.getTitle()))
                .findFirst();
        Optional<Job> reactMentorJob = openJobs.stream()
                .filter(job -> "Mentoring lộ trình học React cho người đã biết JavaScript".equals(job.getTitle()))
                .findFirst();
        Optional<Job> unitTestJob = openJobs.stream()
                .filter(job -> "Viết Unit Test cho code Java Spring Boot - Test Coverage < 10%".equals(job.getTitle()))
                .findFirst();
        Optional<Job> corsJob = openJobs.stream()
                .filter(job -> "Lỗi CORS và REST API Spring Boot không trả về JSON đúng format".equals(job.getTitle()))
                .findFirst();
        Optional<Job> reactPerfJob = openJobs.stream()
                .filter(job -> "Tối ưu performance website React bị lag khi render danh sách lớn".equals(job.getTitle()))
                .findFirst();
        Optional<Job> etlJob = openJobs.stream()
                .filter(job -> "Cần review code Python ETL pipeline bị lỗi khi xử lý file lớn".equals(job.getTitle()))
                .findFirst();
        Optional<Job> dockerJob = openJobs.stream()
                .filter(job -> "Mentoring Docker & Docker Compose cho microservices".equals(job.getTitle()))
                .findFirst();

        User mentor1 = userRepository.findByEmail("mentor1@mentorx.demo").orElse(null);
        User mentor2 = userRepository.findByEmail("mentor2@mentorx.demo").orElse(null);
        User mentor3 = userRepository.findByEmail("mentor3@mentorx.demo").orElse(null);
        User mentor4 = userRepository.findByEmail("mentor4@mentorx.demo").orElse(null);
        User mentor5 = userRepository.findByEmail("mentor5@mentorx.demo").orElse(null);
        User mentor6 = userRepository.findByEmail("mentor6@mentorx.demo").orElse(null);

        mlLabJob.ifPresent(job -> {
            seedProposal(job, mentor3,
                    "Mình chuyên về Deep Learning và Computer Vision. Có thể giúp bạn debug overfitting, thiết kế CNN architecture phù hợp với CIFAR-10, và hướng dẫn data augmentation. Sẽ giải thích chi tiết từng bước để bạn hiểu bản chất chứ không chỉ chạy code.",
                    new BigDecimal("480.00"),
                    null,
                    5,
                    "5 năm kinh nghiệm trong ML/DL, từng làm nhiều dự án image classification, am hiểu PyTorch và TensorFlow.",
                    new BigDecimal("95.00"),
                    ProposalStatus.SUBMITTED);
            seedProposal(job, mentor5,
                    "Có thể hỗ trợ bạn về phần regularization, tuning hyperparameters. Mình sẽ review code hiện tại và đề xuất cải tiến cụ thể, đồng thời giải thích các khái niệm như dropout, batch normalization.",
                    new BigDecimal("380.00"),
                    null,
                    5,
                    "Data Scientist với 3 năm kinh nghiệm, từng mentor cho sinh viên làm đồ án ML.",
                    new BigDecimal("82.00"),
                    ProposalStatus.SUBMITTED);
            updateJobProposalCount(job);
        });

        hikariCpJob.ifPresent(job -> {
            seedProposal(job, mentor1,
                    "Mình đã từng gặp và fix lỗi này nhiều lần. Nguyên nhân thường là connection leak do không đóng PreparedStatement hoặc transaction không commit. Mình sẽ giúp bạn kiểm tra code, cấu hình HikariCP production-ready, và setup monitoring với Micrometer + Grafana.",
                    new BigDecimal("180.00"),
                    new BigDecimal("150.00"),
                    2,
                    "8 năm Java/Spring, từng vận hành hệ thống phục vụ 10K+ concurrent users.",
                    new BigDecimal("96.00"),
                    ProposalStatus.SUBMITTED);
            seedProposal(job, mentor4,
                    "Mình có thể debug qua teamview và chỉ ra chỗ leak connection. Đồng thời hướng dẫn cấu hình connection pool cho phù hợp với resource VPS.",
                    new BigDecimal("150.00"),
                    new BigDecimal("150.00"),
                    1,
                    "Full-stack developer, từng xử lý performance issue cho nhiều Spring Boot app trên VPS.",
                    new BigDecimal("85.00"),
                    ProposalStatus.SUBMITTED);
            updateJobProposalCount(job);
        });

        uiUxMentorJob.ifPresent(job -> {
            seedProposal(job, mentor2,
                    "Mình sẽ hướng dẫn bạn quy trình thiết kế UI/UX từ A-Z: research, wireframe, prototype đến handoff. Tập trung vào e-commerce UX patterns, sắp xếp thông tin sản phẩm, tối ưu conversion. Mỗi tuần 1 buổi review design của bạn và đưa ra góp ý cụ thể trên Figma.",
                    new BigDecimal("1000.00"),
                    null,
                    28,
                    "6 năm UI/UX design, từng thiết kế cho 5+ e-commerce platform, thạo Figma và design system.",
                    new BigDecimal("93.00"),
                    ProposalStatus.SUBMITTED);
            seedProposal(job, mentor4,
                    "Mình có thể hỗ trợ bạn cả về UI lẫn frontend implementation. Sẽ hướng dẫn bạn biến design thành code React component chuẩn chỉnh.",
                    new BigDecimal("850.00"),
                    null,
                    30,
                    "Frontend developer kiêm UI designer, từng xây dựng web bán hàng hoàn chỉnh.",
                    new BigDecimal("80.00"),
                    ProposalStatus.SUBMITTED);
            updateJobProposalCount(job);
        });

        bstJob.ifPresent(job -> {
            seedProposal(job, mentor4,
                    "Mình sẽ giải thích BST và đệ quy bằng hình ảnh trực quan, debug code delete của bạn step-by-step. Sau đó hướng dẫn các cách duyệt cây và ứng dụng thực tế. Guarantee bạn sẽ hiểu sau 1 buổi.",
                    new BigDecimal("100.00"),
                    new BigDecimal("120.00"),
                    2,
                    "Từng làm TA môn Cấu trúc dữ liệu 2 năm, giải thích dễ hiểu cho sinh viên.",
                    new BigDecimal("90.00"),
                    ProposalStatus.SUBMITTED);
            updateJobProposalCount(job);
        });

        sqlOptimizeJob.ifPresent(job -> {
            seedProposal(job, mentor3,
                    "Mình sẽ phân tích EXPLAIN plan từng query, tối ưu index và viết lại JOIN cho hiệu quả. Có thể giảm thời gian truy vấn từ 40s xuống <1s. Hướng dẫn bạn cách đọc EXPLAIN và thiết kế index phù hợp với business logic.",
                    new BigDecimal("180.00"),
                    new BigDecimal("180.00"),
                    3,
                    "7 năm data engineering, chuyên về SQL optimization cho hệ thống báo cáo hàng triệu records.",
                    new BigDecimal("94.00"),
                    ProposalStatus.SUBMITTED);
            updateJobProposalCount(job);
        });

        reduxJob.ifPresent(job -> {
            seedProposal(job, mentor4,
                    "Mình sẽ giúp bạn chuyển từ Context API sang Redux Toolkit đúng cách. Thiết kế store structure, phân tách slices, dùng createAsyncThunk cho API calls, và áp dụng best practices để tránh unnecessary re-renders. Có thể làm việc trực tiếp trên codebase của bạn.",
                    new BigDecimal("750.00"),
                    null,
                    12,
                    "5 năm React, từng build nhiều dashboard phức tạp với Redux Toolkit và TypeScript.",
                    new BigDecimal("95.00"),
                    ProposalStatus.SUBMITTED);
            seedProposal(job, mentor1,
                    "Mình có thể hỗ trợ phần API design để state management phía frontend được đơn giản hơn. Backend trả về dữ liệu đúng cấu trúc sẽ giảm đáng kể complexity cho Redux.",
                    new BigDecimal("650.00"),
                    null,
                    14,
                    "Full-stack developer, hiểu cả backend và frontend, có thể tư vấn end-to-end.",
                    new BigDecimal("84.00"),
                    ProposalStatus.SUBMITTED);
            updateJobProposalCount(job);
        });

        mlMentorJob.ifPresent(job -> {
            seedProposal(job, mentor3,
                    "Mình sẽ xây dựng lộ trình học ML bài bản cho bạn, từ toán nền tảng đến các model thực tế. Mỗi tuần giao bài tập code, review và giải thích lý thuyết. Đảm bảo sau 6 tuần bạn có thể tự tin làm project ML cơ bản.",
                    new BigDecimal("1200.00"),
                    null,
                    42,
                    "Data Scientist 5 năm, từng dạy ML cho hơn 100 sinh viên tại các trường ĐH.",
                    new BigDecimal("96.00"),
                    ProposalStatus.SUBMITTED);
            seedProposal(job, mentor1,
                    "Mình có thể hỗ trợ bạn phần lập trình Python và tích hợp ML model vào ứng dụng thực tế. Kết hợp kiến thức backend để bạn thấy ML được áp dụng trong sản phẩm thế nào.",
                    new BigDecimal("900.00"),
                    null,
                    45,
                    "Backend developer có kinh nghiệm tích hợp ML models vào production systems.",
                    new BigDecimal("82.00"),
                    ProposalStatus.SUBMITTED);
            updateJobProposalCount(job);
        });

        fastapiJob.ifPresent(job -> {
            seedProposal(job, mentor3,
                    "Mình sẽ hướng dẫn bạn deploy ML model với FastAPI từ đầu: tạo endpoint predict, load model hiệu quả, preprocessing input, tối ưu inference với batching, và load testing với Locust. Có thể deploy lên AWS/GCP nếu bạn cần.",
                    new BigDecimal("500.00"),
                    new BigDecimal("250.00"),
                    5,
                    "Đã deploy 10+ ML models lên production, thành thạo FastAPI, Docker, và cloud services.",
                    new BigDecimal("93.00"),
                    ProposalStatus.SUBMITTED);
            updateJobProposalCount(job);
        });

        reactMentorJob.ifPresent(job -> {
            seedProposal(job, mentor4,
                    "Mình sẽ thiết kế lộ trình React phù hợp với background JavaScript của bạn. Bắt đầu từ React basics, functional components, hooks, đến routing, state management, testing, và deployment. Mỗi tuần 1-2 buổi + bài tập thực hành.",
                    new BigDecimal("1400.00"),
                    null,
                    30,
                    "5 năm React, từng chuyển đổi team từ Vue sang React, mentor cho 20+ junior devs.",
                    new BigDecimal("95.00"),
                    ProposalStatus.SUBMITTED);
            seedProposal(job, mentor2,
                    "Mình có thể hỗ trợ bạn về UI/React component design song song với lộ trình học. Giúp bạn viết component sạch, dễ bảo trì, đúng chuẩn UI/UX.",
                    new BigDecimal("1100.00"),
                    null,
                    35,
                    "UI/UX designer có kinh nghiệm làm việc với React teams, biết chuyển design thành component.",
                    new BigDecimal("86.00"),
                    ProposalStatus.SUBMITTED);
            updateJobProposalCount(job);
        });

        unitTestJob.ifPresent(job -> {
            seedProposal(job, mentor1,
                    "Mình sẽ hướng dẫn team bạn viết test hiệu quả với JUnit 5 + Mockito, Integration Test với TestContainers, và thiết lập CI pipeline chạy test tự động. Có thể review code hiện tại và đề xuất strategy test phù hợp.",
                    new BigDecimal("900.00"),
                    null,
                    18,
                    "8 năm Java, từng đưa test coverage từ 5% lên 85% cho dự án Spring Boot 50+ services.",
                    new BigDecimal("96.00"),
                    ProposalStatus.SUBMITTED);
            seedProposal(job, mentor4,
                    "Mình hỗ trợ về phần test React component (Jest + React Testing Library) nếu frontend cũng cần. Có thể kết hợp để đảm bảo full-stack test coverage.",
                    new BigDecimal("700.00"),
                    null,
                    20,
                    "Full-stack developer với kinh nghiệm unit test cả frontend lẫn backend.",
                    new BigDecimal("84.00"),
                    ProposalStatus.SUBMITTED);
            updateJobProposalCount(job);
        });

        corsJob.ifPresent(job -> {
            seedProposal(job, mentor1,
                    "Mình sẽ fix lỗi CORS ngay lập tức. Vấn đề thường là @CrossOrigin không đủ, cần cấu hình CORS global trong SecurityConfig. Đồng thời hướng dẫn bạn dùng @JsonInclude và Jackson annotations để kiểm soát JSON response, xử lý null fields đúng cách.",
                    new BigDecimal("300.00"),
                    null,
                    2,
                    "Spring Boot expert, đã xử lý CORS và JSON serialization issues cho nhiều dự án.",
                    new BigDecimal("97.00"),
                    ProposalStatus.SUBMITTED);
            seedProposal(job, mentor4,
                    "Mình có thể fix cả frontend lẫn backend để CORS hoạt động. Đồng thời hướng dẫn cấu hình axios interceptors cho đồng bộ.",
                    new BigDecimal("250.00"),
                    null,
                    2,
                    "Full-stack developer, quen thuộc với cả Spring Boot CORS config và React axios setup.",
                    new BigDecimal("88.00"),
                    ProposalStatus.SUBMITTED);
            updateJobProposalCount(job);
        });

        reactPerfJob.ifPresent(job -> {
            seedProposal(job, mentor4,
                    "Mình sẽ profile ứng dụng React của bạn để tìm bottlenecks, sau đó implement virtualization với react-window, tối ưu re-renders với useMemo/useCallback, và code splitting với React.lazy. Cam kết cải thiện performance rõ rệt.",
                    new BigDecimal("650.00"),
                    null,
                    8,
                    "React performance specialist, đã tối ưu dashboard với 50K+ rows cho công ty fintech.",
                    new BigDecimal("95.00"),
                    ProposalStatus.SUBMITTED);
            seedProposal(job, mentor1,
                    "Mình hỗ trợ tối ưu phía API để giảm lượng dữ liệu trả về, implement pagination và caching, giúp frontend nhẹ hơn.",
                    new BigDecimal("500.00"),
                    null,
                    10,
                    "Backend optimization specialist, từng tối ưu API response time từ 5s xuống 200ms.",
                    new BigDecimal("87.00"),
                    ProposalStatus.SUBMITTED);
            updateJobProposalCount(job);
        });

        etlJob.ifPresent(job -> {
            seedProposal(job, mentor3,
                    "Mình sẽ review code Python pipeline của bạn và đề xuất giải pháp xử lý file lớn theo chunk, dùng pandas với chunksize, tối ưu memory usage, thêm logging và error handling. Có thể refactor thành module dễ bảo trì.",
                    new BigDecimal("160.00"),
                    new BigDecimal("160.00"),
                    3,
                    "Data engineer 5 năm, xử lý file >10GB hàng ngày, thành thạo pandas và Python optimization.",
                    new BigDecimal("94.00"),
                    ProposalStatus.SUBMITTED);
            updateJobProposalCount(job);
        });

        dockerJob.ifPresent(job -> {
            seedProposal(job, mentor5,
                    "Mình sẽ hướng dẫn team bạn Docker từ zero: viết Dockerfile cho từng service, docker-compose với networking, volumes, environment variables, và CI/CD integration. Thực hành trực tiếp trên dự án thật của team.",
                    new BigDecimal("1300.00"),
                    null,
                    21,
                    "DevOps engineer 5 năm, Docker chuyên sâu, từng chuyển đổi 20+ services sang container.",
                    new BigDecimal("95.00"),
                    ProposalStatus.SUBMITTED);
            seedProposal(job, mentor1,
                    "Mình có thể hỗ trợ phần Docker hóa Spring Boot services, tối ưu Dockerfile multi-stage build, và kết nối các service với nhau.",
                    new BigDecimal("1000.00"),
                    null,
                    21,
                    "Backend developer thành thạo Docker, từng container hóa hệ thống microservices cho startup.",
                    new BigDecimal("88.00"),
                    ProposalStatus.SUBMITTED);
            updateJobProposalCount(job);
        });
    }

    private void seedMentorPackages() {
        seedMentorPackagesForProfile(requireMentorProfile("mentor1@mentorx.demo"), List.of(
                buildMentorPackage("Career Debug Session", "One focused session to unblock architecture, backend design, or delivery issues.", PackageType.SINGLE_SESSION, 1, "450.00", 1,
                        "Live Zoom session", "Action notes after the call", "Follow-up via chat for 48 hours"),
                buildMentorPackage("Backend Project Review", "Deep review of code structure, API boundaries, and database design for a real project.", PackageType.PACKAGE_DEAL, 2, "820.00", 2,
                        "Code review before session", "Architecture recommendations", "Prioritized next steps"),
                buildMentorPackage("4-Week Mentoring Sprint", "Weekly sessions for interview prep or backend skill growth with accountability.", PackageType.SUBSCRIPTION, 4, "1600.00", 3,
                        "4 weekly sessions", "Homework and feedback", "Slack-style async support")
        ));

        seedMentorPackagesForProfile(requireMentorProfile("mentor2@mentorx.demo"), List.of(
                buildMentorPackage("Portfolio Critique", "Detailed feedback on portfolio, case study narrative, and hiring presentation.", PackageType.SINGLE_SESSION, 1, "380.00", 1,
                        "Portfolio walkthrough", "Structured critique", "Improvement checklist"),
                buildMentorPackage("UX Audit Package", "Collaborative review of onboarding, conversion, and usability for a live product flow.", PackageType.PACKAGE_DEAL, 2, "720.00", 2,
                        "Flow audit", "Annotated UX notes", "Design priorities")
        ));

        seedMentorPackagesForProfile(requireMentorProfile("mentor3@mentorx.demo"), List.of(
                buildMentorPackage("Data Storytelling Session", "Refine dashboards, metrics, and presentation flow for stakeholder-facing analytics.", PackageType.SINGLE_SESSION, 1, "520.00", 1,
                        "Metric review", "Dashboard critique", "Actionable recommendations"),
                buildMentorPackage("Analytics Coaching Pack", "Multi-session coaching on SQL, KPI design, and decision-ready dashboarding.", PackageType.SUBSCRIPTION, 4, "1850.00", 2,
                        "4 guided sessions", "Query review", "Project feedback")
        ));

        seedMentorPackagesForProfile(requireMentorProfile("mentor4@mentorx.demo"), List.of(
                buildMentorPackage("React Performance Review", "Targeted review of rendering cost, state shape, and frontend delivery risks.", PackageType.SINGLE_SESSION, 1, "410.00", 1,
                        "Profiler walkthrough", "Optimization notes", "Follow-up checklist"),
                buildMentorPackage("Frontend Architecture Sprint", "Short sprint to stabilize component boundaries and async UI behavior.", PackageType.PACKAGE_DEAL, 3, "1150.00", 2,
                        "Codebase walkthrough", "Architecture recommendations", "Refactor plan")
        ));

        seedMentorPackagesForProfile(requireMentorProfile("mentor5@mentorx.demo"), List.of(
                buildMentorPackage("Metrics Framework Session", "Define key product metrics and decision-ready KPI structure.", PackageType.SINGLE_SESSION, 1, "430.00", 1,
                        "Metric tree", "Instrumentation review", "Next-step recommendations"),
                buildMentorPackage("Growth Analytics Coaching", "Recurring sessions on experimentation, retention, and analytics storytelling.", PackageType.SUBSCRIPTION, 4, "1700.00", 2,
                        "Weekly analytics review", "Experiment feedback", "Stakeholder narrative coaching")
        ));

        seedMentorPackagesForProfile(requireMentorProfile("mentor6@mentorx.demo"), List.of(
                buildMentorPackage("Interview Readiness Session", "Sharpen technical stories, tradeoff communication, and interview confidence.", PackageType.SINGLE_SESSION, 1, "360.00", 1,
                        "Mock interview", "Story feedback", "Action notes"),
                buildMentorPackage("Career Growth Plan", "Structured coaching on promotion readiness and communication systems.", PackageType.PACKAGE_DEAL, 2, "690.00", 2,
                        "Growth plan", "Weekly habits", "Promotion packet guidance")
        ));
    }

    private void seedMentorPackagesForProfile(MentorProfile profile, List<MentorPackage> packages) {
        if (!mentorPackageRepository.findByMentorProfileIdOrderByDisplayOrderAsc(profile.getId()).isEmpty()) {
            return;
        }

        for (MentorPackage mentorPackage : packages) {
            mentorPackage.setMentorProfileId(profile.getId());
        }
        mentorPackageRepository.saveAll(packages);
    }

    private MentorPackage buildMentorPackage(
            String title,
            String description,
            PackageType packageType,
            Integer durationHours,
            String priceMxc,
            Integer displayOrder,
            String... features) {
        MentorPackage mentorPackage = new MentorPackage();
        mentorPackage.setTitle(title);
        mentorPackage.setDescription(description);
        mentorPackage.setPackageType(packageType);
        mentorPackage.setDurationHours(durationHours);
        mentorPackage.setPriceMxc(new BigDecimal(priceMxc));
        mentorPackage.setFeatures(features);
        mentorPackage.setIsActive(true);
        mentorPackage.setDisplayOrder(displayOrder);
        return mentorPackage;
    }

    private void seedMentorAvailability() {
        seedAvailabilityForProfile(requireMentorProfile("mentor1@mentorx.demo"), List.of(
                buildAvailability(2, "19:00", "21:00"),
                buildAvailability(4, "19:00", "21:00"),
                buildAvailability(6, "09:00", "11:00")
        ));

        seedAvailabilityForProfile(requireMentorProfile("mentor2@mentorx.demo"), List.of(
                buildAvailability(3, "20:00", "21:30"),
                buildAvailability(5, "20:00", "21:30"),
                buildAvailability(7, "09:30", "11:30")
        ));

        seedAvailabilityForProfile(requireMentorProfile("mentor3@mentorx.demo"), List.of(
                buildAvailability(2, "08:30", "10:00"),
                buildAvailability(4, "20:00", "22:00"),
                buildAvailability(6, "14:00", "16:00")
        ));

        seedAvailabilityForProfile(requireMentorProfile("mentor4@mentorx.demo"), List.of(
                buildAvailability(2, "19:30", "21:30"),
                buildAvailability(5, "19:30", "21:30"),
                buildAvailability(7, "10:00", "12:00")
        ));

        seedAvailabilityForProfile(requireMentorProfile("mentor5@mentorx.demo"), List.of(
                buildAvailability(3, "18:30", "20:00"),
                buildAvailability(5, "08:30", "10:00"),
                buildAvailability(7, "14:00", "15:30")
        ));

        seedAvailabilityForProfile(requireMentorProfile("mentor6@mentorx.demo"), List.of(
                buildAvailability(1, "20:00", "21:00"),
                buildAvailability(4, "20:00", "21:00"),
                buildAvailability(6, "09:00", "10:30")
        ));
    }

    private void seedAvailabilityForProfile(MentorProfile profile, List<MentorAvailability> slots) {
        if (!mentorAvailabilityRepository.findByMentorProfileIdOrderByDayOfWeekAscStartTimeAsc(profile.getId()).isEmpty()) {
            return;
        }

        for (MentorAvailability slot : slots) {
            slot.setMentorProfileId(profile.getId());
        }
        mentorAvailabilityRepository.saveAll(slots);
    }

    private MentorAvailability buildAvailability(int dayOfWeek, String startTime, String endTime) {
        MentorAvailability availability = new MentorAvailability();
        availability.setDayOfWeek(dayOfWeek);
        availability.setStartTime(LocalTime.parse(startTime));
        availability.setEndTime(LocalTime.parse(endTime));
        availability.setIsActive(true);
        return availability;
    }

    private void seedReviews() {
        User client1 = ensureDemoUser("client1@mentorx.demo", "Công ty ABC", "ABC Company", false, MentorStatus.NONE);
        User client2 = ensureDemoUser("client2@mentorx.demo", "Startup XYZ", "XYZ Startup", false, MentorStatus.NONE);
        User learner1 = ensureDemoUser("user1@mentorx.demo", "Lê Hà Anh", "Ha Anh", false, MentorStatus.NONE);
        User mentor1 = ensureDemoUser("mentor1@mentorx.demo", "Nguyễn Văn An", "An Nguyen", true, MentorStatus.APPROVED);
        User mentor2 = ensureDemoUser("mentor2@mentorx.demo", "Trần Thị Bình", "Binh Tran", true, MentorStatus.APPROVED);
        User client3 = ensureDemoUser("client3@mentorx.demo", "Doanh nghiệp DEF", "DEF Enterprise", false, MentorStatus.NONE);
        User learner2 = ensureDemoUser("user2@mentorx.demo", "Ngoc Mai", "Ngoc Mai", false, MentorStatus.NONE);
        User learner3 = ensureDemoUser("user3@mentorx.demo", "Quang Huy", "Quang Huy", false, MentorStatus.NONE);
        User mentor4 = ensureDemoUser("mentor4@mentorx.demo", "Pham Gia Linh", "Linh Pham", true, MentorStatus.APPROVED);
        User mentor6 = ensureDemoUser("mentor6@mentorx.demo", "Vu Thu Ha", "Ha Vu", true, MentorStatus.APPROVED);

        seedReview(client1, ReviewTargetType.MENTOR, mentor1.getId(), "Strong backend guidance", "An helped us simplify a messy Spring Boot service into something the team could maintain.",
                "Clear explanation style", "Could provide more written examples", "4.9", "4.8", "4.9", "5.0", "4.7", true, false, true,
                "Happy to help. If your team wants, we can do a follow-up session focused on testing strategy.");
        seedReview(client2, ReviewTargetType.MENTOR, mentor2.getId(), "Useful UX audit", "Binh identified the main onboarding friction points and gave concrete UI recommendations we could apply quickly.",
                "Actionable and practical", "Wanted one more alternate flow", "4.8", "4.9", "4.8", "4.7", "4.8", true, false, true,
                null);
        seedReview(learner1, ReviewTargetType.MENTOR, mentor1.getId(), "Great for interview prep", "I used the mentoring sprint to prepare for backend interviews and the structure was exactly what I needed.",
                "Focused homework", "No major downsides", "4.7", "4.7", "4.8", "4.7", "4.6", true, false, false,
                null);

        Course springBootCourse = requireCourse("spring-boot-foundations");
        seedReview(learner1, ReviewTargetType.COURSE, springBootCourse.getId(), "Practical and well structured", "The course content felt very applied and the PDF resources were useful after each lesson.",
                "Practical examples", "A few videos could be longer", "4.8", "4.7", "4.9", "4.8", "4.7", true, false, false,
                null);
        seedReview(client3, ReviewTargetType.MENTOR, mentor4.getId(), "Reliable frontend review", "Linh pointed out the components that actually caused our dashboard lag and kept the refactor plan realistic.",
                "Very concrete feedback", "Could use one more live debugging session", "4.8", "4.8", "4.9", "4.7", "4.8", true, false, false,
                null);
        seedReview(learner2, ReviewTargetType.MENTOR, mentor6.getId(), "Useful communication coaching", "The exercises helped me make my project updates clearer and more structured in team meetings.",
                "Actionable homework", "Wanted a longer follow-up plan", "4.9", "5.0", "4.8", "4.8", "4.7", true, false, true,
                "Glad it helped. Keep the weekly writing habit and the structure will become natural.");

        Course reactPerformanceCourse = requireCourse("react-performance-clinic");
        seedReview(learner3, ReviewTargetType.COURSE, reactPerformanceCourse.getId(), "Advanced but practical", "The performance workflow was advanced enough for real projects and still easy to apply step by step.",
                "Clear process", "Needs one more caching example", "4.8", "4.8", "4.9", "4.7", "4.8", true, false, false,
                null);
    }

    private void seedReview(
            User reviewer,
            ReviewTargetType targetType,
            UUID targetId,
            String reviewTitle,
            String reviewText,
            String pros,
            String cons,
            String overallRating,
            String communicationRating,
            String qualityRating,
            String timelinessRating,
            String valueRating,
            boolean isVerified,
            boolean isAnonymous,
            boolean isFeatured,
            String responseText) {
        if (reviewRepository.findByReviewerIdAndTargetTypeAndTargetId(reviewer.getId(), targetType, targetId).isPresent()) {
            return;
        }

        Review review = new Review();
        review.setReviewer(reviewer);
        review.setTargetType(targetType);
        review.setTargetId(targetId);
        review.setOverallRating(new BigDecimal(overallRating));
        review.setCommunicationRating(new BigDecimal(communicationRating));
        review.setQualityRating(new BigDecimal(qualityRating));
        review.setTimelinessRating(new BigDecimal(timelinessRating));
        review.setProfessionalismRating(new BigDecimal("4.9"));
        review.setValueRating(new BigDecimal(valueRating));
        review.setReviewTitle(reviewTitle);
        review.setReviewText(reviewText);
        review.setPros(pros);
        review.setCons(cons);
        review.setIsVerified(isVerified);
        review.setVerifiedAt(isVerified ? LocalDateTime.now().minusDays(2) : null);
        review.setIsAnonymous(isAnonymous);
        review.setIsPublic(true);
        review.setIsFeatured(isFeatured);
        review.setHelpfulCount(8);
        review.setNotHelpfulCount(1);
        review.setLanguage("vi");
        review.setWouldRecommend(true);
        review.setServiceCompletedAt(LocalDateTime.now().minusDays(7));
        review.setServiceDurationHours(new BigDecimal("1.50"));
        review.setServiceAmountMxc(new BigDecimal("450.00"));
        review.setResponseText(responseText);
        review.setResponseAt(responseText != null ? LocalDateTime.now().minusDays(1) : null);
        review.setResponseByUserId(responseText != null ? targetId : null);
        reviewRepository.save(review);
    }

    private void seedNotifications() {
        User admin = ensureDemoUser("admin@mentorx.demo", "System Administrator", "Admin", false, MentorStatus.NONE);
        User mentor1 = ensureDemoUser("mentor1@mentorx.demo", "Nguyễn Văn An", "An Nguyen", true, MentorStatus.APPROVED);
        User client1 = ensureDemoUser("client1@mentorx.demo", "Công ty ABC", "ABC Company", false, MentorStatus.NONE);
        User learner1 = ensureDemoUser("user1@mentorx.demo", "Lê Hà Anh", "Ha Anh", false, MentorStatus.NONE);

        Job backendJob = findJobByTitle("Backend Developer for Spring Boot API");
        Course springBootCourse = requireCourse("spring-boot-foundations");

        createNotification(mentor1, client1, NotificationType.NEW_MESSAGE, "New chat message", "ABC Company sent you a follow-up about the Spring Boot API scope.",
                "/chat", backendJob.getId(), "JOB", 2, false);
        createNotification(client1, mentor1, NotificationType.JOB_APPLICATION_RECEIVED, "New proposal received", "An Nguyen submitted a proposal for your backend API project.",
                "/jobs/" + backendJob.getId(), backendJob.getId(), "JOB", 2, false);
        createNotification(learner1, mentor1, NotificationType.COURSE_UPDATED, "Course resource updated", "A new resource was added to Spring Boot Foundations for Real Projects.",
                "/courses/" + springBootCourse.getId(), springBootCourse.getId(), "COURSE", 3, true);
        createNotification(learner1, admin, NotificationType.FEATURE_UPDATE, "Chat is ready for testing", "You can now test direct messaging, unread badges, and seeded demo conversations.",
                "/chat", null, "SYSTEM", 4, false);
    }

    private void createNotification(
            User recipient,
            User sender,
            NotificationType type,
            String title,
            String message,
            String actionUrl,
            UUID referenceId,
            String referenceType,
            int priority,
            boolean markAsRead) {
        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setSenderUser(sender);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setActionUrl(actionUrl);
        notification.setReferenceId(referenceId);
        notification.setReferenceType(referenceType);
        notification.setPriorityLevel(priority);
        notification.setCategory(type.name());
        notification.setIsDelivered(true);
        notification.setDeliveredAt(LocalDateTime.now().minusHours(2));
        if (markAsRead) {
            notification.markAsRead();
        }
        notificationRepository.save(notification);
    }

    private void seedChatData() {
        User admin = ensureDemoUser("admin@mentorx.demo", "System Administrator", "Admin", false, MentorStatus.NONE);
        User mentor1 = ensureDemoUser("mentor1@mentorx.demo", "Nguyễn Văn An", "An Nguyen", true, MentorStatus.APPROVED);
        User mentor2 = ensureDemoUser("mentor2@mentorx.demo", "Trần Thị Bình", "Binh Tran", true, MentorStatus.APPROVED);
        User client1 = ensureDemoUser("client1@mentorx.demo", "Công ty ABC", "ABC Company", false, MentorStatus.NONE);
        User learner1 = ensureDemoUser("user1@mentorx.demo", "Lê Hà Anh", "Ha Anh", false, MentorStatus.NONE);

        Job backendJob = findJobByTitle("Backend Developer for Spring Boot API");
        Course designCourse = requireCourse("ux-design-sprint");

        ChatRoom projectRoom = createChatRoom(
                ChatRoomType.DIRECT_MESSAGE,
                "Backend API Kickoff",
                "Scope and delivery discussion for the Spring Boot API project.",
                client1,
                List.of(mentor1),
                backendJob.getId(),
                "JOB"
        );
        seedChatMessage(projectRoom, client1, "Hi An, can you review the API authentication scope before we finalize milestones?", LocalDateTime.now().minusHours(14));
        seedChatMessage(projectRoom, mentor1, "Yes. I reviewed the brief and I would split auth, profile, and job flows into separate deliverables.", LocalDateTime.now().minusHours(13));
        seedChatMessage(projectRoom, client1, "Perfect. Please start with auth and user profile, then we will extend to jobs.", LocalDateTime.now().minusHours(12));

        ChatRoom courseRoom = createChatRoom(
                ChatRoomType.COURSE_DISCUSSION,
                "UX course questions",
                "Follow-up discussion around the UX audit playbook.",
                learner1,
                List.of(mentor2),
                designCourse.getId(),
                "COURSE"
        );
        seedChatMessage(courseRoom, learner1, "I finished the onboarding audit lesson. Do you recommend testing the new copy before changing the layout?", LocalDateTime.now().minusHours(8));
        seedChatMessage(courseRoom, mentor2, "Yes. Validate copy first so you can separate messaging issues from layout friction.", LocalDateTime.now().minusHours(7));
        seedChatMessage(courseRoom, mentor2, "After that, test one layout change at a time and keep the funnel instrumentation consistent.", LocalDateTime.now().minusHours(6));

        ChatRoom supportRoom = createChatRoom(
                ChatRoomType.QUICK_SUPPORT,
                "Platform support",
                "General support conversation for demo testing.",
                learner1,
                List.of(admin),
                null,
                "SUPPORT"
        );
        seedChatMessage(supportRoom, learner1, "I can access chat now. Which demo accounts should I use to verify unread badges?", LocalDateTime.now().minusHours(3));
        seedChatMessage(supportRoom, admin, "Use mentor1@mentorx.demo and client1@mentorx.demo for the direct project conversation.", LocalDateTime.now().minusHours(2));
    }

    private ChatRoom createChatRoom(
            ChatRoomType roomType,
            String roomName,
            String description,
            User creator,
            List<User> otherMembers,
            UUID referenceId,
            String referenceType) {
        ChatRoom room = new ChatRoom();
        room.setRoomType(roomType);
        room.setRoomName(roomName);
        room.setDescription(description);
        room.setCreatedByUser(creator);
        room.setIsActive(true);
        room.setIsPrivate(true);
        room.setMaxMembers(Math.max(2, otherMembers.size() + 1));
        room.setReferenceId(referenceId);
        room.setReferenceType(referenceType);
        room.setLastActivityAt(LocalDateTime.now());
        room = chatRoomRepository.save(room);

        room.getMembers().add(buildChatMember(room, creator, "OWNER", null, true));
        for (User member : otherMembers) {
            room.getMembers().add(buildChatMember(room, member, "MEMBER", creator, false));
        }
        room.setMemberCount(room.getMembers().size());
        return chatRoomRepository.save(room);
    }

    private ChatRoomMember buildChatMember(ChatRoom room, User user, String role, User invitedBy, boolean canInviteMembers) {
        ChatRoomMember member = new ChatRoomMember();
        member.setChatRoom(room);
        member.setUser(user);
        member.setMemberRole(role);
        member.setJoinedAt(LocalDateTime.now().minusDays(3));
        member.setInvitedByUser(invitedBy);
        member.setCanInviteMembers(canInviteMembers);
        member.setCanShareFiles(true);
        member.setCanSendMessages(true);
        member.setIsActive(true);
        member.setIsOnline(false);
        member.setUnreadCount(0);
        return member;
    }

    private void seedChatMessage(ChatRoom room, User sender, String content, LocalDateTime sentAt) {
        Message message = new Message();
        message.setChatRoom(room);
        message.setSender(sender);
        message.setMessageType(com.mentorx.api.feature.chat.enums.MessageType.TEXT);
        message.setContent(content);
        message.setSentAt(sentAt);
        message.setReadCount(1);
        Message savedMessage = messageRepository.save(message);

        for (ChatRoomMember member : room.getMembers()) {
            if (member.getUser().getId().equals(sender.getId())) {
                member.setLastSeenAt(sentAt);
                member.markAsRead(savedMessage.getId());
                continue;
            }
            member.setUnreadCount((member.getUnreadCount() == null ? 0 : member.getUnreadCount()) + 1);
        }
        chatRoomMemberRepository.saveAll(room.getMembers());

        room.setLastMessageId(savedMessage.getId());
        room.setLastMessagePreview(savedMessage.getDisplayContent());
        room.setLastMessageAt(sentAt);
        room.setLastMessageSenderId(sender.getId());
        room.setMessageCount(room.getMessageCount() + 1);
        room.setLastActivityAt(sentAt);
        chatRoomRepository.save(room);
    }

    private User ensureDemoUser(String email, String fullName, String displayName, boolean isMentor, MentorStatus mentorStatus) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createUser(email, fullName, displayName, UserStatus.ACTIVE, isMentor, mentorStatus));

        user.setFullName(fullName);
        user.setDisplayName(displayName);
        user.setStatus(UserStatus.ACTIVE);
        user.setIsEmailVerified(true);
        user.setIsMentor(isMentor);
        user.setMentorStatus(mentorStatus);
        user.setProfileIsPublic(true);
        user.setIsOnboarded(true);
        userRepository.save(user);

        assignRoleToUserIfMissing(user, "USER");
        if (isMentor) {
            assignRoleToUserIfMissing(user, "MENTOR");
        }
        setupUserAccountIfMissing(user);
        return user;
    }

    private User ensureSeedUser(DemoUserSeed seed) {
        User user = ensureDemoUser(seed.email(), seed.fullName(), seed.displayName(), seed.isMentor(), seed.mentorStatus());
        for (String roleName : seed.roles()) {
            assignRoleToUserIfMissing(user, roleName);
        }
        return user;
    }

    private void createJobIfMissing(
            User client,
            Integer categoryId,
            JobType jobType,
            String title,
            String description,
            BudgetType budgetType,
            BigDecimal budgetMinMxc,
            BigDecimal budgetMaxMxc,
            BigDecimal hourlyRateMxc,
            BigDecimal estimatedHours,
            LocalDateTime deadlineAt,
            JobStatus status,
            boolean featured,
            LocalDateTime publishedAt) {
        boolean exists = jobRepository.findAll().stream()
                .anyMatch(job -> title.equals(job.getTitle()));
        if (exists) {
            return;
        }

        jobRepository.save(Job.builder()
                .client(client)
                .categoryId(categoryId)
                .jobType(jobType)
                .title(title)
                .description(description)
                .budgetType(budgetType)
                .budgetMinMxc(budgetMinMxc)
                .budgetMaxMxc(budgetMaxMxc)
                .hourlyRateMxc(hourlyRateMxc)
                .estimatedHours(estimatedHours)
                .deadlineAt(deadlineAt)
                .status(status)
                .isFeatured(featured)
                .proposalCount(0)
                .publishedAt(publishedAt)
                .build());
    }

    private void updateJobProposalCount(Job job) {
        job.setProposalCount((int) proposalRepository.findByJobId(job.getId(), PageRequest.of(0, 100)).getTotalElements());
        jobRepository.save(job);
    }

    private MentorProfile requireMentorProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Demo mentor user not found for " + email));
        return mentorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Mentor profile not found for " + email));
    }

    private Course requireCourse(String slug) {
        return courseRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new IllegalStateException("Course not found for slug " + slug));
    }

    private Job findJobByTitle(String title) {
        return jobRepository.findAll().stream()
                .filter(job -> title.equals(job.getTitle()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Job not found for title " + title));
    }

    private void seedProposal(
            Job job,
            User mentor,
            String coverLetter,
            BigDecimal proposedAmount,
            BigDecimal proposedHourlyRate,
            Integer durationDays,
            String relevantExperience,
            BigDecimal score,
            ProposalStatus status) {
        if (mentor == null || proposalRepository.findByJobIdAndMentorId(job.getId(), mentor.getId()).isPresent()) {
            return;
        }

        Proposal proposal = new Proposal();
        proposal.setJob(job);
        proposal.setMentor(mentor);
        proposal.setStatus(status);
        proposal.setCoverLetter(coverLetter);
        proposal.setProposedAmount(proposedAmount);
        proposal.setProposedHourlyRate(proposedHourlyRate);
        proposal.setEstimatedDurationDays(durationDays);
        proposal.setProposedStartDate(LocalDate.now().plusDays(2));
        proposal.setProposedDeliveryDate(LocalDate.now().plusDays(durationDays == null ? 7 : durationDays));
        proposal.setRelevantExperience(relevantExperience);
        proposal.setQuestions("What access, repositories, or product context should I review before starting?");
        proposal.setTerms("Includes one revision round after initial delivery.");
        proposal.setSubmittedAt(LocalDateTime.now().minusHours(6));
        proposal.setScore(score);
        proposal.setIsCounterProposal(false);
        proposal.setIsFeatured(false);
        proposal.setInterviewRequested(false);
        proposal.setViewCount(0);
        proposalRepository.save(proposal);
    }

    private record DemoUserSeed(
            String email,
            String fullName,
            String displayName,
            boolean isMentor,
            MentorStatus mentorStatus,
            List<String> roles
    ) {}

    private record MentorProfileSeed(
            String email,
            String fullName,
            String displayName,
            String headline,
            String availability,
            BigDecimal hourlyRateMxc,
            Short yearsOfExperience,
            BigDecimal averageRating,
            Integer totalReviews,
            Boolean featured
    ) {}
}
