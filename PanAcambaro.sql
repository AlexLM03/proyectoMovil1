DROP DATABASE IF EXISTS pantradicion_db;
CREATE DATABASE pantradicion_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pantradicion_db;

-- 1. Categorías
CREATE TABLE categorias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    icono_emoji VARCHAR(10) DEFAULT '🥐',
    activo BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

-- 2. Usuarios
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    correo VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    rol ENUM('CLIENTE', 'ADMIN', 'REPARTIDOR') DEFAULT 'CLIENTE',
    activo BOOLEAN DEFAULT TRUE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Productos
CREATE TABLE productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    categoria_id INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    emoji VARCHAR(10) DEFAULT '🥖',
    disponible BOOLEAN DEFAULT TRUE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_productos_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 4. Tandas de Horneado
CREATE TABLE tandas_horneado (
    id INT AUTO_INCREMENT PRIMARY KEY,
    producto_id INT NOT NULL,
    hora_salida TIME NOT NULL,
    piezas_estimadas INT NOT NULL DEFAULT 50,
    piezas_disponibles INT NOT NULL DEFAULT 50,
    estado ENUM('EN_HORNO', 'LISTO_CALIENTE', 'AGOTADO') DEFAULT 'EN_HORNO',
    dias_semana VARCHAR(60) DEFAULT 'Lunes a Domingo',
    CONSTRAINT fk_tandas_producto FOREIGN KEY (producto_id) REFERENCES productos(id) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Pedidos
CREATE TABLE pedidos (
    folio VARCHAR(30) PRIMARY KEY,
    cliente_id INT NULL,
    cliente_nombre VARCHAR(120) NOT NULL,
    cliente_telefono VARCHAR(20) NOT NULL,
    tipo_entrega ENUM('A domicilio', 'Para recoger') NOT NULL DEFAULT 'A domicilio',
    direccion_entrega VARCHAR(255) NULL,
    hora_recoleccion TIME NULL,
    tipo_empaque ENUM('Bolsa ecológica de papel', 'Caja tradicional de cartón') NOT NULL DEFAULT 'Bolsa ecológica de papel',
    requiere_rebanado BOOLEAN NOT NULL DEFAULT FALSE,
    notas_panadero TEXT NULL,
    subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    costo_envio DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    estado ENUM('RECIBIDO', 'EN_PREPARACION', 'EN_CAMINO', 'ENTREGADO', 'CANCELADO') NOT NULL DEFAULT 'RECIBIDO',
    repartidor_id INT NULL,
    tiempo_estimado VARCHAR(40) DEFAULT '15 min',
    fecha_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedidos_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_pedidos_repartidor FOREIGN KEY (repartidor_id) REFERENCES usuarios(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- 6. Detalles del Pedido
CREATE TABLE detalles_pedido (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pedido_folio VARCHAR(30) NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
    CONSTRAINT fk_detalles_pedido FOREIGN KEY (pedido_folio) REFERENCES pedidos(folio) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_detalles_producto FOREIGN KEY (producto_id) REFERENCES productos(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 7. Insumos
CREATE TABLE insumos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    stock DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    minimo DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    unidad VARCHAR(20) NOT NULL,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 8. Relación Producto - Insumos
CREATE TABLE producto_insumos (
    producto_id INT NOT NULL,
    insumo_id INT NOT NULL,
    cantidad_requerida DECIMAL(10, 3) NOT NULL,
    PRIMARY KEY (producto_id, insumo_id),
    CONSTRAINT fk_receta_producto FOREIGN KEY (producto_id) REFERENCES productos(id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_receta_insumo FOREIGN KEY (insumo_id) REFERENCES insumos(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- DATOS INICIALES
INSERT INTO categorias (id, nombre, icono_emoji) VALUES
(1, 'Pan dulce', '🥯'), (2, 'Pan salado', '🥖'), (3, 'Pasteles', '🍰'), (4, 'Bebidas', '☕');

INSERT INTO usuarios (id, nombre, correo, password_hash, telefono, direccion, rol) VALUES
(1, 'Administrador Acambapan', 'admin@pantradicion.com', '123456', '445 000 0001', 'Centro, Acámbaro, Gto.', 'ADMIN'),
(2, 'Luis Martínez', 'luis.reparto@pantradicion.com', '123456', '55 8765 4321', 'Col. Ferrocarrilera, Acámbaro', 'REPARTIDOR'),
(3, 'Janetzy Maldonado Nava', 's23120227@alumnos.itsur.edu.mx', '123456', '445 217 3556', 'Av. Juárez 165, Acámbaro, Guanajuato', 'CLIENTE');

INSERT INTO productos (id, categoria_id, nombre, descripcion, precio, emoji, disponible) VALUES
(1, 1, 'Concha de vainilla', 'Costra tradicional de azúcar y extracto de vainilla', 18.00, '🥯', 1),
(2, 1, 'Concha de chocolate', 'Costra de cacao y chocolate amargo tradicional', 18.00, '🍫', 1),
(3, 1, 'Cuernito de mantequilla', 'Hojaldre artesanal recién horneado con mantequilla pura', 16.00, '🥐', 1),
(4, 1, 'Orejas de hojaldre', 'Crujiente hojaldre acaramelado con azúcar y canela', 17.00, '🥨', 1),
(5, 2, 'Bolillo artesanal', 'Pan blanco crujiente de sal, horneado en piso cada hora', 6.00, '🥖', 1),
(6, 2, 'Telera tradicional', 'Pan suave de sal para tortas, molletes y lonches', 12.00, '🫓', 1),
(7, 1, 'Pan grande de muerto', 'Especialidad de temporada con flor de naranja y ajonjolí', 45.00, '🍞', 1),
(8, 3, 'Rebanada de pastel chocolate', 'Tres capas esponjosas rellenas de ganache artesanal', 55.00, '🍰', 1),
(9, 3, 'Cheesecake de fresa', 'Base crujiente de galleta con mermelada de fresa natural', 60.00, '🍓', 1),
(10, 4, 'Café de olla', 'Café de grano infusionado con canela y piloncillo en barro', 25.00, '☕', 1),
(11, 4, 'Chocolate caliente', 'Chocolate de mesa tradicional batido con leche entera', 30.00, '🍵', 1),
(12, 1, 'Dona glaseada', 'Masa suave y esponjosa con glaseado clásico de azúcar', 20.00, '🍩', 1);

INSERT INTO insumos (id, nombre, stock, minimo, unidad) VALUES
(1, 'Harina de trigo', 40.00, 10.00, 'sacos'),
(2, 'Azúcar estándar', 25.00, 8.00, 'kg'),
(3, 'Mantequilla pura', 6.00, 6.00, 'kg'),
(4, 'Levadura fresca', 3.00, 4.00, 'kg'),
(5, 'Huevo de granja', 180.00, 60.00, 'piezas'),
(6, 'Leche entera', 30.00, 10.00, 'litros'),
(7, 'Chocolate amargo', 8.00, 5.00, 'kg'),
(8, 'Café en grano', 12.00, 4.00, 'kg');

INSERT INTO pedidos (folio, cliente_id, cliente_nombre, cliente_telefono, tipo_entrega, direccion_entrega, subtotal, costo_envio, total, estado, repartidor_id, tiempo_estimado) VALUES
('ACB-1042', 3, 'Janetzy Maldonado', '445 217 3556', 'A domicilio', 'Av. Juárez 165, Acámbaro, Guanajuato', 115.00, 30.00, 145.00, 'EN_CAMINO', 2, '12 min'),
('ACB-1040', 1, 'Ambar Fujarte', '445 333 4455', 'A domicilio', 'Prol. Pino Suárez 80', 320.00, 0.00, 320.00, 'ENTREGADO', 2, 'Entregado');

INSERT INTO detalles_pedido (pedido_folio, producto_id, cantidad, precio_unitario) VALUES
('ACB-1042', 1, 2, 18.00),
('ACB-1042', 5, 4, 6.00),
('ACB-1042', 10, 1, 25.00);