ALTER TABLE courses
    ADD COLUMN IF NOT EXISTS product_type VARCHAR(20) NOT NULL DEFAULT 'COURSE';

ALTER TABLE courses
    DROP CONSTRAINT IF EXISTS courses_product_type_check;

ALTER TABLE courses
    ADD CONSTRAINT courses_product_type_check
    CHECK (product_type IN ('COURSE', 'DOCUMENT'));

ALTER TABLE course_lessons
    DROP CONSTRAINT IF EXISTS course_lessons_lesson_type_check;

UPDATE course_lessons
SET lesson_type = 'LESSON'
WHERE lesson_type IN ('VIDEO', 'ARTICLE', 'TEXT', 'DOWNLOADABLE');

UPDATE course_lessons
SET lesson_type = 'QUIZ'
WHERE lesson_type IN ('ASSIGNMENT', 'LIVE_SESSION');

ALTER TABLE course_lessons
    ADD CONSTRAINT course_lessons_lesson_type_check
    CHECK (lesson_type IN ('LESSON', 'QUIZ', 'DOCUMENT'));
