ALTER TABLE mentor_profiles
    ADD COLUMN IF NOT EXISTS proof_links JSONB;

UPDATE mentor_profiles
SET proof_links = COALESCE(
    to_jsonb(
        array_remove(
            ARRAY[
                CASE WHEN linkedin_url IS NOT NULL AND btrim(linkedin_url) <> '' THEN jsonb_build_object('label', 'LinkedIn profile', 'url', linkedin_url) END,
                CASE WHEN github_url IS NOT NULL AND btrim(github_url) <> '' THEN jsonb_build_object('label', 'GitHub profile', 'url', github_url) END,
                CASE WHEN portfolio_url IS NOT NULL AND btrim(portfolio_url) <> '' THEN jsonb_build_object('label', 'Portfolio', 'url', portfolio_url) END,
                CASE WHEN portfolio_evidence_url IS NOT NULL AND btrim(portfolio_evidence_url) <> '' THEN jsonb_build_object('label', 'Proof of work', 'url', portfolio_evidence_url) END,
                CASE WHEN video_intro_url IS NOT NULL AND btrim(video_intro_url) <> '' THEN jsonb_build_object('label', 'Intro video', 'url', video_intro_url) END
            ]::jsonb[],
            NULL
        )
    ),
    '[]'::jsonb
)
WHERE proof_links IS NULL;
