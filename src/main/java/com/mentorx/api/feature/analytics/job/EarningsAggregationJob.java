package com.mentorx.api.feature.analytics.job;

import com.mentorx.api.common.enums.TxnType;
import com.mentorx.api.common.enums.WalletAccountType;
import com.mentorx.api.feature.analytics.entity.CourseDailySnapshot;
import com.mentorx.api.feature.analytics.entity.EarningsDailySnapshot;
import com.mentorx.api.feature.analytics.repository.CourseDailySnapshotRepository;
import com.mentorx.api.feature.analytics.repository.EarningsDailySnapshotRepository;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.repository.CourseEnrollmentRepository;
import com.mentorx.api.feature.course.repository.CourseRepository;
import com.mentorx.api.feature.job.repository.ContractRepository;
import com.mentorx.api.feature.job.repository.ProposalRepository;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.wallet.entity.Wallet;
import com.mentorx.api.feature.wallet.repository.WalletRepository;
import com.mentorx.api.feature.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Phase 1.5 / 1.6 / 2.3: nightly aggregation job that fills
 * {@code earnings_daily_snapshots} and {@code course_daily_snapshots} from the source-of-truth
 * tables. Runs at 02:30 (30 min after {@code FeedPrecomputationJob} at 02:00) to avoid
 * DB contention.
 *
 * <p>Source attribution (DEC-008 Option 3): wallet transactions for the totals, plus
 * contract / enrollment rows for the by-source split.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EarningsAggregationJob {

    private final EarningsDailySnapshotRepository earningsRepository;
    private final CourseDailySnapshotRepository courseDailyRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final ContractRepository contractRepository;
    private final ProposalRepository proposalRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    /**
     * Runs daily at 02:30 server time. Aggregates the previous calendar day in UTC.
     */
    @Scheduled(cron = "0 30 2 * * *")
    @Transactional
    public void aggregate() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("[Analytics] Earnings aggregation job starting for {}", yesterday);
        long startMs = System.currentTimeMillis();

        int earnings = aggregateEarnings(yesterday);
        int courses = aggregateCourseSnapshots(yesterday);

        long durationMs = System.currentTimeMillis() - startMs;
        log.info("[Analytics] Earnings aggregation job done: {} earnings rows, {} course rows in {} ms",
                earnings, courses, durationMs);
    }

    /**
     * Manual trigger for testing / replay.
     */
    public int aggregateEarnings(LocalDate day) {
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = day.plusDays(1).atStartOfDay();

        Map<UUID, EarningsRow> byUser = new HashMap<>();

        // JOB_RELEASE credits → earned_from_freelance_mxc
        for (Object[] row : transactionRepository.sumCreditsByUserInWindow(
                TxnType.JOB_RELEASE, start, end)) {
            UUID userId = (UUID) row[0];
            BigDecimal amount = toBigDecimal(row[1]);
            byUser.computeIfAbsent(userId, id -> new EarningsRow())
                    .freelanceMxc = amount;
        }

        // COURSE_PURCHASE credits → earned_from_courses_mxc
        for (Object[] row : transactionRepository.sumCreditsByUserInWindow(
                TxnType.COURSE_PURCHASE, start, end)) {
            UUID userId = (UUID) row[0];
            BigDecimal amount = toBigDecimal(row[1]);
            byUser.computeIfAbsent(userId, id -> new EarningsRow())
                    .coursesMxc = amount;
        }

        // Withdrawals (DEBIT side) → withdrawn_mxc
        for (Object[] row : transactionRepository.sumCreditsByUserInWindow(
                TxnType.WITHDRAWAL, start, end)) {
            // WITHDRAWAL is a DEBIT not a CREDIT — we use a dedicated query path below.
            // The helper name above is a misnomer for DEBIT flows; ignore this loop result.
            // (Kept for source attribution symmetry; the actual DEBIT sum is computed separately.)
            if (row[0] != null) {
                UUID userId = (UUID) row[0];
                byUser.computeIfAbsent(userId, id -> new EarningsRow());
            }
        }

        // DEBIT-side: WITHDRAWAL is a DEBIT for the user — fetch a separate sum.
        Map<UUID, BigDecimal> withdrawals = sumDebitsByUser(TxnType.WITHDRAWAL, start, end);
        withdrawals.forEach((uid, amount) ->
                byUser.computeIfAbsent(uid, id -> new EarningsRow()).withdrawnMxc = amount);

        Map<UUID, BigDecimal> platformFees = sumDebitsByUser(TxnType.PLATFORM_FEE, start, end);
        platformFees.forEach((uid, amount) ->
                byUser.computeIfAbsent(uid, id -> new EarningsRow()).platformFeeMxc = amount);

        // Contract completions → jobs_completed
        for (Object[] row : contractRepository.countCompletedByMentorInWindow(start, end)) {
            UUID userId = (UUID) row[0];
            long completed = ((Number) row[1]).longValue();
            byUser.computeIfAbsent(userId, id -> new EarningsRow()).jobsCompleted = (int) completed;
        }

        // Proposals sent / accepted
        for (Object[] row : proposalRepository.countProposalsByMentorInWindow(start, end)) {
            UUID userId = (UUID) row[0];
            long count = ((Number) row[1]).longValue();
            byUser.computeIfAbsent(userId, id -> new EarningsRow()).proposalsSent = (int) count;
        }
        for (Object[] row : proposalRepository.countAcceptedProposalsByMentorInWindow(start, end)) {
            UUID userId = (UUID) row[0];
            long count = ((Number) row[1]).longValue();
            byUser.computeIfAbsent(userId, id -> new EarningsRow()).proposalsAccepted = (int) count;
        }

        // Active contracts as of the snapshot date
        long active = contractRepository.count();
        byUser.values().forEach(r -> r.contractsActive = (int) active);

        // Enrollments per course → course_enrollments (rollup per user via their courses)
        Map<UUID, Integer> enrollmentsByInstructor = aggregateEnrollmentsByInstructor(start, end);
        enrollmentsByInstructor.forEach((uid, count) ->
                byUser.computeIfAbsent(uid, id -> new EarningsRow()).courseEnrollments = count);

        // Persist snapshots
        AtomicInteger written = new AtomicInteger(0);
        for (Map.Entry<UUID, EarningsRow> entry : byUser.entrySet()) {
            UUID userId = entry.getKey();
            EarningsRow row = entry.getValue();
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) continue;

            EarningsDailySnapshot snapshot = earningsRepository
                    .findByUserIdAndSnapshotDate(userId, day)
                    .orElseGet(() -> EarningsDailySnapshot.builder()
                            .user(user)
                            .snapshotDate(day)
                            .build());

            snapshot.setEarnedMxc(row.freelanceMxc.add(row.coursesMxc).add(row.mentoringMxc));
            snapshot.setEarnedFromFreelanceMxc(row.freelanceMxc);
            snapshot.setEarnedFromCoursesMxc(row.coursesMxc);
            snapshot.setEarnedFromMentoringMxc(row.mentoringMxc);
            snapshot.setWithdrawnMxc(row.withdrawnMxc);
            snapshot.setPlatformFeeMxc(row.platformFeeMxc);
            snapshot.setJobsCompleted(row.jobsCompleted);
            snapshot.setCoursesSold(row.coursesSold);
            snapshot.setProposalsSent(row.proposalsSent);
            snapshot.setProposalsAccepted(row.proposalsAccepted);
            snapshot.setContractsActive(row.contractsActive);
            snapshot.setContractsCompleted(row.jobsCompleted);
            snapshot.setCourseEnrollments(row.courseEnrollments);

            // Balance snapshots
            walletRepository.findByUserIdAndAccountType(userId, WalletAccountType.USER_AVAILABLE)
                    .ifPresent(w -> snapshot.setAvailableBalanceMxc(w.getBalanceMxc()));
            walletRepository.findByUserIdAndAccountType(userId, WalletAccountType.USER_PENDING)
                    .ifPresent(w -> snapshot.setEscrowBalanceMxc(w.getBalanceMxc()));

            earningsRepository.save(snapshot);
            written.incrementAndGet();
        }
        return written.get();
    }

    private int aggregateCourseSnapshots(LocalDate day) {
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = day.plusDays(1).atStartOfDay();

        Map<UUID, CourseAgg> byCourse = new HashMap<>();
        for (Object[] row : enrollmentRepository.countEnrollmentsByCourseInWindow(start, end)) {
            UUID courseId = (UUID) row[0];
            long count = ((Number) row[1]).longValue();
            byCourse.computeIfAbsent(courseId, id -> new CourseAgg()).enrollments = (int) count;
        }
        for (Object[] row : enrollmentRepository.revenueByCourseInWindow(start, end)) {
            UUID courseId = (UUID) row[0];
            BigDecimal revenue = toBigDecimal(row[2]);
            byCourse.computeIfAbsent(courseId, id -> new CourseAgg()).revenue = revenue;
        }

        AtomicInteger written = new AtomicInteger(0);
        for (Map.Entry<UUID, CourseAgg> entry : byCourse.entrySet()) {
            UUID courseId = entry.getKey();
            CourseAgg agg = entry.getValue();
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) continue;

            CourseDailySnapshot snapshot = courseDailyRepository
                    .findByCourseIdAndSnapshotDate(courseId, day)
                    .orElseGet(() -> CourseDailySnapshot.builder()
                            .course(course)
                            .snapshotDate(day)
                            .build());

            snapshot.setEnrollmentsCount(agg.enrollments);
            snapshot.setSoldCount(agg.enrollments);
            snapshot.setRevenueMxc(agg.revenue);

            courseDailyRepository.save(snapshot);
            written.incrementAndGet();
        }
        return written.get();
    }

    private Map<UUID, BigDecimal> sumDebitsByUser(TxnType type, LocalDateTime start, LocalDateTime end) {
        // Walk raw transactions because the helper is CREDIT-only. For small windows this is fine.
        // We use a derived query via transactionRepository to keep the dependency one-way.
        Map<UUID, BigDecimal> result = new HashMap<>();
        // Use a custom in-memory aggregation: the volume per day is bounded by user transaction count.
        List<com.mentorx.api.feature.wallet.entity.WalletTransaction> txs =
                transactionRepository.findTransactionsBetweenDates(start, end);
        for (var tx : txs) {
            if (tx.getTxnType() != type) continue;
            if (tx.getDirection() != com.mentorx.api.common.enums.LedgerDirection.DEBIT) continue;
            if (tx.getWallet() == null || tx.getWallet().getUser() == null) continue;
            UUID uid = tx.getWallet().getUser().getId();
            result.merge(uid, tx.getAmountMxc() == null ? BigDecimal.ZERO : tx.getAmountMxc(), BigDecimal::add);
        }
        return result;
    }

    private Map<UUID, Integer> aggregateEnrollmentsByInstructor(LocalDateTime start, LocalDateTime end) {
        Map<UUID, Integer> out = new HashMap<>();
        for (Object[] row : enrollmentRepository.revenueByCourseInWindow(start, end)) {
            UUID instructorId = (UUID) row[1];
            int count = enrollmentRepository.countEnrollmentsByCourseInWindow(start, end).stream()
                    .filter(r -> r[0].equals(row[0]))
                    .map(r -> ((Number) r[1]).intValue())
                    .findFirst()
                    .orElse(0);
            out.merge(instructorId, count, Integer::sum);
        }
        return out;
    }

    private BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal bd) return bd;
        if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }

    private static class EarningsRow {
        BigDecimal freelanceMxc = BigDecimal.ZERO;
        BigDecimal coursesMxc   = BigDecimal.ZERO;
        BigDecimal mentoringMxc = BigDecimal.ZERO;
        BigDecimal withdrawnMxc = BigDecimal.ZERO;
        BigDecimal platformFeeMxc = BigDecimal.ZERO;
        int jobsCompleted = 0;
        int coursesSold = 0;
        int proposalsSent = 0;
        int proposalsAccepted = 0;
        int contractsActive = 0;
        int courseEnrollments = 0;
    }

    private static class CourseAgg {
        int enrollments = 0;
        BigDecimal revenue = BigDecimal.ZERO;
    }

    @SuppressWarnings("unused")
    private static class DurationLog {
        public static String of(long startMs) {
            return Duration.ofMillis(System.currentTimeMillis() - startMs).toString();
        }
    }
}
