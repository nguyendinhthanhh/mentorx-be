-- MentorX Platform Database Schema
-- Comprehensive PostgreSQL schema for mentoring platform with 25+ feature domains

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Enums
CREATE TYPE user_status AS ENUM ('active', 'inactive', 'suspended', 'deleted');
CREATE TYPE user_role AS ENUM ('student', 'mentor', 'admin', 'moderator');
CREATE TYPE mentor_status AS ENUM ('pending', 'approved', 'rejected', 'suspended');
CREATE TYPE job_status AS ENUM ('draft', 'published', 'in_progress', 'completed', 'cancelled');
CREATE TYPE job_type AS ENUM ('project', 'consultation', 'course');
CREATE TYPE budget_type AS ENUM ('fixed', 'hourly');
CREATE TYPE proposal_status AS ENUM ('pending', 'accepted', 'rejected', 'withdrawn');
CREATE TYPE contract_status AS ENUM ('active', 'completed', 'cancelled', 'disputed');
CREATE TYPE milestone_status AS ENUM ('pending', 'in_progress', 'completed', 'approved');
CREATE TYPE course_status AS ENUM ('draft', 'published', 'archived');
CREATE TYPE lesson_type AS ENUM ('video', 'text', 'quiz', 'assignment');
CREATE TYPE payment_gateway AS ENUM ('vnpay', 'momo', 'payos', 'stripe', 'paypal', 'bank_transfer');
CREATE TYPE txn_type AS ENUM ('deposit', 'withdrawal', 'payment', 'refund', 'commission');
CREATE TYPE txn_status AS ENUM ('pending', 'completed', 'failed', 'cancelled');
CREATE TYPE ledger_direction AS ENUM ('debit', 'credit');
CREATE TYPE wallet_account_type AS ENUM ('main', 'escrow', 'commission');
CREATE TYPE withdrawal_status AS ENUM ('pending', 'processing', 'completed', 'rejected');
CREATE TYPE notification_type AS ENUM ('system', 'job', 'message', 'payment', 'course');
CREATE TYPE message_type AS ENUM ('text', 'image', 'file', 'system');
CREATE TYPE chat_room_type AS ENUM ('direct', 'group', 'support');
CREATE TYPE review_target_type AS ENUM ('mentor', 'student', 'course', 'job');
CREATE TYPE report_target_type AS ENUM ('user', 'job', 'course', 'message');
CREATE TYPE report_status AS ENUM ('pending', 'investigating', 'resolved', 'dismissed');
CREATE TYPE dispute_status AS ENUM ('open', 'investigating', 'resolved', 'escalated');
CREATE TYPE dispute_outcome AS ENUM ('favor_client', 'favor_mentor', 'partial_refund', 'no_action');
CREATE TYPE badge_type AS ENUM ('skill', 'achievement', 'certification', 'milestone');
CREATE TYPE feed_item_type AS ENUM ('job_posted', 'course_published', 'achievement_earned', 'review_received');
CREATE TYPE supported_language AS ENUM ('en', 'vi', 'zh', 'ja');

-- ============================================================================
-- USER MANAGEMENT
-- ============================================================================

-- Core user table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    avatar_url TEXT,
    bio TEXT,
    status user_status DEFAULT 'active',
    role user_role DEFAULT 'student',
    email_verified BOOLEAN DEFAULT FALSE,
    phone VARCHAR(20),
    date_of_birth DATE,
    timezone VARCHAR(50) DEFAULT 'UTC',
    language supported_language DEFAULT 'en',
    metadata JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- User profiles with additional information
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    location VARCHAR(200),
    website_url TEXT,
    linkedin_url TEXT,
    github_url TEXT,
    education JSONB,
    work_experience JSONB,
    skills TEXT[],
    interests TEXT[],
    preferences JSONB,
    privacy_settings JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================================
-- AUTHENTICATION & SECURITY
-- ============================================================================

-- Refresh tokens for JWT authentication
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE,
    device_info JSONB,
    ip_address INET,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- User sessions tracking
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    session_token VARCHAR(255) NOT NULL,
    device_fingerprint VARCHAR(255),
    user_agent TEXT,
    ip_address INET,
    location JSONB,
    is_active BOOLEAN DEFAULT TRUE,
    last_activity TIMESTAMPTZ DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================================
-- MENTOR MANAGEMENT
-- ============================================================================

-- Mentor profiles and verification
CREATE TABLE mentor_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(200),
    hourly_rate DECIMAL(10,2),
    currency VARCHAR(3) DEFAULT 'USD',
    experience_years INTEGER,
    skills TEXT[],
    specializations TEXT[],
    certifications JSONB,
    portfolio JSONB,
    availability JSONB,
    rating DECIMAL(3,2) DEFAULT 0.00,
    total_reviews INTEGER DEFAULT 0,
    total_jobs_completed INTEGER DEFAULT 0,
    total_earnings DECIMAL(15,2) DEFAULT 0.00,
    status mentor_status DEFAULT 'pending',
    is_verified BOOLEAN DEFAULT FALSE,
    verification_documents JSONB,
    verification_notes TEXT,
    verified_at TIMESTAMPTZ,
    verified_by UUID,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_mentor_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_mentor_profiles_verifier FOREIGN KEY (verified_by) REFERENCES users(id)
);

-- ============================================================================
-- JOB MARKETPLACE
-- ============================================================================

-- Job postings
CREATE TABLE jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    requirements TEXT,
    deliverables TEXT,
    type job_type NOT NULL,
    budget_type budget_type NOT NULL,
    budget_amount DECIMAL(10,2),
    budget_currency VARCHAR(3) DEFAULT 'USD',
    skills_required TEXT[],
    experience_level VARCHAR(20),
    status job_status DEFAULT 'draft',
    deadline DATE,
    location VARCHAR(200),
    is_remote BOOLEAN DEFAULT TRUE,
    attachments JSONB,
    metadata JSONB,
    views_count INTEGER DEFAULT 0,
    applications_count INTEGER DEFAULT 0,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_jobs_client FOREIGN KEY (client_id) REFERENCES users(id)
);

-- Job proposals from mentors
CREATE TABLE job_proposals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL,
    mentor_id UUID NOT NULL,
    cover_letter TEXT NOT NULL,
    proposed_rate DECIMAL(10,2),
    proposed_timeline INTEGER, -- in days
    deliverables TEXT,
    status proposal_status DEFAULT 'pending',
    attachments JSONB,
    submitted_at TIMESTAMPTZ DEFAULT NOW(),
    responded_at TIMESTAMPTZ,
    CONSTRAINT fk_job_proposals_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    CONSTRAINT fk_job_proposals_mentor FOREIGN KEY (mentor_id) REFERENCES users(id),
    CONSTRAINT uk_job_proposals_job_mentor UNIQUE (job_id, mentor_id)
);

-- Contracts between clients and mentors
CREATE TABLE contracts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL,
    client_id UUID NOT NULL,
    mentor_id UUID NOT NULL,
    proposal_id UUID,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    agreed_rate DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    estimated_hours INTEGER,
    deadline DATE,
    status contract_status DEFAULT 'active',
    terms_and_conditions TEXT,
    signed_at TIMESTAMPTZ DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    cancellation_reason TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_contracts_job FOREIGN KEY (job_id) REFERENCES jobs(id),
    CONSTRAINT fk_contracts_client FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT fk_contracts_mentor FOREIGN KEY (mentor_id) REFERENCES users(id),
    CONSTRAINT fk_contracts_proposal FOREIGN KEY (proposal_id) REFERENCES job_proposals(id)
);

-- Contract milestones
CREATE TABLE contract_milestones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    amount DECIMAL(10,2) NOT NULL,
    due_date DATE,
    status milestone_status DEFAULT 'pending',
    deliverables TEXT,
    submission_notes TEXT,
    feedback TEXT,
    submitted_at TIMESTAMPTZ,
    approved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_contract_milestones_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
);

-- ============================================================================
-- COURSE SYSTEM
-- ============================================================================

-- Courses created by mentors
CREATE TABLE courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mentor_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    short_description VARCHAR(500),
    thumbnail_url TEXT,
    price DECIMAL(10,2),
    currency VARCHAR(3) DEFAULT 'USD',
    level VARCHAR(20), -- beginner, intermediate, advanced
    duration_hours INTEGER,
    language supported_language DEFAULT 'en',
    skills_taught TEXT[],
    prerequisites TEXT[],
    learning_objectives TEXT[],
    status course_status DEFAULT 'draft',
    is_featured BOOLEAN DEFAULT FALSE,
    rating DECIMAL(3,2) DEFAULT 0.00,
    total_reviews INTEGER DEFAULT 0,
    total_enrollments INTEGER DEFAULT 0,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_courses_mentor FOREIGN KEY (mentor_id) REFERENCES users(id)
);

-- Course lessons/modules
CREATE TABLE course_lessons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    type lesson_type NOT NULL,
    content TEXT,
    video_url TEXT,
    duration_minutes INTEGER,
    order_index INTEGER NOT NULL,
    is_preview BOOLEAN DEFAULT FALSE,
    resources JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_course_lessons_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Course enrollments
CREATE TABLE course_enrollments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL,
    student_id UUID NOT NULL,
    enrolled_at TIMESTAMPTZ DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    progress_percentage INTEGER DEFAULT 0,
    last_accessed_lesson_id UUID,
    certificate_issued BOOLEAN DEFAULT FALSE,
    certificate_url TEXT,
    CONSTRAINT fk_course_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_course_enrollments_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT fk_course_enrollments_lesson FOREIGN KEY (last_accessed_lesson_id) REFERENCES course_lessons(id),
    CONSTRAINT uk_course_enrollments_course_student UNIQUE (course_id, student_id)
);

-- ============================================================================
-- FINANCIAL SYSTEM (Double-Entry Bookkeeping)
-- ============================================================================

-- Wallet accounts for users
CREATE TABLE wallet_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    account_type wallet_account_type NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00 CHECK (balance >= 0),
    currency VARCHAR(3) DEFAULT 'USD',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_wallet_accounts_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_wallet_accounts_user_type UNIQUE (user_id, account_type, currency)
);

-- Financial transactions
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_id VARCHAR(100) UNIQUE NOT NULL,
    type txn_type NOT NULL,
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) DEFAULT 'USD',
    status txn_status DEFAULT 'pending',
    gateway payment_gateway,
    gateway_transaction_id VARCHAR(255),
    description TEXT,
    metadata JSONB,
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Double-entry ledger entries
CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL,
    account_id UUID NOT NULL,
    direction ledger_direction NOT NULL,
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    balance_after DECIMAL(15,2) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_ledger_entries_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    CONSTRAINT fk_ledger_entries_account FOREIGN KEY (account_id) REFERENCES wallet_accounts(id)
);

-- Withdrawal requests
CREATE TABLE withdrawal_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) DEFAULT 'USD',
    bank_details JSONB NOT NULL,
    status withdrawal_status DEFAULT 'pending',
    processed_by UUID,
    processed_at TIMESTAMPTZ,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_withdrawal_requests_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_withdrawal_requests_processor FOREIGN KEY (processed_by) REFERENCES users(id)
);

-- ============================================================================
-- COMMUNICATION SYSTEM
-- ============================================================================

-- Chat rooms
CREATE TABLE chat_rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type chat_room_type NOT NULL,
    name VARCHAR(200),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    metadata JSONB,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_chat_rooms_creator FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Chat room participants
CREATE TABLE chat_room_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL,
    user_id UUID NOT NULL,
    joined_at TIMESTAMPTZ DEFAULT NOW(),
    left_at TIMESTAMPTZ,
    is_admin BOOLEAN DEFAULT FALSE,
    is_muted BOOLEAN DEFAULT FALSE,
    last_read_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_chat_room_participants_room FOREIGN KEY (room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_room_participants_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_chat_room_participants_room_user UNIQUE (room_id, user_id)
);

-- Messages
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    type message_type DEFAULT 'text',
    content TEXT,
    attachments JSONB,
    reply_to_id UUID,
    is_edited BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMPTZ DEFAULT NOW(),
    edited_at TIMESTAMPTZ,
    CONSTRAINT fk_messages_room FOREIGN KEY (room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id),
    CONSTRAINT fk_messages_reply_to FOREIGN KEY (reply_to_id) REFERENCES messages(id)
);

-- ============================================================================
-- NOTIFICATION SYSTEM
-- ============================================================================

-- Notifications
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type notification_type NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    data JSONB,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================================
-- REVIEW & RATING SYSTEM
-- ============================================================================

-- Reviews and ratings
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reviewer_id UUID NOT NULL,
    target_type review_target_type NOT NULL,
    target_id UUID NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(200),
    comment TEXT,
    is_anonymous BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN DEFAULT FALSE,
    helpful_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id)
);

-- ============================================================================
-- REPORTING & MODERATION
-- ============================================================================

-- User reports
CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID NOT NULL,
    target_type report_target_type NOT NULL,
    target_id UUID NOT NULL,
    reason VARCHAR(100) NOT NULL,
    description TEXT,
    evidence JSONB,
    status report_status DEFAULT 'pending',
    assigned_to UUID,
    resolution TEXT,
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(id),
    CONSTRAINT fk_reports_assignee FOREIGN KEY (assigned_to) REFERENCES users(id)
);

-- Disputes
CREATE TABLE disputes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id UUID NOT NULL,
    raised_by UUID NOT NULL,
    reason VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    evidence JSONB,
    status dispute_status DEFAULT 'open',
    outcome dispute_outcome,
    resolution TEXT,
    assigned_to UUID,
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_disputes_contract FOREIGN KEY (contract_id) REFERENCES contracts(id),
    CONSTRAINT fk_disputes_raiser FOREIGN KEY (raised_by) REFERENCES users(id),
    CONSTRAINT fk_disputes_assignee FOREIGN KEY (assigned_to) REFERENCES users(id)
);

-- ============================================================================
-- GAMIFICATION & ACHIEVEMENTS
-- ============================================================================

-- User badges and achievements
CREATE TABLE user_badges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type badge_type NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon_url TEXT,
    earned_at TIMESTAMPTZ DEFAULT NOW(),
    metadata JSONB,
    CONSTRAINT fk_user_badges_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ============================================================================
-- ANALYTICS & TRACKING
-- ============================================================================

-- User activity feed
CREATE TABLE activity_feed (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type feed_item_type NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    data JSONB,
    is_public BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_activity_feed_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- User analytics events
CREATE TABLE user_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    session_id UUID,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_user_events_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================

-- User indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_status_role ON users(status, role);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Mentor profile indexes
CREATE INDEX idx_mentor_profiles_user_id ON mentor_profiles(user_id);
CREATE INDEX idx_mentor_profiles_status ON mentor_profiles(status);
CREATE INDEX idx_mentor_profiles_rating ON mentor_profiles(rating DESC);
CREATE INDEX idx_mentor_profiles_skills_gin ON mentor_profiles USING gin(skills);

-- Job indexes
CREATE INDEX idx_jobs_client_id ON jobs(client_id);
CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_type ON jobs(type);
CREATE INDEX idx_jobs_published_at ON jobs(published_at DESC);
CREATE INDEX idx_jobs_skills_gin ON jobs USING gin(skills_required);
CREATE INDEX idx_jobs_location ON jobs(location);

-- Proposal indexes
CREATE INDEX idx_job_proposals_job_id ON job_proposals(job_id);
CREATE INDEX idx_job_proposals_mentor_id ON job_proposals(mentor_id);
CREATE INDEX idx_job_proposals_status ON job_proposals(status);

-- Contract indexes
CREATE INDEX idx_contracts_client_id ON contracts(client_id);
CREATE INDEX idx_contracts_mentor_id ON contracts(mentor_id);
CREATE INDEX idx_contracts_status ON contracts(status);

-- Course indexes
CREATE INDEX idx_courses_mentor_id ON courses(mentor_id);
CREATE INDEX idx_courses_status ON courses(status);
CREATE INDEX idx_courses_rating ON courses(rating DESC);
CREATE INDEX idx_courses_skills_gin ON courses USING gin(skills_taught);

-- Financial indexes
CREATE INDEX idx_wallet_accounts_user_id ON wallet_accounts(user_id);
CREATE INDEX idx_transactions_reference_id ON transactions(reference_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_ledger_entries_account_id ON ledger_entries(account_id);
CREATE INDEX idx_ledger_entries_transaction_id ON ledger_entries(transaction_id);

-- Communication indexes
CREATE INDEX idx_chat_room_participants_room_id ON chat_room_participants(room_id);
CREATE INDEX idx_chat_room_participants_user_id ON chat_room_participants(user_id);
CREATE INDEX idx_messages_room_id ON messages(room_id);
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_sent_at ON messages(sent_at DESC);

-- Notification indexes
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

-- Review indexes
CREATE INDEX idx_reviews_target ON reviews(target_type, target_id);
CREATE INDEX idx_reviews_reviewer_id ON reviews(reviewer_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);

-- Full-text search indexes
CREATE INDEX idx_jobs_title_description_gin ON jobs USING gin(to_tsvector('english', title || ' ' || description));
CREATE INDEX idx_courses_title_description_gin ON courses USING gin(to_tsvector('english', title || ' ' || description));
CREATE INDEX idx_users_search_gin ON users USING gin(to_tsvector('english', first_name || ' ' || last_name || ' ' || username));
