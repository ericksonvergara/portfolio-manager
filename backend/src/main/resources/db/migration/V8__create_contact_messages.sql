-- Messages sent by visitors from the contact form of the landing page (HU006).
CREATE TABLE contact_messages (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    email       VARCHAR(150)  NOT NULL,
    message     VARCHAR(2000) NOT NULL,
    received_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    read_at     TIMESTAMPTZ
);

-- The inbox lists the newest first.
CREATE INDEX idx_contact_messages_received_at ON contact_messages (received_at DESC);
