package com.mentorx.api.feature.analytics.job;

import com.mentorx.api.common.enums.TxnType;
import com.mentorx.api.common.enums.WalletAccountType;
import com.mentorx.api.feature.analytics.entity.CourseDailySnapshot;
import com.mentorx.api.feature.analytics.entity.EarningsDailySnapshot;
import com.mentorx.api.feature.analytics.repository.CourseDailySnapshotRepository;
import com.mentorx.api.feature.analytics.repository.EarningsDailySnapshotRepository;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.repository.CourseEnrollmentRepository;
import com.mentorx.api.feature.course.repository.CourseLessonRepository;
import com.mentorx.api.feature.course.repository.CourseRepository;
import com.mentorx.api.feature.job.repository.ContractRepository;
import com.mentorx.api.feature.job.repository.ProposalRepository;
import com.mentorx.api.feature.review.enums.ReviewTargetType;
import com.mentorx.api.feature.review.repository.ReviewRepository;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.wallet.entity.Wallet;
import com.mentorx.api.feature.wallet.repository.WalletRepository;
import com.mentorx.api.feature.wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * M12.2 Phase H3: unit tests for EarningsAggregationJob.
 * Covers H1 bug fixes (BUG-A, BUG-B, BUG-D) and H2 performance fixes (L2/L6, L3).
 */
class EarningsAggregationJobTest {

    private EarningsDailySnapshotRepository earningsRepository;
    private CourseDailySnapshotRepository courseDailyRepository;
    private WalletTransactionRepository transactionRepository;
    private WalletRepository walletRepository;
    private ContractRepository contractRepository;
    private ProposalRepository proposalRepository;
    private CourseEnrollmentRepository enrollmentRepository;
    private CourseLessonRepository lessonRepository;
    private CourseRepository courseRepository;
    private ReviewRepository reviewRepository;
    private UserRepository userRepository;

    private EarningsAggregationJob job;

    private static final LocalDate DAY = LocalDate.of(2026, 6, 15);
    private static final LocalDateTime START = DAY.atStartOfDay();
    private static final LocalDateTime END = DAY.plusDays(1).atStartOfDay();

    @BeforeEach
    void setUp() {
        earningsRepository = mock(EarningsDailySnapshotRepository.class);
        courseDailyRepository = mock(CourseDailySnapshotRepository.class);
        transactionRepository = mock(WalletTransactionRepository.class);
        walletRepository = mock(WalletRepository.class);
        contractRepository = mock(ContractRepository.class);
        proposalRepository = mock(ProposalRepository.class);
        enrollmentRepository = mock(CourseEnrollmentRepository.class);
        lessonRepository = mock(CourseLessonRepository.class);
        courseRepository = mock(CourseRepository.class);
        reviewRepository = mock(ReviewRepository.class);
        userRepository = mock(UserRepository.class);

        job = new EarningsAggregationJob(
                earningsRepository, courseDailyRepository, transactionRepository,
                walletRepository, contractRepository, proposalRepository,
                enrollmentRepository, lessonRepository, courseRepository,
                reviewRepository, userRepository
        );

        // Default: all queries return empty
        when(transactionRepository.sumCreditsByUserInWindow(any(), any(), any())).thenReturn(List.of());
        when(transactionRepository.sumDebitsByUserInWindow(any(), any(), any())).thenReturn(List.of());
        when(contractRepository.countCompletedByMentorInWindow(any(), any())).thenReturn(List.of());
        when(contractRepository.countActiveByMentorAsOf(any())).thenReturn(List.of());
        when(proposalRepository.countProposalsByMentorInWindow(any(), any())).thenReturn(List.of());
        when(proposalRepository.countAcceptedProposalsByMentorInWindow(any(), any())).thenReturn(List.of());
        when(enrollmentRepository.countEnrollmentsByInstructorInWindow(any(), any())).thenReturn(List.of());
        when(enrollmentRepository.countEnrollmentsByCourseInWindow(any(), any())).thenReturn(List.of());
        when(enrollmentRepository.revenueByCourseInWindow(any(), any())).thenReturn(List.of());
    }

    // ── H3.1: BUG-A regression — contractsActive is per-user ──────────────────

    @Test
    void aggregate_writesCorrectContractsActivePerUser() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        when(contractRepository.countActiveByMentorAsOf(END)).thenReturn(List.<Object[]>of(new Object[]{user1, 3L},
                new Object[]{user2, 5L}
        ));
        stubUser(user1);
        stubUser(user2);
        when(earningsRepository.findByUserIdAndSnapshotDate(any(), eq(DAY))).thenReturn(Optional.empty());
        when(earningsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        job.aggregateEarnings(DAY);

        ArgumentCaptor<EarningsDailySnapshot> captor = ArgumentCaptor.forClass(EarningsDailySnapshot.class);
        verify(earningsRepository, times(2)).save(captor.capture());

        List<EarningsDailySnapshot> saved = captor.getAllValues();
        EarningsDailySnapshot s1 = saved.stream().filter(s -> s.getUser().getId().equals(user1)).findFirst().orElseThrow();
        EarningsDailySnapshot s2 = saved.stream().filter(s -> s.getUser().getId().equals(user2)).findFirst().orElseThrow();

        assertThat(s1.getContractsActive()).isEqualTo(3);
        assertThat(s2.getContractsActive()).isEqualTo(5);
    }

    // ── H3.2: BUG-B regression — contractsCompleted separate from jobsCompleted ──

    @Test
    void aggregate_writesSeparateContractsCompletedAndJobsCompleted() {
        UUID userId = UUID.randomUUID();

        when(contractRepository.countCompletedByMentorInWindow(START, END)).thenReturn(List.<Object[]>of(new Object[]{userId, 4L}
        ));
        stubUser(userId);
        when(earningsRepository.findByUserIdAndSnapshotDate(userId, DAY)).thenReturn(Optional.empty());
        when(earningsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        job.aggregateEarnings(DAY);

        ArgumentCaptor<EarningsDailySnapshot> captor = ArgumentCaptor.forClass(EarningsDailySnapshot.class);
        verify(earningsRepository).save(captor.capture());
        EarningsDailySnapshot snapshot = captor.getValue();

        assertThat(snapshot.getJobsCompleted()).isEqualTo(4);
        assertThat(snapshot.getContractsCompleted()).isEqualTo(4);
    }

    // ── H3.3: L6 regression — withdrawals via dedicated debit query ───────────

    @Test
    void aggregate_sumsWithdrawalsViaDedicatedQuery() {
        UUID userId = UUID.randomUUID();

        when(transactionRepository.sumDebitsByUserInWindow(eq(TxnType.WITHDRAWAL), eq(START), eq(END)))
                .thenReturn(List.<Object[]>of(new Object[]{userId, new BigDecimal("300.0000")}));
        stubUser(userId);
        when(earningsRepository.findByUserIdAndSnapshotDate(userId, DAY)).thenReturn(Optional.empty());
        when(earningsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        job.aggregateEarnings(DAY);

        ArgumentCaptor<EarningsDailySnapshot> captor = ArgumentCaptor.forClass(EarningsDailySnapshot.class);
        verify(earningsRepository).save(captor.capture());
        assertThat(captor.getValue().getWithdrawnMxc()).isEqualByComparingTo(new BigDecimal("300.0000"));
    }

    // ── H3.4: L2 regression — platform fees via dedicated debit query ─────────

    @Test
    void aggregate_sumsPlatformFeesViaDedicatedQuery() {
        UUID userId = UUID.randomUUID();

        when(transactionRepository.sumDebitsByUserInWindow(eq(TxnType.PLATFORM_FEE), eq(START), eq(END)))
                .thenReturn(List.<Object[]>of(new Object[]{userId, new BigDecimal("50.0000")}));
        stubUser(userId);
        when(earningsRepository.findByUserIdAndSnapshotDate(userId, DAY)).thenReturn(Optional.empty());
        when(earningsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        job.aggregateEarnings(DAY);

        ArgumentCaptor<EarningsDailySnapshot> captor = ArgumentCaptor.forClass(EarningsDailySnapshot.class);
        verify(earningsRepository).save(captor.capture());
        assertThat(captor.getValue().getPlatformFeeMxc()).isEqualByComparingTo(new BigDecimal("50.0000"));
    }

    // ── H3.5: BUG-D regression — enrollment rollup via instructor query ───────

    @Test
    void aggregate_populatesCourseEnrollmentRollupViaInstructorQuery() {
        UUID instructorId = UUID.randomUUID();

        when(enrollmentRepository.countEnrollmentsByInstructorInWindow(START, END))
                .thenReturn(List.<Object[]>of(new Object[]{instructorId, 25L}));
        stubUser(instructorId);
        when(earningsRepository.findByUserIdAndSnapshotDate(instructorId, DAY)).thenReturn(Optional.empty());
        when(earningsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        job.aggregateEarnings(DAY);

        // Verify the instructor query is called exactly once (not N+1)
        verify(enrollmentRepository, times(1)).countEnrollmentsByInstructorInWindow(START, END);

        ArgumentCaptor<EarningsDailySnapshot> captor = ArgumentCaptor.forClass(EarningsDailySnapshot.class);
        verify(earningsRepository).save(captor.capture());
        assertThat(captor.getValue().getCourseEnrollments()).isEqualTo(25);
    }

    // ── H3.6: L3 regression — course snapshot populates all 6 fields ──────────

    @Test
    void aggregate_populatesCourseSnapshotAllSixFields() {
        UUID courseId = UUID.randomUUID();
        Course course = new Course();
        course.setId(courseId);

        when(enrollmentRepository.countEnrollmentsByCourseInWindow(START, END))
                .thenReturn(List.<Object[]>of(new Object[]{courseId, 10L}));
        when(enrollmentRepository.revenueByCourseInWindow(START, END))
                .thenReturn(List.<Object[]>of(new Object[]{courseId, UUID.randomUUID(), new BigDecimal("500.00")}));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseDailyRepository.findByCourseIdAndSnapshotDate(courseId, DAY)).thenReturn(Optional.empty());
        when(courseDailyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // H2.3 fields
        when(enrollmentRepository.countCompletedByCourseId(courseId)).thenReturn(7L);
        when(enrollmentRepository.countByCourseId(courseId)).thenReturn(10L);
        when(lessonRepository.sumViewCountByCourseId(courseId)).thenReturn(1500L);
        when(reviewRepository.averagePublicRatingByTarget(ReviewTargetType.COURSE, courseId))
                .thenReturn(new BigDecimal("4.35"));

        job.aggregateEarnings(DAY); // triggers aggregateCourseSnapshots internally via aggregate()
        // Call directly since aggregateEarnings does not call aggregateCourseSnapshots
        // We need to invoke via the public aggregate or call the method via day
        // Actually aggregate() calls both. Let's use the test-friendly approach:
        // The aggregateCourseSnapshots is private, called from aggregate().
        // For unit test, let's just verify the public aggregateEarnings + the snapshot separately.

        // Reset to test course snapshots in isolation — use reflection-free approach via aggregate()
        // Actually better: call aggregate() which does both
        // But aggregate() calls LocalDate.now() — let's just test via aggregateEarnings which only does earnings.
        // The course snapshots are tested via the full aggregate() path but we'd need to mock LocalDate.
        // Simplest: invoke aggregateEarnings for earnings assertions, then test course snapshot behavior
        // by calling the package-private method. Since it's private, let's just trust that aggregate() calls it.

        // Actually let me just test this via calling aggregate() — but it uses LocalDate.now().
        // Better: just verify the mock interactions since we set up the repos above.
        // The simplest approach: the previous call already triggered enrollmentRepository mocks.
        // Let me reset and use a direct test.

        // For this test, we need to trigger aggregateCourseSnapshots. It's called from aggregate()
        // which uses yesterday = LocalDate.now().minusDays(1). We can't control that.
        // So we'll just verify that the repos we set up for courseId were NOT called (because
        // aggregateEarnings doesn't call aggregateCourseSnapshots).
        // Let's restructure: just verify via the mock that courseDailyRepository.save was called
        // with expected fields. We need to trigger aggregateCourseSnapshots — which is private.
        // The cleanest approach in our context: make a new instance and just trust the public API.

        // Reset and use a fresh test approach:
        reset(courseDailyRepository, enrollmentRepository, lessonRepository, reviewRepository, courseRepository);
        when(enrollmentRepository.countEnrollmentsByCourseInWindow(any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{courseId, 10L}));
        when(enrollmentRepository.revenueByCourseInWindow(any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{courseId, UUID.randomUUID(), new BigDecimal("500.00")}));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseDailyRepository.findByCourseIdAndSnapshotDate(eq(courseId), any())).thenReturn(Optional.empty());
        when(courseDailyRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(enrollmentRepository.countCompletedByCourseId(courseId)).thenReturn(7L);
        when(enrollmentRepository.countByCourseId(courseId)).thenReturn(10L);
        when(lessonRepository.sumViewCountByCourseId(courseId)).thenReturn(1500L);
        when(reviewRepository.averagePublicRatingByTarget(ReviewTargetType.COURSE, courseId))
                .thenReturn(new BigDecimal("4.35"));

        // Also need to re-stub earnings queries to avoid NPE in aggregate()
        when(transactionRepository.sumCreditsByUserInWindow(any(), any(), any())).thenReturn(List.of());
        when(transactionRepository.sumDebitsByUserInWindow(any(), any(), any())).thenReturn(List.of());
        when(contractRepository.countCompletedByMentorInWindow(any(), any())).thenReturn(List.of());
        when(contractRepository.countActiveByMentorAsOf(any())).thenReturn(List.of());
        when(proposalRepository.countProposalsByMentorInWindow(any(), any())).thenReturn(List.of());
        when(proposalRepository.countAcceptedProposalsByMentorInWindow(any(), any())).thenReturn(List.of());
        when(enrollmentRepository.countEnrollmentsByInstructorInWindow(any(), any())).thenReturn(List.of());

        job.aggregate(); // calls aggregateCourseSnapshots(yesterday)

        ArgumentCaptor<CourseDailySnapshot> captor = ArgumentCaptor.forClass(CourseDailySnapshot.class);
        verify(courseDailyRepository).save(captor.capture());
        CourseDailySnapshot saved = captor.getValue();

        assertThat(saved.getEnrollmentsCount()).isEqualTo(10);
        assertThat(saved.getSoldCount()).isEqualTo(10);
        assertThat(saved.getRevenueMxc()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(saved.getCompletionRate()).isEqualByComparingTo(new BigDecimal("0.7000"));
        assertThat(saved.getLessonViews()).isEqualTo(1500);
        assertThat(saved.getAverageRating()).isEqualByComparingTo(new BigDecimal("4.35"));
    }

    // ── H3.7: upsert — existing row updated, not duplicated ───────────────────

    @Test
    void aggregate_upsertsExistingRow() {
        UUID userId = UUID.randomUUID();
        EarningsDailySnapshot existing = EarningsDailySnapshot.builder()
                .user(stubUser(userId))
                .snapshotDate(DAY)
                .build();

        when(transactionRepository.sumCreditsByUserInWindow(eq(TxnType.JOB_RELEASE), eq(START), eq(END)))
                .thenReturn(List.<Object[]>of(new Object[]{userId, new BigDecimal("100.00")}));
        when(earningsRepository.findByUserIdAndSnapshotDate(userId, DAY)).thenReturn(Optional.of(existing));
        when(earningsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        job.aggregateEarnings(DAY);

        ArgumentCaptor<EarningsDailySnapshot> captor = ArgumentCaptor.forClass(EarningsDailySnapshot.class);
        verify(earningsRepository).save(captor.capture());
        // Same instance reused (not a new builder)
        assertThat(captor.getValue()).isSameAs(existing);
        assertThat(existing.getEarnedFromFreelanceMxc()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    // ── H3.8: zero-balance when user has no wallets ───────────────────────────

    @Test
    void aggregate_writesZeroBalanceSnapshotWhenUserHasNoWallets() {
        UUID userId = UUID.randomUUID();

        when(transactionRepository.sumCreditsByUserInWindow(eq(TxnType.JOB_RELEASE), eq(START), eq(END)))
                .thenReturn(List.<Object[]>of(new Object[]{userId, new BigDecimal("200.00")}));
        stubUser(userId);
        when(earningsRepository.findByUserIdAndSnapshotDate(userId, DAY)).thenReturn(Optional.empty());
        when(earningsRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        // No wallets found
        when(walletRepository.findByUserIdAndAccountType(eq(userId), any())).thenReturn(Optional.empty());

        job.aggregateEarnings(DAY);

        ArgumentCaptor<EarningsDailySnapshot> captor = ArgumentCaptor.forClass(EarningsDailySnapshot.class);
        verify(earningsRepository).save(captor.capture());
        EarningsDailySnapshot snapshot = captor.getValue();

        // Balance fields should remain at default zero (not null)
        assertThat(snapshot.getAvailableBalanceMxc()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(snapshot.getEscrowBalanceMxc()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User stubUser(UUID userId) {
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        return user;
    }
}

