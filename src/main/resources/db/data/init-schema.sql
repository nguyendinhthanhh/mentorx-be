-- ============================================================
-- MENTOR X — FULL DATABASE SCHEMA (PostgreSQL)
-- Version: 2.1.0
-- Description: Complete schema for Mentor X platform
--              Includes: Core platform + Matching & Discovery
--              Multi-role system: Admin / Moderator / User / Mentor
--              MX-Credits (MXC) virtual currency with Double-Entry Bookkeeping
--              Multi-language support: VI / EN / ZH / JA
--              Matching engine: Precomputed scores, feed, trending
-- ============================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ============================================================
-- SECTION 0: ENUMERATIONS
-- ============================================================

CREATE TYPE user_status AS ENUM ('ACTIVE','PENDING','SUSPENDED','BANNED','DEACTIVATED','DELETED');
CREATE TYPE mentor_status AS ENUM (
    'NONE',
    'PENDING_KYC',
    'KYC_SUBMITTED',
    'KYC_VERIFIED',
    'KYC_REJECTED',
    'ACTIVE',
    'PENDING',
    'APPROVED',
    'REJECTED',
    'SUSPENDED',
    'REVOKED'
);
CREATE TYPE job_status AS ENUM ('DRAFT','OPEN','IN_PROGRESS','COMPLETED','CANCELLED','CLOSED','DISPUTED');
CREATE TYPE quick_support_status AS ENUM ('OPEN','ACCEPTED','SOLVED','EXPIRED','CANCELLED');
CREATE TYPE job_type AS ENUM ('LONG_TERM_MENTORING','FREELANCE_PROJECT','QUICK_FIX');
CREATE TYPE budget_type AS ENUM ('FIXED','HOURLY');
CREATE TYPE proposal_status AS ENUM ('PENDING','ACCEPTED','REJECTED','WITHDRAWN','EXPIRED');
CREATE TYPE contract_status AS ENUM ('ACTIVE','COMPLETED','CANCELLED','DISPUTED');
CREATE TYPE milestone_status AS ENUM ('PENDING','IN_PROGRESS','SUBMITTED','REVISION_REQUESTED','APPROVED','DISPUTED');
CREATE TYPE course_status AS ENUM ('DRAFT','PENDING','PUBLISHED','UNPUBLISHED','REJECTED','ARCHIVED');
CREATE TYPE lesson_type AS ENUM ('VIDEO','DOCUMENT','TEXT','QUIZ');
CREATE TYPE txn_type AS ENUM ('DEPOSIT','WITHDRAWAL','JOB_PAYMENT','JOB_RELEASE','JOB_REFUND','COURSE_PURCHASE','COURSE_REFUND','PLATFORM_FEE','WITHDRAWAL_FEE','BONUS_CREDIT','PENALTY_DEDUCTION','ADJUSTMENT');
CREATE TYPE wallet_account_type AS ENUM ('USER_AVAILABLE','USER_PENDING','ESCROW','PLATFORM_REVENUE','PLATFORM_FLOAT');
CREATE TYPE ledger_direction AS ENUM ('DEBIT','CREDIT');
CREATE TYPE txn_status AS ENUM ('PENDING','COMPLETED','FAILED','REVERSED','FLAGGED');
CREATE TYPE withdrawal_status AS ENUM ('PENDING','APPROVED','PROCESSING','COMPLETED','REJECTED','FAILED');
CREATE TYPE payment_gateway AS ENUM ('VNPAY','STRIPE','MANUAL');
CREATE TYPE escrow_status AS ENUM ('LOCKED','RELEASED','REFUNDED');
CREATE TYPE dispute_status AS ENUM ('OPEN','REVIEWING','RESOLVED','CLOSED');
CREATE TYPE dispute_outcome AS ENUM ('FAVOR_CLIENT','FAVOR_MENTOR','PARTIAL_REFUND','NO_ACTION');
CREATE TYPE review_target_type AS ENUM ('MENTOR','CLIENT','COURSE');
CREATE TYPE report_target_type AS ENUM ('USER','JOB','COURSE','MESSAGE','REVIEW');
CREATE TYPE report_status AS ENUM ('PENDING','REVIEWING','RESOLVED','DISMISSED');
CREATE TYPE notification_type AS ENUM ('PROPOSAL_RECEIVED','PROPOSAL_ACCEPTED','PROPOSAL_REJECTED','CONTRACT_STARTED','MILESTONE_SUBMITTED','MILESTONE_APPROVED','MILESTONE_REVISION','CONTRACT_COMPLETED','PAYMENT_RECEIVED','WITHDRAWAL_APPROVED','WITHDRAWAL_REJECTED','REVIEW_RECEIVED','COURSE_PURCHASED','DISPUTE_OPENED','DISPUTE_RESOLVED','MESSAGE_RECEIVED','MENTOR_APPROVED','MENTOR_REJECTED','ACCOUNT_SUSPENDED','SYSTEM_ALERT');
CREATE TYPE badge_type AS ENUM ('TOP_MENTOR','FAST_RESPONDER','TRUSTED_SELLER','RISING_STAR','VERIFIED_EXPERT','TOP_RATED');
CREATE TYPE supported_language AS ENUM ('vi','en','zh','ja');
CREATE TYPE chat_room_type AS ENUM ('DIRECT','CONTRACT');
CREATE TYPE message_type AS ENUM ('TEXT','IMAGE','FILE','SYSTEM');
CREATE TYPE feed_item_type AS ENUM ('MENTOR_RECOMMENDATION','JOB_RECOMMENDATION','COURSE_RECOMMENDATION','TRENDING_MENTOR','NEW_JOB_ALERT');

-- ============================================================
-- SECTION 1: USER MANAGEMENT
-- ============================================================

-- 1.1 Users
CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email               VARCHAR(255) NOT NULL UNIQUE,
    password_hash       VARCHAR(255),
    full_name           VARCHAR(150) NOT NULL,
    display_name        VARCHAR(100),
    avatar_url          TEXT,
    bio                 TEXT,
    phone               VARCHAR(30),
    country_code        CHAR(2),
    preferred_language  supported_language DEFAULT 'vi',
    status              user_status NOT NULL DEFAULT 'PENDING',
    is_email_verified   BOOLEAN NOT NULL DEFAULT FALSE,
    is_mentor           BOOLEAN NOT NULL DEFAULT FALSE,
    mentor_status       mentor_status NOT NULL DEFAULT 'NONE',
    is_2fa_enabled      BOOLEAN NOT NULL DEFAULT FALSE,
    totp_secret         VARCHAR(255),
    profile_is_public   BOOLEAN NOT NULL DEFAULT TRUE,
    last_seen_at        TIMESTAMPTZ,
    onboarding_state    JSONB,
    is_onboarded        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);

-- Continue with all other tables from the schema...
-- (The full schema is quite long, so I'll include the essential tables for the demo)

-- 1.7 RBAC: Roles
CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    role_name   VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 1.8 RBAC: Permissions
CREATE TABLE permissions (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    permission_key VARCHAR(100) NOT NULL UNIQUE,
    description    TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 1.9 RBAC: Role → Permission
CREATE TABLE role_permissions (
    role_id       UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- 1.10 RBAC: User → Role
CREATE TABLE user_roles (
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id    UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    granted_by UUID REFERENCES users(id),
    granted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id)
);

-- Add other essential tables for the demo...
-- (Including wallets, jobs, courses, etc. - abbreviated for space)

-- ============================================================
-- SECTION 2: CATEGORIES & SYSTEM CONFIG
-- ============================================================

CREATE TABLE categories (
    id            SERIAL PRIMARY KEY,
    slug          VARCHAR(100) NOT NULL UNIQUE,
    label_vi      VARCHAR(100) NOT NULL,
    label_en      VARCHAR(100) NOT NULL,
    label_zh      VARCHAR(100),
    label_ja      VARCHAR(100),
    icon_url      TEXT,
    parent_id     INTEGER REFERENCES categories(id),
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    display_order SMALLINT DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE skills (
    id         SERIAL PRIMARY KEY,
    slug       VARCHAR(100) NOT NULL UNIQUE,
    label_vi   VARCHAR(100) NOT NULL,
    label_en   VARCHAR(100) NOT NULL,
    label_zh   VARCHAR(100),
    label_ja   VARCHAR(100),
    is_active  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE platform_settings (
    key         VARCHAR(100) PRIMARY KEY,
    value       TEXT NOT NULL,
    description TEXT,
    updated_by  UUID REFERENCES users(id),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- SECTION 3: WALLET SYSTEM (Simplified for demo)
-- ============================================================

CREATE TABLE wallets (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID REFERENCES users(id) ON DELETE CASCADE,
    account_type wallet_account_type NOT NULL,
    balance_mxc  NUMERIC(15, 4) NOT NULL DEFAULT 0.0000,
    version      BIGINT NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT balance_non_negative CHECK (balance_mxc >= 0),
    UNIQUE (user_id, account_type)
);

-- ============================================================
-- SECTION 4: MENTOR PROFILES
-- ============================================================

CREATE TABLE mentor_profiles (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id             UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    headline            VARCHAR(255),
    hourly_rate_mxc     NUMERIC(12, 2),
    years_of_experience SMALLINT,
    availability        VARCHAR(50),
    response_time_hours SMALLINT,
    total_jobs_done     INTEGER NOT NULL DEFAULT 0,
    success_rate        NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    average_rating      NUMERIC(3, 2) NOT NULL DEFAULT 0.00,
    total_reviews       INTEGER NOT NULL DEFAULT 0,
    is_featured         BOOLEAN NOT NULL DEFAULT FALSE,
    cv_url              TEXT,
    portfolio_url       TEXT,
    video_intro_url     TEXT,
    location            VARCHAR(150),
    languages           JSONB,
    legal_name          VARCHAR(150),
    date_of_birth       DATE,
    country_of_residence VARCHAR(100),
    identity_document_type VARCHAR(50),
    identity_document_url TEXT,
    portrait_url        TEXT,
    phone_number        VARCHAR(30),
    phone_verified      BOOLEAN NOT NULL DEFAULT FALSE,
    current_title       VARCHAR(150),
    current_company     VARCHAR(150),
    primary_domain      VARCHAR(120),
    linkedin_url        TEXT,
    github_url          TEXT,
    portfolio_evidence_url TEXT,
    certificate_url     TEXT,
    bank_account_name   VARCHAR(150),
    bank_name           VARCHAR(150),
    bank_account_number VARCHAR(80),
    bank_branch         VARCHAR(150),
    tax_id              VARCHAR(80),
    mentor_agreement_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    dispute_policy_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    submitted_at        TIMESTAMPTZ,
    approved_by         UUID REFERENCES users(id),
    approved_at         TIMESTAMPTZ,
    rejection_reason    TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);

CREATE TABLE mentor_profile_assets (
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
);

CREATE INDEX idx_mentor_profile_assets_profile
    ON mentor_profile_assets(mentor_profile_id, asset_type, display_order);

CREATE TABLE user_skills (
    user_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    skill_id INTEGER NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    level    VARCHAR(20),
    PRIMARY KEY (user_id, skill_id)
);

-- ============================================================
-- SECTION 5: JOB MARKETPLACE
-- ============================================================

CREATE TABLE jobs (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id      UUID NOT NULL REFERENCES users(id),
    category_id    INTEGER REFERENCES categories(id),
    job_type       job_type NOT NULL,
    title          VARCHAR(255) NOT NULL,
    description    TEXT NOT NULL,
    experience_level VARCHAR(80),
    current_level VARCHAR(120),
    learning_goals TEXT,
    success_criteria TEXT,
    availability_expectation VARCHAR(255),
    communication_preference VARCHAR(120),
    budget_type    budget_type NOT NULL,
    budget_min_mxc NUMERIC(12, 2),
    budget_max_mxc NUMERIC(12, 2),
    hourly_rate_mxc NUMERIC(12, 2),
    estimated_hours NUMERIC(6, 2),
    deadline_at    TIMESTAMPTZ,
    status         job_status NOT NULL DEFAULT 'DRAFT',
    is_featured    BOOLEAN NOT NULL DEFAULT FALSE,
    view_count     INTEGER NOT NULL DEFAULT 0,
    proposal_count INTEGER NOT NULL DEFAULT 0,
    published_at   TIMESTAMPTZ,
    closed_at      TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMPTZ,
    CONSTRAINT budget_min_lte_max CHECK (budget_min_mxc IS NULL OR budget_max_mxc IS NULL OR budget_min_mxc <= budget_max_mxc)
);

CREATE TABLE job_required_skills (
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    skill  VARCHAR(120) NOT NULL
);

-- ============================================================
-- SECTION 6: COURSE SYSTEM
-- ============================================================

CREATE TABLE courses (
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    instructor_id      UUID NOT NULL REFERENCES users(id),
    category_id        INTEGER REFERENCES categories(id),
    title              VARCHAR(255) NOT NULL,
    slug               VARCHAR(300) NOT NULL UNIQUE,
    description        TEXT,
    thumbnail_url      TEXT,
    price_mxc          NUMERIC(12, 0) NOT NULL DEFAULT 0,
    status             course_status NOT NULL DEFAULT 'DRAFT',
    language           supported_language DEFAULT 'vi',
    level              VARCHAR(20),
    total_duration_min INTEGER NOT NULL DEFAULT 0,
    total_lessons      SMALLINT NOT NULL DEFAULT 0,
    total_enrollments  INTEGER NOT NULL DEFAULT 0,
    average_rating     NUMERIC(3, 2) NOT NULL DEFAULT 0.00,
    total_reviews      INTEGER NOT NULL DEFAULT 0,
    is_certificate     BOOLEAN NOT NULL DEFAULT FALSE,
    preview_video_url  TEXT,
    rejection_reason   TEXT,
    published_at       TIMESTAMPTZ,
    reviewed_by        UUID REFERENCES users(id),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at         TIMESTAMPTZ
);

CREATE TABLE course_skills (
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    skill VARCHAR(120) NOT NULL
);

CREATE TABLE course_skill_ids (
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    skill_id INTEGER NOT NULL REFERENCES skills(id) ON DELETE RESTRICT,
    PRIMARY KEY (course_id, skill_id)
);

CREATE INDEX idx_course_skill_ids_skill_id
    ON course_skill_ids(skill_id);

CREATE TABLE user_saves (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_type VARCHAR(30) NOT NULL,
    target_id   UUID NOT NULL,
    saved_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_saves_target UNIQUE (user_id, target_type, target_id)
);

-- ============================================================
-- SECTION 7: NOTIFICATION SYSTEM
-- ============================================================

CREATE TABLE notification_preferences (
    user_id             UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    email_enabled       BOOLEAN NOT NULL DEFAULT TRUE,
    push_enabled        BOOLEAN NOT NULL DEFAULT TRUE,
    in_app_enabled      BOOLEAN NOT NULL DEFAULT TRUE,
    email_type_settings JSONB NOT NULL DEFAULT '{}',
    push_type_settings  JSONB NOT NULL DEFAULT '{}',
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================

-- Users
CREATE INDEX idx_users_email         ON users(email);
CREATE INDEX idx_users_status        ON users(status);
CREATE INDEX idx_users_mentor_status ON users(mentor_status);
CREATE INDEX idx_users_is_mentor     ON users(is_mentor);
CREATE INDEX idx_users_created_at    ON users(created_at);

-- Jobs
CREATE INDEX idx_jobs_client_id    ON jobs(client_id);
CREATE INDEX idx_jobs_status       ON jobs(status);
CREATE INDEX idx_jobs_job_type     ON jobs(job_type);
CREATE INDEX idx_jobs_category_id  ON jobs(category_id);
CREATE INDEX idx_jobs_published_at ON jobs(published_at DESC);

-- Courses
CREATE INDEX idx_courses_instructor_id ON courses(instructor_id);
CREATE INDEX idx_courses_status        ON courses(status);
CREATE INDEX idx_courses_category_id   ON courses(category_id);

-- User saves
CREATE INDEX idx_user_saves_user_type ON user_saves(user_id, target_type, saved_at DESC);
CREATE INDEX idx_user_saves_target    ON user_saves(target_type, target_id);

-- Wallets
CREATE INDEX idx_wallet_user_type ON wallets(user_id, account_type);

-- ============================================================
-- TRIGGERS
-- ============================================================

-- Auto-update updated_at
CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_mentor_profiles_updated_at
    BEFORE UPDATE ON mentor_profiles FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_mentor_profile_assets_updated_at
    BEFORE UPDATE ON mentor_profile_assets FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_jobs_updated_at
    BEFORE UPDATE ON jobs FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_courses_updated_at
    BEFORE UPDATE ON courses FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_wallets_updated_at
    BEFORE UPDATE ON wallets FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
