-- V001__create_portfolios.sql
-- Creates the portfolios table: one row per user portfolio.
-- data column stores PortfolioJson as JSONB (AI-generated structured content).

CREATE TABLE portfolios (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    username    VARCHAR(30) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    cv_blob_url TEXT,
    cv_hash     VARCHAR(64) NOT NULL,
    data        JSONB       NOT NULL DEFAULT '{}',
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    claimed_at  TIMESTAMPTZ,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT portfolios_pkey PRIMARY KEY (id),
    CONSTRAINT portfolios_username_unique UNIQUE (username),
    CONSTRAINT portfolios_status_check CHECK (status IN ('PENDING', 'LIVE'))
);

-- Index for re-upload lookup by email
CREATE INDEX idx_portfolios_email ON portfolios (email);

-- Index for AI cache deduplication by PDF hash
CREATE INDEX idx_portfolios_cv_hash ON portfolios (cv_hash);

-- Index for public listing queries filtered by status
CREATE INDEX idx_portfolios_status ON portfolios (status);

COMMENT ON TABLE portfolios IS 'One portfolio per user. data column stores AI-generated PortfolioJson as JSONB.';
COMMENT ON COLUMN portfolios.cv_hash IS 'SHA-256 of PDF bytes. Used to detect duplicate uploads and serve cached portfolio.';
COMMENT ON COLUMN portfolios.status IS 'PENDING = awaiting email claim via magic link; LIVE = publicly accessible.';
