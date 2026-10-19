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

-- Three published projects and an inactive one, which must not reach the landing page (RN008).
INSERT INTO projects (name, slug, short_description, description, cover_image_url, project_url,
                      repository_url, video_url, status, developed_on, active, featured, display_order)
VALUES
    ('Portfolio Manager', 'portfolio-manager',
     'Portafolio con panel administrativo seguro, JWT y arquitectura hexagonal.',
     'Plataforma para publicar y administrar proyectos de software. Backend en Spring Boot con arquitectura '
         || 'hexagonal y autenticación JWT con rotación de refresh tokens; frontend en React con Tailwind; '
         || 'todo desplegado con Docker Compose y validado en CI.',
     '/demo/projects/portfolio-manager.svg', 'https://example.com',
     'https://github.com/ericksonvergara/portfolio-manager', NULL,
     'IN_PROGRESS', DATE '2026-10-01', TRUE, TRUE, 1),
    ('Sistema de Inventario', 'sistema-de-inventario',
     'Control de stock, compras y alertas de reposición para pequeños comercios.',
     'Aplicación web para registrar productos, movimientos de inventario y proveedores, con alertas de stock '
         || 'mínimo y reportes exportables.',
     '/demo/projects/sistema-de-inventario.svg', NULL,
     'https://github.com/ericksonvergara/inventario', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
     'COMPLETED', DATE '2025-11-15', TRUE, FALSE, 2),
    ('Agenda de Citas', 'agenda-de-citas',
     'Reserva de citas en línea con recordatorios por correo.',
     'Sistema de reservas para consultorios: calendario de disponibilidad, confirmación y recordatorios '
         || 'automáticos por correo electrónico.',
     '/demo/projects/agenda-de-citas.svg', 'https://example.com/agenda', NULL, NULL,
     'COMPLETED', DATE '2025-06-30', TRUE, FALSE, 3),
    ('Prototipo Oculto', 'prototipo-oculto',
     'Proyecto inactivo: no debe aparecer en la Landing Page.',
     'Proyecto de prueba marcado como inactivo.',
     NULL, NULL, NULL, NULL,
     'IN_PROGRESS', NULL, FALSE, FALSE, 4)
ON CONFLICT (slug) DO UPDATE SET
    name              = EXCLUDED.name,
    short_description = EXCLUDED.short_description,
    description       = EXCLUDED.description,
    cover_image_url   = EXCLUDED.cover_image_url,
    project_url       = EXCLUDED.project_url,
    repository_url    = EXCLUDED.repository_url,
    video_url         = EXCLUDED.video_url,
    status            = EXCLUDED.status,
    developed_on      = EXCLUDED.developed_on,
    active            = EXCLUDED.active,
    featured          = EXCLUDED.featured,
    display_order     = EXCLUDED.display_order,
    updated_at        = now();

DELETE FROM project_technologies;

INSERT INTO project_technologies (project_id, technology_id)
SELECT p.id, t.id
FROM (VALUES
    ('portfolio-manager',     'Java'),
    ('portfolio-manager',     'Spring Boot'),
    ('portfolio-manager',     'React'),
    ('portfolio-manager',     'PostgreSQL'),
    ('portfolio-manager',     'Docker'),
    ('sistema-de-inventario', 'Java'),
    ('sistema-de-inventario', 'Spring Boot'),
    ('sistema-de-inventario', 'PostgreSQL'),
    ('agenda-de-citas',       'React'),
    ('agenda-de-citas',       'TypeScript'),
    ('agenda-de-citas',       'Tailwind CSS'),
    ('prototipo-oculto',      'Java')
) AS link (slug, technology)
JOIN projects p ON p.slug = link.slug
JOIN technologies t ON t.name = link.technology;

DELETE FROM project_images;

INSERT INTO project_images (project_id, image_url, alt_text, display_order)
SELECT p.id, image.url, image.alt_text, image.display_order
FROM (VALUES
    ('portfolio-manager',     '/demo/projects/portfolio-manager.svg',     'Landing Page del portafolio', 1),
    ('portfolio-manager',     '/demo/projects/agenda-de-citas.svg',       'Panel administrativo',        2),
    ('sistema-de-inventario', '/demo/projects/sistema-de-inventario.svg', 'Listado de productos',        1)
) AS image (slug, url, alt_text, display_order)
JOIN projects p ON p.slug = image.slug;
