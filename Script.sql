DROP DATABASE IF EXISTS platforma_licitatii;
CREATE DATABASE platforma_licitatii;


CREATE TABLE utilizator (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    parola VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    sold DOUBLE NOT NULL DEFAULT 0.0
);

CREATE TABLE produs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titlu VARCHAR(100) NOT NULL,
    descriere TEXT,
    pret_start DOUBLE NOT NULL,
    id_utilizator INT NOT NULL,
    FOREIGN KEY (id_utilizator) REFERENCES utilizator(id)
);

CREATE TABLE licitatie (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_produs INT NOT NULL,
    id_vanzator INT NOT NULL,
    data_limita DATETIME NOT NULL,
    FOREIGN KEY (id_produs) REFERENCES produs(id),
    FOREIGN KEY (id_vanzator) REFERENCES utilizator(id)
);


CREATE TABLE oferta (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_licitatie INT NOT NULL,
    id_utilizator INT NOT NULL,
    suma DOUBLE NOT NULL,
    data DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_licitatie) REFERENCES licitatie(id),
    FOREIGN KEY (id_utilizator) REFERENCES utilizator(id)
);

CREATE TABLE administrator (
    id_utilizator INT PRIMARY KEY,
    FOREIGN KEY (id_utilizator) REFERENCES utilizator(id) ON DELETE CASCADE
);

DROP TABLE mesaj;



CREATE TABLE mesaj (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_expeditor INT NOT NULL,
    id_destinatar INT NOT NULL,
    continut TEXT NOT NULL,
    data_trimiterii DATETIME NOT NULL,
    citit BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id_expeditor) REFERENCES utilizator(id),
    FOREIGN KEY (id_destinatar) REFERENCES utilizator(id)
);

ALTER TABLE mesaj
ADD CONSTRAINT chk_mesaj_expeditor_id_positiv CHECK (id_expeditor > 0);

ALTER TABLE mesaj
ADD CONSTRAINT chk_mesaj_destinatar_id_positiv CHECK (id_destinatar > 0);

ALTER TABLE licitatie
ADD CONSTRAINT chk_licitatie_vanzator_id_positiv CHECK (id_vanzator > 0);

ALTER TABLE oferta
ADD CONSTRAINT chk_oferta_utilizator_id_positiv CHECK (id_utilizator > 0);

ALTER TABLE produs 
ADD CONSTRAINT chk_produs_utilizator_id_positiv CHECK (id_utilizator > 0);

ALTER TABLE administrator 
ADD CONSTRAINT chk_administrator_utilizator_id_positiv CHECK (id_utilizator > 0);

SHOW COLUMNS FROM mesaj;

ALTER TABLE utilizator
MODIFY username VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL UNIQUE;

ALTER TABLE licitatie
DROP COLUMN procesate;

ALTER TABLE licitatie
ADD COLUMN procesata BOOLEAN DEFAULT FALSE;



INSERT INTO utilizator (username, parola, email, sold)
VALUES ('admin', 'admin', 'admin@email.com', 1000.0);


INSERT INTO administrator (id_utilizator)
VALUES (1);

COMMIT 

USE platforma_licitatii;
SELECT * FROM administrator;


SELECT id, username FROM utilizator WHERE username='admin'

SELECT id_utilizator FROM administrator WHERE id_utilizator = 1;