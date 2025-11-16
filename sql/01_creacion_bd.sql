CREATE DATABASE IF NOT EXISTS tpiprogramacion
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE tpiprogramacion;

CREATE TABLE IF NOT EXISTS envio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    eliminado BOOLEAN NOT NULL DEFAULT 0,
    tracking VARCHAR(40) UNIQUE,
    empresa ENUM('ANDREANI', 'OCA', 'CORREO_ARG'),
    tipo ENUM('ESTANDAR', 'EXPRES'),
    costo DECIMAL(10,2),
    fechaDespacho DATE,  
    fechaEstimada DATE, 
    estado ENUM('EN_PREPARACION', 'EN_TRANSITO', 'ENTREGADO')
);

CREATE TABLE IF NOT EXISTS pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    eliminado BOOLEAN NOT NULL DEFAULT 0,
    numero VARCHAR(20) NOT NULL UNIQUE,
    fecha DATE NOT NULL,
    clienteNombre VARCHAR(120) NOT NULL, 
    total DECIMAL(12,2) NOT NULL,
    estado ENUM('NUEVO', 'FACTURADO', 'ENVIADO') NOT NULL,
    envioId BIGINT UNIQUE, 
    CONSTRAINT fk_pedido_envio
        FOREIGN KEY (envioId)
        REFERENCES envio(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

