-- BWI520 / vorn - Auth Basis (Inkrement 1)
-- Hinweis: Diese SQL-Datei ist als Dokumentations- und Fallback-Skript gedacht.
-- Im Projekt wird die Tabelle standardmaessig auch beim Startup erzeugt (db.init.enabled=true).

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed-Admin wird in der App gesetzt, sobald kein ADMIN existiert.
-- Konfiguration in WEB-INF/web.xml:
-- admin.email
-- admin.displayName
-- admin.password
