-- Projects of the portfolio. The landing page lists the active ones (RN008); the detail page
-- (HU003) shows the full description, gallery, date, status, links and optional video.
CREATE TABLE projects (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(100)  NOT NULL UNIQUE,
    slug              VARCHAR(120)  NOT NULL UNIQUE,
    short_description VARCHAR(300)  NOT NULL,
    description       VARCHAR(5000) NOT NULL,
    cover_image_url   VARCHAR(500),
    project_url       VARCHAR(500),
    repository_url    VARCHAR(500),
    video_url         VARCHAR(500),
    status            VARCHAR(20)   NOT NULL DEFAULT 'COMPLETED'
        CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    developed_on      DATE,
    active            BOOLEAN       NOT NULL DEFAULT FALSE,
    featured          BOOLEAN       NOT NULL DEFAULT FALSE,
    display_order     INTEGER       NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    -- RN004: only projects with a cover image can be published.
    CONSTRAINT projects_published_with_cover CHECK (NOT active OR cover_image_url IS NOT NULL)
);

-- The public listing filters by active and sorts by featured, display order and name.
CREATE INDEX idx_projects_published ON projects (featured DESC, display_order, name) WHERE active;

CREATE TABLE project_images (
    id            BIGSERIAL PRIMARY KEY,
    project_id    BIGINT       NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    image_url     VARCHAR(500) NOT NULL,
    alt_text      VARCHAR(200),
    display_order INTEGER      NOT NULL DEFAULT 0
);

CREATE INDEX idx_project_images_project_id ON project_images (project_id);

CREATE TABLE project_technologies (
    project_id    BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    technology_id BIGINT NOT NULL REFERENCES technologies (id),
    PRIMARY KEY (project_id, technology_id)
);

CREATE INDEX idx_project_technologies_technology_id ON project_technologies (technology_id);
