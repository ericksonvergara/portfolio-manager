-- Public profile of the portfolio owner shown on the landing page. There is exactly one:
-- the singleton column only accepts TRUE and is unique, so a second row is rejected.
CREATE TABLE profiles (
    id         BIGSERIAL PRIMARY KEY,
    singleton  BOOLEAN       NOT NULL DEFAULT TRUE UNIQUE CHECK (singleton),
    full_name  VARCHAR(100)  NOT NULL,
    headline   VARCHAR(150)  NOT NULL,
    about      VARCHAR(2000) NOT NULL,
    photo_url  VARCHAR(500),
    email      VARCHAR(150)  NOT NULL,
    location   VARCHAR(100),
    created_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE social_links (
    id            BIGSERIAL PRIMARY KEY,
    profile_id    BIGINT       NOT NULL REFERENCES profiles (id) ON DELETE CASCADE,
    platform      VARCHAR(20)  NOT NULL
        CHECK (platform IN ('GITHUB', 'LINKEDIN', 'X', 'INSTAGRAM', 'YOUTUBE', 'WEBSITE')),
    url           VARCHAR(500) NOT NULL,
    display_order INTEGER      NOT NULL DEFAULT 0,
    UNIQUE (profile_id, platform)
);

-- Catalog of technologies, shown on the landing page and later associated with projects.
CREATE TABLE technologies (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(50) NOT NULL UNIQUE,
    category      VARCHAR(20) NOT NULL
        CHECK (category IN ('BACKEND', 'FRONTEND', 'DATABASE', 'DEVOPS', 'MOBILE', 'TOOLS')),
    display_order INTEGER     NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
