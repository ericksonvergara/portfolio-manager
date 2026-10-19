-- Demo content for local development ONLY. Flyway applies this folder when
-- SPRING_FLYWAY_LOCATIONS includes classpath:db/seed, which production never sets.
-- Repeatable migration: Flyway re-runs it whenever it changes, so every statement is idempotent.

INSERT INTO profiles (full_name, headline, about, photo_url, email, location)
VALUES (
    'Erickson Vergara',
    'Desarrollador Full Stack',
    'Construyo aplicaciones web de extremo a extremo con Java, Spring Boot y React. '
        || 'Me interesan la arquitectura limpia, las pruebas automatizadas y los despliegues reproducibles con Docker.',
    '/demo/profile.svg',
    'contacto@example.com',
    'Colombia'
)
ON CONFLICT (singleton) DO UPDATE SET
    full_name  = EXCLUDED.full_name,
    headline   = EXCLUDED.headline,
    about      = EXCLUDED.about,
    photo_url  = EXCLUDED.photo_url,
    email      = EXCLUDED.email,
    location   = EXCLUDED.location,
    updated_at = now();

DELETE FROM social_links;

INSERT INTO social_links (profile_id, platform, url, display_order)
SELECT p.id, link.platform, link.url, link.display_order
FROM profiles p
CROSS JOIN (VALUES
    ('GITHUB',   'https://github.com/ericksonvergara',      1),
    ('LINKEDIN', 'https://www.linkedin.com/in/example',      2),
    ('WEBSITE',  'https://example.com',                      3)
) AS link (platform, url, display_order);

INSERT INTO technologies (name, category, display_order)
VALUES
    ('Java',         'BACKEND',  1),
    ('Spring Boot',  'BACKEND',  2),
    ('React',        'FRONTEND', 3),
    ('TypeScript',   'FRONTEND', 4),
    ('Tailwind CSS', 'FRONTEND', 5),
    ('PostgreSQL',   'DATABASE', 6),
    ('Docker',       'DEVOPS',   7),
    ('GitHub Actions', 'DEVOPS', 8),
    ('Git',          'TOOLS',    9)
ON CONFLICT (name) DO UPDATE SET
    category      = EXCLUDED.category,
    display_order = EXCLUDED.display_order,
    updated_at    = now();
