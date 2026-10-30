-- Technologies of artificial intelligence (HU005: filter the projects by "IA").
ALTER TABLE technologies DROP CONSTRAINT technologies_category_check;
ALTER TABLE technologies ADD CONSTRAINT technologies_category_check
    CHECK (category IN ('BACKEND', 'FRONTEND', 'DATABASE', 'DEVOPS', 'MOBILE', 'AI', 'TOOLS'));
