ALTER TABLE quiz_questions
ADD COLUMN IF NOT EXISTS answer_data_json TEXT;

UPDATE quiz_questions
SET answer_data_json = CASE
    WHEN question_type = 'TEXT_ANSWER' THEN
        jsonb_build_object(
            'correctAnswer',
            COALESCE(correct_answers_json::jsonb #>> '{}', correct_answers_json)
        )::text
    ELSE
        jsonb_build_object(
            'options',
            COALESCE(options_json::jsonb, '[]'::jsonb),
            'correctAnswers',
            COALESCE(correct_answers_json::jsonb, '[]'::jsonb)
        )::text
    END
WHERE answer_data_json IS NULL
  AND correct_answers_json IS NOT NULL;

UPDATE quiz_questions
SET answer_data_json = CASE
    WHEN question_type IN ('TEXT_ANSWER', 'TEXT_INPUT') THEN
        jsonb_build_object(
            'correctAnswer',
            COALESCE(correct_answers #>> '{}', '')
        )::text
    ELSE
        jsonb_build_object(
            'options',
            COALESCE(options, '[]'::jsonb),
            'correctAnswers',
            COALESCE(correct_answers, '[]'::jsonb)
        )::text
    END
WHERE answer_data_json IS NULL
  AND correct_answers IS NOT NULL;

ALTER TABLE quiz_questions
DROP CONSTRAINT IF EXISTS quiz_questions_question_type_check;

UPDATE quiz_questions
SET question_type = 'MULTIPLE_CHOICE'
WHERE question_type = 'MULTI_SELECT';

UPDATE quiz_questions
SET question_type = 'TEXT_ANSWER'
WHERE question_type = 'TEXT_INPUT';

ALTER TABLE quiz_questions
ADD CONSTRAINT quiz_questions_question_type_check
CHECK (question_type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TRUE_FALSE', 'TEXT_ANSWER'));

ALTER TABLE quiz_questions
ALTER COLUMN answer_data_json SET NOT NULL;

ALTER TABLE quiz_questions
DROP COLUMN IF EXISTS options_json,
DROP COLUMN IF EXISTS correct_answers_json,
DROP COLUMN IF EXISTS options,
DROP COLUMN IF EXISTS correct_answers;
