CREATE TABLE analyses (
    id          UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    resume_id   UUID      NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    content     TEXT      NOT NULL,
    tokens_used INTEGER,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_analyses_resume_id ON analyses(resume_id);
