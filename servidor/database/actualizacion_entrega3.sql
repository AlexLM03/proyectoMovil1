-- ============================================================================
-- Actualización: direcciones de entrega y método de pago
--
-- Se aplica sobre una base ya instalada (pantradicion_db) sin borrar datos.
-- Para una instalación nueva, panaderia.sql ya incluye estos cambios.
--
-- Uso:
--   C:\xampp\mysql\bin\mysql.exe -u root --default-character-set=utf8mb4 -e "source C:/xampp/virtualhost/panaderia/database/actualizacion_entrega3.sql"
-- ============================================================================

USE pantradicion_db;

-- Direcciones del cliente (varias por usuario, una predeterminada).
CREATE TABLE IF NOT EXISTS direcciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    alias VARCHAR(40) NOT NULL,
    calle VARCHAR(160) NOT NULL,
    referencias VARCHAR(160) NULL,
    predeterminada BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_direcciones_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- Método de pago preferido del cliente.
ALTER TABLE usuarios
    ADD COLUMN metodo_pago_preferido ENUM('EFECTIVO', 'TARJETA_TERMINAL', 'TRANSFERENCIA')
    NOT NULL DEFAULT 'EFECTIVO';

-- Método de pago con el que se hizo cada pedido.
ALTER TABLE pedidos
    ADD COLUMN metodo_pago ENUM('EFECTIVO', 'TARJETA_TERMINAL', 'TRANSFERENCIA')
    NOT NULL DEFAULT 'EFECTIVO';

-- Direcciones de ejemplo para la cuenta cliente (Janetzy).
INSERT INTO direcciones (usuario_id, alias, calle, referencias, predeterminada) VALUES
(3, 'Casa', 'Av. Juárez 165, Acámbaro, Guanajuato', 'Portón verde, frente a la papelera', 1),
(3, 'Trabajo', 'Prol. Pino Suárez 80, Acámbaro', 'Edificio de oficinas, segundo piso', 0);
