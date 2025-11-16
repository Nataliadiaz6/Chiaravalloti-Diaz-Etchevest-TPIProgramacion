USE tpi;

INSERT INTO pedido (eliminado, numero, fecha, clienteNombre, total, estado)
VALUES
(0, 'P-0001', '2025-11-01', 'Juan Pérez',   15000.00, 'NUEVO'),
(0, 'P-0002', '2025-11-02', 'María López',  23000.50, 'FACTURADO'),
(0, 'P-0003', '2025-11-03', 'Carlos Gómez',  8000.00, 'ENVIADO');

INSERT INTO envio (eliminado, tracking, empresa, tipo, costo,
                   fechaDespacho, fechaEstimada, estado, pedido.id)
VALUES
(0, 'TRK-AND-0001', 'ANDREANI', 'ESTANDAR', 1200.00,
 '2025-11-02', '2025-11-05', 'EN_TRANSITO', 1),
(0, 'TRK-OCA-0002', 'OCA', 'EXPRES', 1800.00,
 '2025-11-03', '2025-11-04', 'ENTREGADO', 2);
