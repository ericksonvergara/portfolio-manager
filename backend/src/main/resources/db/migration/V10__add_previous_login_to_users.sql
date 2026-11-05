-- The access before the current one (HU012): last_login_at always holds the session in progress, so the
-- dashboard could only show "now". Filled by the login; empty until the second one.
ALTER TABLE users ADD COLUMN previous_login_at TIMESTAMPTZ;
