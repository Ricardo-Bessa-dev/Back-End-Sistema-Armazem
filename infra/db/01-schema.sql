-- 01-schema.sql — Armazém (Redes II)
-- Cria as três tabelas do sistema: products, users, sessions.
CREATE DATABASE IF NOT EXISTS ArmazemRedes
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ArmazemRedes;


-- products

CREATE TABLE IF NOT EXISTS products (
    id       BIGINT        NOT NULL AUTO_INCREMENT,
    name     VARCHAR(120)  NOT NULL,
    quantity INT           NOT NULL DEFAULT 0,
    price    DECIMAL(10,2) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT chk_quantity_nao_negativa CHECK (quantity >= 0)
) ENGINE=InnoDB;

-- users

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(120) NOT NULL,
    login         VARCHAR(60)  NOT NULL,
    password_hash CHAR(60)     NOT NULL,       -- hash BCrypt: sempre 60 caracteres
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_users_login UNIQUE (login)
) ENGINE=InnoDB;

-- sessions

CREATE TABLE IF NOT EXISTS sessions (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    token      VARCHAR(64) NOT NULL,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME    NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uq_sessions_token UNIQUE (token),
    CONSTRAINT fk_sessions_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;