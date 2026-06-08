CREATE TABLE IF NOT EXISTS course_skills (
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    skill VARCHAR(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS course_skill_ids (
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    skill_id INTEGER NOT NULL REFERENCES skills(id) ON DELETE RESTRICT,
    PRIMARY KEY (course_id, skill_id)
);

CREATE INDEX IF NOT EXISTS idx_course_skill_ids_skill_id
    ON course_skill_ids(skill_id);

INSERT INTO course_skill_ids (course_id, skill_id)
SELECT DISTINCT cs.course_id, s.id
FROM course_skills cs
JOIN skills s
    ON LOWER(s.slug) = LOWER(cs.skill)
    OR LOWER(s.label_en) = LOWER(cs.skill)
    OR LOWER(s.label_vi) = LOWER(cs.skill)
ON CONFLICT DO NOTHING;

ALTER TABLE courses
    ALTER COLUMN price_mxc TYPE NUMERIC(12, 0)
    USING ROUND(price_mxc);
