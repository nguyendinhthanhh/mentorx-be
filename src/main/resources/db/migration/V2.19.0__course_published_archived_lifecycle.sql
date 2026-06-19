ALTER TABLE courses
    ADD COLUMN IF NOT EXISTS discount_price_mxc NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS discount_start_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS discount_end_at TIMESTAMP;

ALTER TABLE course_enrollments
    ADD COLUMN IF NOT EXISTS last_accessed_at TIMESTAMP;

UPDATE courses
SET status = 'PUBLISHED',
    published_at = COALESCE(published_at, updated_at, created_at, CURRENT_TIMESTAMP),
    rejection_reason = NULL,
    submitted_at = NULL
WHERE status IN ('DRAFT', 'PENDING_REVIEW', 'REJECTED');

UPDATE mentor_courses
SET status = 'PUBLISHED'
WHERE status NOT IN ('PUBLISHED', 'ARCHIVED');

UPDATE course_enrollments
SET last_accessed_at = COALESCE(last_accessed_at, enrolled_at, completed_at, CURRENT_TIMESTAMP);

ALTER TABLE courses
DROP CONSTRAINT IF EXISTS courses_status_check;

ALTER TABLE courses
ADD CONSTRAINT courses_status_check
CHECK (status IN ('PUBLISHED', 'ARCHIVED'));

ALTER TABLE courses
DROP CONSTRAINT IF EXISTS courses_discount_price_check;

ALTER TABLE courses
ADD CONSTRAINT courses_discount_price_check
CHECK (
    discount_price_mxc IS NULL
    OR (
        discount_price_mxc >= 0
        AND discount_price_mxc < price_mxc
        AND discount_start_at IS NOT NULL
        AND discount_end_at IS NOT NULL
        AND discount_start_at < discount_end_at
    )
);
