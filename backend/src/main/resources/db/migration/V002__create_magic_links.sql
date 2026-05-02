-- V002__create_magic_links.sql
-- Creates the magic_links table for one-time-use auth tokens.
-- Used for both portfolio CLAIM and UPDATE flows.

CREATE TABLE magic_links (
    id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    email        VARCHAR(255) NOT NULL,
    portfolio_id UUID,
    token        VARCHAR(64) NOT NULL,
    purpose      VARCHAR(20) NOT NULL,
    expires_at   TIMESTAMPTZ NOT NULL,
    used_at      TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT magic_links_pkey PRIMARY KEY (id),
    CONSTRAINT magic_links_token_unique UNIQUE (token),
    CONSTRAINT magic_links_purpose_check CHECK (purpose IN ('CLAIM', 'UPDATE')),
    CONSTRAINT magic_links_used_at_check CHECK (used_at IS NULL OR used_at >= created_at),
    CONSTRAINT magic_links_portfolio_fk
        FOREIGN KEY (portfolio_id) REFERENCES portfolios (id) ON DELETE SET NULL
);

-- Index for rate limit queries: how many active tokens does this email have?
CREATE INDEX idx_magic_links_email ON magic_links (email);

-- Composite index optimised for the rate limit query pattern:
-- WHERE email = ? AND used_at IS NULL AND expires_at > now()
CREATE INDEX idx_magic_links_email_active
    ON magic_links (email, used_at, expires_at);

COMMENT ON TABLE magic_links IS 'One-time-use tokens for portfolio claim and update flows. Tokens expire after 24h.';
COMMENT ON COLUMN magic_links.token IS 'base64url of 32 random SecureRandom bytes. Never a UUID (entropy too low).';
COMMENT ON COLUMN magic_links.purpose IS 'CLAIM = activate pending portfolio; UPDATE = trigger re-upload flow.';
