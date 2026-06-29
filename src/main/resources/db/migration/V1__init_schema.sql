-- ==========================================
-- V1__init_schema.sql
-- ==========================================

-- Siege (headquarters/branch)
CREATE TABLE siege (
    id         BIGSERIAL PRIMARY KEY,
    nom        VARCHAR(150) NOT NULL UNIQUE,
    adresse    VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Depot de stockage
CREATE TABLE depot (
    id         BIGSERIAL PRIMARY KEY,
    nom        VARCHAR(150) NOT NULL,
    adresse    VARCHAR(255),
    siege_id   BIGINT NOT NULL REFERENCES siege(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Permission
CREATE TABLE permission (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Role
CREATE TABLE role (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Role <-> Permission
CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL REFERENCES role(id),
    permission_id BIGINT NOT NULL REFERENCES permission(id),
    PRIMARY KEY (role_id, permission_id)
);

-- User
CREATE TABLE app_user (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(100) NOT NULL UNIQUE,
    email      VARCHAR(150) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    enabled    BOOLEAN NOT NULL DEFAULT true,
    siege_id   BIGINT NOT NULL REFERENCES siege(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- User <-> Role
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    role_id BIGINT NOT NULL REFERENCES role(id),
    PRIMARY KEY (user_id, role_id)
);

-- ==========================================
-- Seed data
-- ==========================================

INSERT INTO permission (name) VALUES
    ('READ'),
    ('WRITE'),
    ('DELETE'),
    ('ADMIN');

INSERT INTO role (name) VALUES
    ('ROLE_USER'),
    ('ROLE_MANAGER'),
    ('ROLE_ADMIN');

-- ROLE_USER -> READ
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p
WHERE r.name = 'ROLE_USER' AND p.name IN ('READ');

-- ROLE_MANAGER -> READ, WRITE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p
WHERE r.name = 'ROLE_MANAGER' AND p.name IN ('READ', 'WRITE');

-- ROLE_ADMIN -> all
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p
WHERE r.name = 'ROLE_ADMIN';

-- Demo siege + depot
INSERT INTO siege (nom, adresse) VALUES
    ('Siège Central', '10 Rue de la Paix, Casablanca'),
    ('Siège Nord', '5 Avenue Hassan II, Rabat');

INSERT INTO depot (nom, adresse, siege_id) VALUES
    ('Dépôt A', 'Zone Industrielle, Casablanca', 1),
    ('Dépôt B', 'Ain Sebaâ, Casablanca', 1),
    ('Dépôt C', 'Hay Riad, Rabat', 2);
