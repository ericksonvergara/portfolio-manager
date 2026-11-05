-- Every visit to the public site (HU025). The IP is stored masked; the full address is never saved.
-- The visit outlives its project: history stays in the totals after the project is deleted.
CREATE TABLE visits (
    id               BIGSERIAL PRIMARY KEY,
    visited_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    anonymized_ip    VARCHAR(45) NOT NULL,
    browser          VARCHAR(30) NOT NULL,
    operating_system VARCHAR(30) NOT NULL,
    device           VARCHAR(20) NOT NULL,
    country          VARCHAR(2),
    project_id       BIGINT REFERENCES projects (id) ON DELETE SET NULL
);

-- The statistics count by day and month, and rank the projects.
CREATE INDEX idx_visits_visited_at ON visits (visited_at);
CREATE INDEX idx_visits_project_id ON visits (project_id);
