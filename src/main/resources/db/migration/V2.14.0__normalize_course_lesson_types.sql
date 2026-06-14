UPDATE course_lessons
SET lesson_type = 'LESSON'
WHERE lesson_type IN ('VIDEO', 'ARTICLE', 'TEXT', 'DOWNLOADABLE');
