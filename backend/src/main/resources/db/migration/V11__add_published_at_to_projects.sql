-- When a project was last published (HU013). Empty for the projects published before this column:
-- the dashboard falls back to updated_at for them.
ALTER TABLE projects ADD COLUMN published_at TIMESTAMPTZ;
