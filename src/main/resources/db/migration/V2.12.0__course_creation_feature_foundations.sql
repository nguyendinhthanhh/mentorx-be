ALTER TABLE courses
    ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP;

ALTER TABLE course_enrollments
    ADD COLUMN IF NOT EXISTS certificate_code VARCHAR(80),
    ADD COLUMN IF NOT EXISTS certificate_issued_at TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS ux_course_enrollments_certificate_code
    ON course_enrollments(certificate_code)
    WHERE certificate_code IS NOT NULL;

ALTER TABLE lesson_progress
    ADD COLUMN IF NOT EXISTS progress_percent INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS scroll_percent INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS active_time_sec INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_position_sec INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS completed_by_rule BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS quiz_questions (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    lesson_id UUID NOT NULL REFERENCES course_lessons(id),
    question_type VARCHAR(30) NOT NULL,
    question_text TEXT NOT NULL,
    options_json TEXT,
    correct_answers_json TEXT NOT NULL,
    points INTEGER NOT NULL DEFAULT 1,
    explanation TEXT,
    order_index INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_quiz_question_lesson ON quiz_questions(lesson_id);
CREATE INDEX IF NOT EXISTS idx_quiz_question_order ON quiz_questions(lesson_id, order_index);

CREATE TABLE IF NOT EXISTS quiz_attempts (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    enrollment_id UUID NOT NULL REFERENCES course_enrollments(id),
    lesson_id UUID NOT NULL REFERENCES course_lessons(id),
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    score NUMERIC(8, 2) NOT NULL DEFAULT 0,
    max_score NUMERIC(8, 2) NOT NULL DEFAULT 0,
    passed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_quiz_attempt_enrollment_lesson ON quiz_attempts(enrollment_id, lesson_id);

CREATE TABLE IF NOT EXISTS quiz_answers (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    attempt_id UUID NOT NULL REFERENCES quiz_attempts(id),
    question_id UUID NOT NULL REFERENCES quiz_questions(id),
    given_answer_json TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    points_earned NUMERIC(8, 2) NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_quiz_answer_attempt ON quiz_answers(attempt_id);

CREATE TABLE IF NOT EXISTS course_download_audits (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    course_id UUID NOT NULL REFERENCES courses(id),
    lesson_id UUID NOT NULL REFERENCES course_lessons(id),
    user_id UUID NOT NULL REFERENCES users(id),
    file_url TEXT,
    ip_address VARCHAR(80),
    user_agent TEXT,
    downloaded_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_course_download_user ON course_download_audits(user_id);
CREATE INDEX IF NOT EXISTS idx_course_download_lesson ON course_download_audits(lesson_id);

CREATE TABLE IF NOT EXISTS course_qa_messages (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    course_id UUID NOT NULL REFERENCES courses(id),
    lesson_id UUID REFERENCES course_lessons(id),
    sender_id UUID NOT NULL REFERENCES users(id),
    content TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_course_qa_course ON course_qa_messages(course_id);
CREATE INDEX IF NOT EXISTS idx_course_qa_lesson ON course_qa_messages(lesson_id);
