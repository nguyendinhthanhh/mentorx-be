package com.mentorx.api.common.config;

import com.mentorx.api.common.enums.BudgetType;
import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.common.enums.JobType;
import com.mentorx.api.common.enums.LessonType;
import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.enums.PackageType;
import com.mentorx.api.common.enums.SupportedLanguage;
import com.mentorx.api.common.enums.UserStatus;
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

        if (mentorProfileRepository.count() == 0) {
            log.info("Seeding sample mentor profiles...");
        }
        seedMentorProfiles();

        if (courseRepository.count() == 0) {
            log.info("Seeding sample courses...");
            seedCourses();
        }

        if (jobRepository.findOpen(PageRequest.of(0, 1)).isEmpty()) {
            log.info("Seeding sample open jobs...");
            seedJobs();
        }

        log.info("Ensuring sample job proposals exist...");
        seedProposals();

        if (mentorPackageRepository.count() == 0) {
            log.info("Seeding mentor packages...");
            seedMentorPackages();
        }

        if (mentorAvailabilityRepository.count() == 0) {
            log.info("Seeding mentor availability...");
            seedMentorAvailability();
        }

        if (reviewRepository.count() == 0) {
            log.info("Seeding sample reviews...");
            seedReviews();
        }

        if (notificationRepository.count() == 0) {
            log.info("Seeding sample notifications...");
            seedNotifications();
        }

        if (chatRoomRepository.count() == 0) {
            log.info("Seeding sample chat rooms and messages...");
            seedChatData();
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

        User client2 = createUser("client2@mentorx.demo", "Startup XYZ", "XYZ Startup", UserStatus.ACTIVE, false, MentorStatus.NONE);
        assignRoleToUser(client2, "USER");
        setupUserAccount(client2);

        User client3 = createUser("client3@mentorx.demo", "Doanh nghiệp DEF", "DEF Enterprise", UserStatus.ACTIVE, false, MentorStatus.NONE);
        assignRoleToUser(client3, "USER");
        setupUserAccount(client3);

        User learner1 = createUser("user1@mentorx.demo", "Lê Hà Anh", "Ha Anh", UserStatus.ACTIVE, false, MentorStatus.NONE);
        assignRoleToUser(learner1, "USER");
        setupUserAccount(learner1);
    }

    private User createUser(String email, String fullName, String displayName, UserStatus status, boolean isMentor, MentorStatus mentorStatus) {
        String avatarSeed = (displayName != null && !displayName.isBlank() ? displayName : fullName).trim().replace(" ", "+");
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("password"))
                .fullName(fullName)
                .displayName(displayName)
                .avatarUrl("https://ui-avatars.com/api/?name=" + avatarSeed + "&background=random")
                .bio("Demo account seeded for MentorX development.")
                .status(status)
                .isEmailVerified(true)
                .isMentor(isMentor)
                .mentorStatus(mentorStatus)
                .preferredLanguage(SupportedLanguage.vi)
                .profileIsPublic(true)
                .isOnboarded(true)
                .build();
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
        Integer designCategoryId = categoryRepository.findBySlug("design").map(Category::getId).orElse(null);

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

        String sampleDocumentUrl = ensureSampleDocument("mentorx-sample-document", "MentorX Sample Document");

        CourseSection course1Section1 = createSection(course1, 1, "Khởi động dự án", "Cài đặt môi trường và hiểu cấu trúc dự án.");
        createLesson(course1Section1, 1, "Giới thiệu khóa học", "Tổng quan lộ trình và kết quả đạt được.", LessonType.VIDEO, 8, true,
                "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", null, null);
        createLesson(course1Section1, 2, "Tài liệu setup môi trường", "Checklist cài đặt JDK, IDE, và Postgres.", LessonType.ARTICLE, 12, false,
                null, "Hướng dẫn chi tiết cài đặt môi trường phát triển Spring Boot.", sampleDocumentUrl);
        updateSectionDuration(course1Section1);

        CourseSection course1Section2 = createSection(course1, 2, "Xây dựng REST API", "Thiết kế API và triển khai CRUD chuẩn.");
        createLesson(course1Section2, 1, "Thiết kế data model", "Xây entity và mapping JPA hiệu quả.", LessonType.VIDEO, 18, false,
                "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", null, null);
        createLesson(course1Section2, 2, "Tài liệu API mẫu", "Swagger template và checklist kiểm thử.", LessonType.ARTICLE, 10, false,
                null, "Template Swagger và checklist kiểm thử cơ bản.", sampleDocumentUrl);
        updateSectionDuration(course1Section2);

        updateCourseTotals(course1);

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
        createLesson(course2Section1, 1, "Research plan", "Cách xây dựng kế hoạch research nhanh.", LessonType.VIDEO, 14, true,
                "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", null, null);
        createLesson(course2Section1, 2, "Research template", "Mẫu interview script và summary.", LessonType.ARTICLE, 9, false,
                null, "Mẫu câu hỏi interview và template tổng hợp insight.", sampleDocumentUrl);
        updateSectionDuration(course2Section1);

        CourseSection course2Section2 = createSection(course2, 2, "Prototype & Handoff", "Thiết kế prototype và bàn giao dev.");
        createLesson(course2Section2, 1, "Prototype nhanh với Figma", "Thực hành flow và component.", LessonType.VIDEO, 16, false,
                "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", null, null);
        createLesson(course2Section2, 2, "Checklist handoff", "Đảm bảo dev hiểu spec.", LessonType.ARTICLE, 7, false,
                null, "Checklist bàn giao thiết kế và QA UI.", sampleDocumentUrl);
        updateSectionDuration(course2Section2);

        updateCourseTotals(course2);

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
        createLesson(course3Section1, 1, "API design guideline", "Nguyên tắc thiết kế endpoint và naming.", LessonType.ARTICLE, 6, true,
                null, "Bộ guideline thiết kế API nhất quán cho team backend.", sampleDocumentUrl);
        createLesson(course3Section1, 2, "Checklist release", "Checklist trước khi release API.", LessonType.ARTICLE, 5, false,
                null, "Checklist kiểm tra security, logging, monitoring.", sampleDocumentUrl);
        updateSectionDuration(course3Section1);

        updateCourseTotals(course3);
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
                .lessonType(lessonType)
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

    private void setupUserAccountIfMissing(User user) {
        if (walletRepository.findByUserIdAndAccountType(user.getId(), WalletAccountType.USER_AVAILABLE).isEmpty()) {
            walletRepository.save(Wallet.builder()
                    .user(user)
                    .accountType(WalletAccountType.USER_AVAILABLE)
                    .balanceMxc(new BigDecimal("1000.00"))
                    .build());
        }

        if (walletRepository.findByUserIdAndAccountType(user.getId(), WalletAccountType.USER_PENDING).isEmpty()) {
            walletRepository.save(Wallet.builder()
                    .user(user)
                    .accountType(WalletAccountType.USER_PENDING)
                    .balanceMxc(BigDecimal.ZERO)
                    .build());
        }

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

    private void seedJobs() {
        User client = userRepository.findByEmail("client1@mentorx.demo")
                .orElseGet(() -> {
                    User user = createUser("client1@mentorx.demo", "ABC Company", "ABC Company", UserStatus.ACTIVE, false, MentorStatus.NONE);
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
                        .categoryId(softwareCategoryId)
                        .jobType(JobType.FREELANCE_PROJECT)
                        .title("Backend Developer for Spring Boot API")
                        .description("Build and optimize REST APIs, authentication flows, and PostgreSQL data access for a mentoring platform.")
                        .budgetType(BudgetType.FIXED)
                        .budgetMinMxc(new BigDecimal("1200.00"))
                        .budgetMaxMxc(new BigDecimal("2500.00"))
                        .deadlineAt(now.plusDays(21))
                        .status(JobStatus.OPEN)
                        .isFeatured(true)
                        .proposalCount(6)
                        .publishedAt(now.minusDays(2))
                        .build(),
                Job.builder()
                        .client(client)
                        .categoryId(designCategoryId)
                        .jobType(JobType.QUICK_FIX)
                        .title("UX Review for Onboarding Flow")
                        .description("Review a multi-step onboarding flow and provide concrete UX improvements for conversion and clarity.")
                        .budgetType(BudgetType.HOURLY)
                        .hourlyRateMxc(new BigDecimal("350.00"))
                        .estimatedHours(new BigDecimal("6.00"))
                        .deadlineAt(now.plusDays(10))
                        .status(JobStatus.OPEN)
                        .isFeatured(true)
                        .proposalCount(4)
                        .publishedAt(now.minusDays(1))
                        .build(),
                Job.builder()
                        .client(client)
                        .categoryId(dataCategoryId)
                        .jobType(JobType.LONG_TERM_MENTORING)
                        .title("Data Analyst Mentor for Dashboard Project")
                        .description("Guide a junior analyst on metric design, SQL analysis, and dashboard storytelling over several sessions.")
                        .budgetType(BudgetType.FIXED)
                        .budgetMinMxc(new BigDecimal("900.00"))
                        .budgetMaxMxc(new BigDecimal("1800.00"))
                        .deadlineAt(now.plusDays(30))
                        .status(JobStatus.OPEN)
                        .isFeatured(false)
                        .proposalCount(3)
                        .publishedAt(now.minusDays(3))
                        .build(),
                Job.builder()
                        .client(client)
                        .categoryId(businessCategoryId)
                        .jobType(JobType.FREELANCE_PROJECT)
                        .title("Go-to-Market Strategy Consultation")
                        .description("Help refine positioning, pricing, and launch strategy for an early-stage SaaS product.")
                        .budgetType(BudgetType.HOURLY)
                        .hourlyRateMxc(new BigDecimal("500.00"))
                        .estimatedHours(new BigDecimal("8.00"))
                        .deadlineAt(now.plusDays(14))
                        .status(JobStatus.OPEN)
                        .isFeatured(true)
                        .proposalCount(5)
                        .publishedAt(now.minusDays(4))
                        .build()
        ));
    }

    private void seedProposals() {
        List<Job> openJobs = jobRepository.findOpen(PageRequest.of(0, 20)).getContent();
        Optional<Job> backendJob = openJobs.stream()
                .filter(job -> "Backend Developer for Spring Boot API".equals(job.getTitle()))
                .findFirst();
        Optional<Job> uxJob = openJobs.stream()
                .filter(job -> "UX Review for Onboarding Flow".equals(job.getTitle()))
                .findFirst();
        Optional<Job> dataJob = openJobs.stream()
                .filter(job -> "Data Analyst Mentor for Dashboard Project".equals(job.getTitle()))
                .findFirst();
        Optional<Job> strategyJob = openJobs.stream()
                .filter(job -> "Go-to-Market Strategy Consultation".equals(job.getTitle()))
                .findFirst();

        User mentor1 = userRepository.findByEmail("mentor1@mentorx.demo").orElse(null);
        User mentor2 = userRepository.findByEmail("mentor2@mentorx.demo").orElse(null);
        User mentor3 = userRepository.findByEmail("mentor3@mentorx.demo").orElse(null);

        backendJob.ifPresent(job -> {
            seedProposal(job, mentor1,
                    "I can help implement the Spring Boot API with clean authentication boundaries, optimized repository queries, and pragmatic testing. I have built similar mentoring and marketplace backends with JWT auth, PostgreSQL, and production-ready DTO mapping.",
                    new BigDecimal("2100.00"),
                    null,
                    12,
                    "8 years building Java/Spring systems, including API performance tuning, RBAC, and PostgreSQL schema optimization.",
                    new BigDecimal("94.00"),
                    ProposalStatus.SUBMITTED);
            seedProposal(job, mentor3,
                    "I can review the existing backend structure first, then deliver the missing API work in small milestones so you can test each part quickly. My focus would be correctness, database access, and clean integration with the frontend.",
                    new BigDecimal("1850.00"),
                    null,
                    14,
                    "Delivered data-heavy APIs and analytics services with Spring Boot, SQL optimization, and test coverage.",
                    new BigDecimal("88.00"),
                    ProposalStatus.SUBMITTED);
            job.setProposalCount((int) proposalRepository.findByJobId(job.getId(), PageRequest.of(0, 100)).getTotalElements());
            jobRepository.save(job);
        });

        uxJob.ifPresent(job -> {
            seedProposal(job, mentor2,
                    "I can audit the onboarding flow end to end and give you a prioritized list of improvements with revised copy, friction points, and conversion-focused screen recommendations.",
                    new BigDecimal("1200.00"),
                    new BigDecimal("350.00"),
                    3,
                    "6 years in product design, onboarding optimization, usability review, and SaaS conversion workflows.",
                    new BigDecimal("91.00"),
                    ProposalStatus.SUBMITTED);
            job.setProposalCount((int) proposalRepository.findByJobId(job.getId(), PageRequest.of(0, 100)).getTotalElements());
            jobRepository.save(job);
        });

        dataJob.ifPresent(job -> {
            seedProposal(job, mentor3,
                    "I can mentor the analyst through metric definition, SQL review, and dashboard storytelling. We can work in weekly sessions with concrete assignments between calls.",
                    new BigDecimal("1500.00"),
                    null,
                    21,
                    "7 years in data science, analytics workflows, and dashboard decision support.",
                    new BigDecimal("92.00"),
                    ProposalStatus.SUBMITTED);
            seedProposal(job, mentor1,
                    "I can help structure the data model, review SQL queries, and make sure the dashboard backend is reliable before the reporting layer is polished.",
                    new BigDecimal("1350.00"),
                    null,
                    18,
                    "Backend and database mentor with strong SQL performance and reporting API experience.",
                    new BigDecimal("86.00"),
                    ProposalStatus.SUBMITTED);
            job.setProposalCount((int) proposalRepository.findByJobId(job.getId(), PageRequest.of(0, 100)).getTotalElements());
            jobRepository.save(job);
        });

        strategyJob.ifPresent(job -> {
            seedProposal(job, mentor2,
                    "I can help clarify positioning, review the pricing page, and map the launch plan into practical experiments that can be validated quickly.",
                    new BigDecimal("1600.00"),
                    new BigDecimal("500.00"),
                    7,
                    "Product design and SaaS positioning experience across onboarding, pricing, and launch research.",
                    new BigDecimal("89.00"),
                    ProposalStatus.SUBMITTED);
            job.setProposalCount((int) proposalRepository.findByJobId(job.getId(), PageRequest.of(0, 100)).getTotalElements());
            jobRepository.save(job);
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

        assignRoleToUserIfMissing(user, "USER");
        if (isMentor) {
            assignRoleToUserIfMissing(user, "MENTOR");
        }
        setupUserAccountIfMissing(user);
        return user;
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
