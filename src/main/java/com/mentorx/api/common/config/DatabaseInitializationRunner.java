package com.mentorx.api.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DatabaseInitializationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Value("${app.database.initialize-on-startup:true}")
    private boolean initializeOnStartup;

    @Value("${app.database.run-sample-data:true}")
    private boolean runSampleData;

    @Value("${app.database.schema-script:classpath:db/data/init-schema.sql}")
    private Resource schemaScript;

    @Value("${app.database.sample-data-script:classpath:db/data/sample-data.sql}")
    private Resource sampleDataScript;

    @Bean
    @Order(1)
    public CommandLineRunner initializeDatabaseOnStartup() {
        return args -> {
            if (!initializeOnStartup) {
                log.info("Database initialization is disabled by configuration");
                ensureOnboardingColumnsIfNeeded();
                ensureUserSavesTableIfNeeded();
                ensureMentorVerificationColumnsIfNeeded();
                ensureUserBankAccountPayoutColumnsIfNeeded();
                ensureMentorStatusEnumValuesIfNeeded();
                ensureDepositGatewayConstraintUpdated();
                ensureExchangeRateTablesIfNeeded();
                ensureWalletMonetarySnapshotColumnsIfNeeded();
                ensureWithdrawalPayoutColumnsIfNeeded();
                ensureEmailVerificationTablesIfNeeded();
                ensurePasswordResetTablesIfNeeded();
                return;
            }

            if (isSchemaAlreadyCreated()) {
                log.info("Database schema already exists, skipping schema initialization");
                ensureOnboardingColumnsIfNeeded();
                ensureUserSavesTableIfNeeded();
                ensureMentorVerificationColumnsIfNeeded();
                ensureUserBankAccountPayoutColumnsIfNeeded();
                ensureMentorStatusEnumValuesIfNeeded();
                ensureDepositGatewayConstraintUpdated();
                ensureExchangeRateTablesIfNeeded();
                ensureWalletMonetarySnapshotColumnsIfNeeded();
                ensureWithdrawalPayoutColumnsIfNeeded();
                ensureEmailVerificationTablesIfNeeded();
                ensurePasswordResetTablesIfNeeded();
                return;
            }

            log.info("Database schema not found. Starting initialization scripts...");
            List<Resource> scripts = new ArrayList<>();
            scripts.add(schemaScript);

            if (runSampleData) {
                scripts.add(sampleDataScript);
            }

            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.setContinueOnError(false);
            populator.setIgnoreFailedDrops(true);

            for (Resource script : scripts) {
                if (script == null || !script.exists()) {
                    log.warn("Initialization script not found, skipped");
                    continue;
                }

                String scriptName = StringUtils.hasText(script.getFilename()) ? script.getFilename() : script.getDescription();
                log.info("Executing initialization script: {}", scriptName);
                populator.addScript(script);
            }

            populator.execute(Objects.requireNonNull(dataSource));
            log.info("Database initialization finished successfully");
            ensureOnboardingColumnsIfNeeded();
            ensureUserSavesTableIfNeeded();
            ensureMentorVerificationColumnsIfNeeded();
            ensureUserBankAccountPayoutColumnsIfNeeded();
            ensureMentorStatusEnumValuesIfNeeded();
            ensureDepositGatewayConstraintUpdated();
            ensureExchangeRateTablesIfNeeded();
            ensureWalletMonetarySnapshotColumnsIfNeeded();
            ensureWithdrawalPayoutColumnsIfNeeded();
            ensureEmailVerificationTablesIfNeeded();
            ensurePasswordResetTablesIfNeeded();
        };
    }

    private void ensureExchangeRateTablesIfNeeded() {
        if (!isSchemaAlreadyCreated()) {
            return;
        }
        log.info("Ensuring exchange_rates table exists...");
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS exchange_rates (
                    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                    from_currency VARCHAR(10) NOT NULL,
                    to_currency VARCHAR(10) NOT NULL,
                    rate NUMERIC(19, 6) NOT NULL,
                    source VARCHAR(100),
                    effective_at TIMESTAMPTZ NOT NULL,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
                """);
        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_exchange_rates_lookup
                ON exchange_rates(from_currency, to_currency, effective_at DESC, created_at DESC)
                """);
    }

    private void ensureWalletMonetarySnapshotColumnsIfNeeded() {
        if (!isSchemaAlreadyCreated()) {
            return;
        }
        log.info("Ensuring monetary snapshot columns exist on deposit_orders and wallet_transactions...");
        jdbcTemplate.execute("ALTER TABLE deposit_orders ADD COLUMN IF NOT EXISTS converted_amount_vnd NUMERIC(19, 2)");
        jdbcTemplate.execute("UPDATE deposit_orders SET converted_amount_vnd = real_amount WHERE converted_amount_vnd IS NULL");
        jdbcTemplate.execute("ALTER TABLE wallet_transactions ADD COLUMN IF NOT EXISTS original_amount NUMERIC(19, 6)");
        jdbcTemplate.execute("ALTER TABLE wallet_transactions ADD COLUMN IF NOT EXISTS original_currency VARCHAR(10)");
        jdbcTemplate.execute("ALTER TABLE wallet_transactions ADD COLUMN IF NOT EXISTS exchange_rate_to_vnd NUMERIC(19, 6)");
        jdbcTemplate.execute("ALTER TABLE wallet_transactions ADD COLUMN IF NOT EXISTS converted_amount_vnd NUMERIC(19, 2)");
        jdbcTemplate.execute("ALTER TABLE wallet_transactions ADD COLUMN IF NOT EXISTS gateway VARCHAR(30)");
        jdbcTemplate.execute("ALTER TABLE wallet_transactions ADD COLUMN IF NOT EXISTS gateway_transaction_id VARCHAR(255)");
    }

    private void ensureWithdrawalPayoutColumnsIfNeeded() {
        if (!isSchemaAlreadyCreated()) {
            return;
        }
        log.info("Ensuring withdrawal payout columns exist...");
        jdbcTemplate.execute("ALTER TABLE withdrawal_requests ADD COLUMN IF NOT EXISTS payout_country VARCHAR(10)");
        jdbcTemplate.execute("ALTER TABLE withdrawal_requests ADD COLUMN IF NOT EXISTS payout_method VARCHAR(40)");
        jdbcTemplate.execute("ALTER TABLE withdrawal_requests ADD COLUMN IF NOT EXISTS payout_reference VARCHAR(255)");
    }

    private void ensureDepositGatewayConstraintUpdated() {
        if (!isSchemaAlreadyCreated()) {
            return;
        }
        log.info("Ensuring deposit_orders gateway constraint includes MOMO and PAYOS...");
        try {
            // Drop old constraint if exists
            jdbcTemplate.execute("ALTER TABLE deposit_orders DROP CONSTRAINT IF EXISTS deposit_orders_gateway_check");
            // Re-add constraint with gateway values used by the application
            // We use standard names as defined by Hibernate/JPA auto-creation
            jdbcTemplate.execute("ALTER TABLE deposit_orders ADD CONSTRAINT deposit_orders_gateway_check CHECK (gateway IN ('VNPAY', 'MOMO', 'PAYOS', 'STRIPE', 'MANUAL'))");
        } catch (Exception e) {
            log.warn("Could not update deposit_orders_gateway_check constraint: {}. This might be expected if the table or constraint doesn't exist yet.", e.getMessage());
        }
    }

    private boolean isSchemaAlreadyCreated() {
        Integer tableCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name = 'users'
                """, Integer.class);
        return tableCount != null && tableCount > 0;
    }

    /**
     * Backward-compatible schema patch for existing DBs created before onboarding JSON state columns.
     */
    private void ensureOnboardingColumnsIfNeeded() {
        if (!isSchemaAlreadyCreated()) {
            return;
        }
        log.info("Ensuring onboarding columns exist on users table...");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS onboarding_state JSONB");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS is_onboarded BOOLEAN NOT NULL DEFAULT FALSE");
    }

    private void ensureUserSavesTableIfNeeded() {
        if (!isSchemaAlreadyCreated()) {
            return;
        }
        log.info("Ensuring user_saves table exists...");
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS user_saves (
                    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    target_type VARCHAR(30) NOT NULL,
                    target_id   UUID NOT NULL,
                    saved_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    CONSTRAINT uq_user_saves_target UNIQUE (user_id, target_type, target_id)
                )
                """);
        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_user_saves_user_type
                    ON user_saves(user_id, target_type, saved_at DESC)
                """);
        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_user_saves_target
                    ON user_saves(target_type, target_id)
                """);
    }

    private void ensureUserBankAccountPayoutColumnsIfNeeded() {
        if (!isSchemaAlreadyCreated()) {
            return;
        }
        log.info("Ensuring user bank account payout columns exist...");
        jdbcTemplate.execute("ALTER TABLE user_bank_accounts ADD COLUMN IF NOT EXISTS payout_country VARCHAR(10)");
        jdbcTemplate.execute("ALTER TABLE user_bank_accounts ADD COLUMN IF NOT EXISTS payout_method VARCHAR(40)");
        jdbcTemplate.execute("ALTER TABLE user_bank_accounts ADD COLUMN IF NOT EXISTS iban VARCHAR(80)");
        jdbcTemplate.execute("ALTER TABLE user_bank_accounts ADD COLUMN IF NOT EXISTS swift_code VARCHAR(40)");
        jdbcTemplate.execute("ALTER TABLE user_bank_accounts ADD COLUMN IF NOT EXISTS paypal_email VARCHAR(255)");
        jdbcTemplate.execute("ALTER TABLE user_bank_accounts ADD COLUMN IF NOT EXISTS wise_email VARCHAR(255)");
        jdbcTemplate.execute("ALTER TABLE user_bank_accounts ADD COLUMN IF NOT EXISTS stripe_connect_account_id VARCHAR(255)");
    }

    private void ensureMentorVerificationColumnsIfNeeded() {
        if (!isSchemaAlreadyCreated()) {
            return;
        }
        log.info("Ensuring mentor verification columns exist...");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS video_intro_url TEXT");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS location VARCHAR(150)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS languages JSONB");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS legal_name VARCHAR(150)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS date_of_birth DATE");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS country_of_residence VARCHAR(100)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS identity_document_type VARCHAR(50)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS identity_document_url TEXT");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS portrait_url TEXT");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS phone_number VARCHAR(30)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS phone_verified BOOLEAN NOT NULL DEFAULT FALSE");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS current_title VARCHAR(150)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS current_company VARCHAR(150)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS primary_domain VARCHAR(120)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS linkedin_url TEXT");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS github_url TEXT");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS portfolio_evidence_url TEXT");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS certificate_url TEXT");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS bank_account_name VARCHAR(150)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS bank_name VARCHAR(150)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS bank_account_number VARCHAR(80)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS bank_branch VARCHAR(150)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS tax_id VARCHAR(80)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS mentor_agreement_accepted BOOLEAN NOT NULL DEFAULT FALSE");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS dispute_policy_accepted BOOLEAN NOT NULL DEFAULT FALSE");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS identity_document_back_url TEXT");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS verification_metadata JSONB");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMPTZ");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS expertise_status VARCHAR(30) NOT NULL DEFAULT 'NOT_SUBMITTED'");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS expertise_review_note TEXT");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS expertise_rejection_reason TEXT");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS expertise_reviewed_by UUID REFERENCES users(id)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS expertise_reviewed_at TIMESTAMPTZ");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS resubmission_allowed BOOLEAN NOT NULL DEFAULT TRUE");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS identity_status VARCHAR(30) NOT NULL DEFAULT 'NOT_SUBMITTED'");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS identity_required BOOLEAN NOT NULL DEFAULT FALSE");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS document_number_masked VARCHAR(40)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS identity_verified_at TIMESTAMPTZ");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS identity_verified_by UUID REFERENCES users(id)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS identity_rejection_reason TEXT");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS verification_provider VARCHAR(120)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS payout_status VARCHAR(30) NOT NULL DEFAULT 'NOT_SUBMITTED'");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS payout_country VARCHAR(10)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS payout_method VARCHAR(40)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS iban VARCHAR(80)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS swift_code VARCHAR(40)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS paypal_email VARCHAR(255)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS wise_email VARCHAR(255)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS stripe_connect_account_id VARCHAR(255)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS payout_rejection_reason TEXT");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS payout_reviewed_by UUID REFERENCES users(id)");
        jdbcTemplate.execute("ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS payout_reviewed_at TIMESTAMPTZ");
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS mentor_profile_assets (
                    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                    mentor_profile_id UUID NOT NULL REFERENCES mentor_profiles(id) ON DELETE CASCADE,
                    asset_type        VARCHAR(30) NOT NULL,
                    title             VARCHAR(200) NOT NULL,
                    description       TEXT,
                    issuer            VARCHAR(150),
                    file_url          TEXT,
                    icon_url          TEXT,
                    issued_at         DATE,
                    is_featured       BOOLEAN NOT NULL DEFAULT FALSE,
                    display_order     INTEGER DEFAULT 0,
                    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
                """);
        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_mentor_profile_assets_profile
                    ON mentor_profile_assets(mentor_profile_id, asset_type, display_order)
                """);
        jdbcTemplate.execute("""
                CREATE OR REPLACE FUNCTION fn_set_updated_at()
                RETURNS TRIGGER AS $$
                BEGIN
                    NEW.updated_at = NOW();
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql;
                """);
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_mentor_profile_assets_updated_at ON mentor_profile_assets");
        jdbcTemplate.execute("""
                CREATE TRIGGER trg_mentor_profile_assets_updated_at
                    BEFORE UPDATE ON mentor_profile_assets FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at()
                """);
    }

    /**
     * Existing databases may still use the short mentor_status enum. JPA uses {@code MentorStatus}
     * string values such as KYC_SUBMITTED; extend the PostgreSQL enum when missing.
     */
    private void ensureMentorStatusEnumValuesIfNeeded() {
        if (!isSchemaAlreadyCreated()) {
            return;
        }
        log.info("Ensuring mentor_status enum includes KYC workflow values...");
        try {
            Integer typeCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM pg_type WHERE typname = 'mentor_status'",
                    Integer.class);
            if (typeCount == null || typeCount == 0) {
                log.debug("No PostgreSQL type mentor_status found; skipping enum extension.");
                return;
            }
        } catch (Exception e) {
            log.warn("Could not inspect mentor_status type: {}", e.getMessage());
            return;
        }
        String[] labels = {"NOT_APPLIED", "PENDING_KYC", "KYC_SUBMITTED", "KYC_VERIFIED", "KYC_REJECTED", "ACTIVE"};
        for (String label : labels) {
            addMentorStatusEnumValueIfMissing(label);
        }
    }

    private void ensureEmailVerificationTablesIfNeeded() {
        if (!isSchemaAlreadyCreated()) {
            return;
        }
        log.info("Ensuring email verification token table exists...");
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS email_verification_tokens (
                    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                    user_id                 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    token                   VARCHAR(255) NOT NULL UNIQUE,
                    email                   VARCHAR(255) NOT NULL,
                    expires_at              TIMESTAMPTZ NOT NULL,
                    is_used                 BOOLEAN NOT NULL DEFAULT FALSE,
                    used_at                 TIMESTAMPTZ,
                    request_ip              VARCHAR(45),
                    verification_ip         VARCHAR(45),
                    request_user_agent      VARCHAR(500),
                    verification_user_agent VARCHAR(500),
                    attempt_count           INTEGER NOT NULL DEFAULT 0,
                    last_attempt_at         TIMESTAMPTZ,
                    is_resend               BOOLEAN NOT NULL DEFAULT FALSE,
                    original_token_id       UUID,
                    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
                """);
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_email_token_user_id ON email_verification_tokens(user_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_email_token_token ON email_verification_tokens(token)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_email_token_email ON email_verification_tokens(email)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_email_token_expires ON email_verification_tokens(expires_at)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_email_token_used ON email_verification_tokens(is_used)");
    }

    private void ensurePasswordResetTablesIfNeeded() {
        if (!isSchemaAlreadyCreated()) {
            return;
        }
        log.info("Ensuring password reset token table exists...");
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS password_reset_tokens (
                    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                    user_id            UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    token              VARCHAR(255) NOT NULL UNIQUE,
                    expires_at         TIMESTAMPTZ NOT NULL,
                    is_used            BOOLEAN NOT NULL DEFAULT FALSE,
                    used_at            TIMESTAMPTZ,
                    request_ip         VARCHAR(45),
                    reset_ip           VARCHAR(45),
                    request_user_agent VARCHAR(500),
                    reset_user_agent   VARCHAR(500),
                    attempt_count      INTEGER NOT NULL DEFAULT 0,
                    last_attempt_at    TIMESTAMPTZ,
                    is_invalidated     BOOLEAN NOT NULL DEFAULT FALSE,
                    invalidated_at     TIMESTAMPTZ,
                    invalidation_reason VARCHAR(200),
                    email              VARCHAR(255),
                    security_answer_hash VARCHAR(255),
                    security_verified  BOOLEAN NOT NULL DEFAULT FALSE,
                    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
                """);
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_password_token_user_id ON password_reset_tokens(user_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_password_token_token ON password_reset_tokens(token)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_password_token_expires ON password_reset_tokens(expires_at)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_password_token_used ON password_reset_tokens(is_used)");
    }

    private void addMentorStatusEnumValueIfMissing(String label) {
        try {
            Boolean exists = jdbcTemplate.queryForObject("""
                            SELECT EXISTS (
                                SELECT 1 FROM pg_enum e
                                JOIN pg_type t ON e.enumtypid = t.oid
                                WHERE t.typname = 'mentor_status' AND e.enumlabel = ?
                            )
                            """,
                    Boolean.class, label);
            if (Boolean.TRUE.equals(exists)) {
                return;
            }
            jdbcTemplate.execute("ALTER TYPE mentor_status ADD VALUE '" + label + "'");
            log.info("Added mentor_status enum value: {}", label);
        } catch (Exception e) {
            log.warn("Could not add mentor_status enum value {}: {}", label, e.getMessage());
        }
    }
}
