-- V003__create_generations.sql
-- Creates the generations table for AI generation audit log.
-- Used for cost accounting, rate limiting, and failure analysis.

CREATE TABLE generations (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    portfolio_id UUID,
    email        VARCHAR(255) NOT NULL,
    ip_address   VARCHAR(45),
    model        VARCHAR(50)  NOT NULL,
    input_tokens  INT,
    output_tokens INT,
    cost_usd     DECIMAL(8, 5),
    duration_ms  INT,
    status       VARCHAR(20)  NOT NULL,
    error_code   VARCHAR(100),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT generations_pkey PRIMARY KEY (id),
    CONSTRAINT generations_status_check CHECK (status IN ('SUCCESS', 'FAIL', 'CACHED')),
    CONSTRAINT generations_portfolio_fk
        FOREIGN KEY (portfolio_id) REFERENCES portfolios (id) ON DELETE SET NULL
);

-- Composite index for per-email rate limit query: WHERE email = ? AND created_at > ?
CREATE INDEX idx_generations_email_created ON generations (email, created_at);

-- Composite index for per-IP rate limit query: WHERE ip_address = ? AND created_at > ?
CREATE INDEX idx_generations_ip_created ON generations (ip_address, created_at);

-- Index for monthly cost rollup: WHERE created_at > start_of_month
CREATE INDEX idx_generations_created_at ON generations (created_at);

-- Index for failure analysis queries
CREATE INDEX idx_generations_status ON generations (status);

COMMENT ON TABLE generations IS 'Audit log of every Anthropic AI call. Used for cost cap enforcement, rate limiting, and failure analysis.';
COMMENT ON COLUMN generations.cost_usd IS 'Computed from input_tokens * haiku_input_price + output_tokens * haiku_output_price at call time.';
COMMENT ON COLUMN generations.error_code IS 'SCHEMA_VALIDATION = AI output invalid; ANTHROPIC_ERROR = API failure; TIMEOUT = deadline exceeded.';
