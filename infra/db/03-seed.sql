-- 03-seed.sql — Armazém (Redes II)
-- Dados mínimos para demonstrar o sistema: um usuário de acesso.

USE ArmazemRedes;

INSERT IGNORE INTO users (name, login, password_hash)
VALUES ('Usuario de Demonstracao', 'demo', 'HASH_BCRYPT_AQUI');