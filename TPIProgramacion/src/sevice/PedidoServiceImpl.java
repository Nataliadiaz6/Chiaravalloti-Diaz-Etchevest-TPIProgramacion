/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sevice;

import config.DatabaseConnection;
import config.TransactionManager;
import dao.EnvioDAO;
import dao.PedidoDAO;
import models.Envio;
import models.Pedido;
import models.EstadoPedido;

import java.sql.Connection;
import java.util.List;

/**
 * Implementación del servicio de negocio para la entidad Pedido (Clase A).
 * Capa intermedia que aplica validaciones y ORQUESTA TRANSACCIONES.
 * * Responsabilidades:
 * - Validar datos de Pedido (numero, total, estado obligatorios)
 * - Garantizar unicidad del Número de Pedido.
 * - Coordinar operaciones transaccionales (Pedido y Envío deben crearse juntos).
 * - Manejar la relación 1:1 unidireccional: insertar Envío primero, luego Pedido con la FK.
 * * Patrón: Service Layer con gestión de transacciones.
 */
public class PedidoServiceImpl implements GenericService<Pedido> {
    
    /** DAO para acceso a datos de Pedidos. */
    private final PedidoDAO pedidoDAO;

    /** Servicio de Envío para coordinar la creación y validación de la Clase B. */
    private final EnvioServiceImpl envioServiceImpl;

    /**
     * Constructor con inyección de dependencias.
     * * @param pedidoDAO DAO de pedidos
     * @param envioServiceImpl Servicio de envíos
     * @throws IllegalArgumentException si alguna dependencia es null
     */
    public PedidoServiceImpl(PedidoDAO pedidoDAO, EnvioServiceImpl envioServiceImpl) {
        if (pedidoDAO == null) {
            throw new IllegalArgumentException("PedidoDAO no puede ser null");
        }
        if (envioServiceImpl == null) {
            throw new IllegalArgumentException("EnvioServiceImpl no puede ser null");
        }
        this.pedidoDAO = pedidoDAO;
        this.envioServiceImpl = envioServiceImpl;
    }

    /**
     * Inserta un nuevo Pedido en la base de datos, manejando transacciones.
     *
     * Flujo transaccional:
     * 1. Abre conexión y desactiva autoCommit.
     * 2. Valida Pedido (incluyendo unicidad del número).
     * 3. Si el Pedido tiene Envío asociado: inserta el Envío en BD usando la conexión transaccional.
     * 4. Inserta el Pedido en BD usando la conexión transaccional (referenciando el ID del Envío recién creado).
     * 5. Si todo es OK: commit().
     * 6. Si hay error: rollback().
     *
     * @param pedido Pedido a insertar (id será regenerado)
     * @throws Exception Si la validación falla, el número está duplicado, o hay error de BD
     */
    @Override
    public void insertar(Pedido pedido) throws Exception {
        // Validación de campos obligatorios del Pedido
        validatePedido(pedido);
        validateNumeroUnique(pedido.getNumero(), null);

        // 1. Abrir Transacción
        try (TransactionManager tx = new TransactionManager(DatabaseConnection.getConnection())) {
            tx.startTransaction();
            Connection conn = tx.getConnection();

            Envio envio = pedido.getEnvio();

            // 2. Coordinación: Insertar el Envío primero (si es nuevo y existe)
            if (envio != null) {
                if (envio.getId() == null || envio.getId() <= 0) {
                    // Validar e insertar el Envío, obteniendo el ID en el objeto 'envio'
                    // El DAO del Envío debe usar la conexión transaccional 'conn'
                    envioServiceImpl.validateEnvio(envio, true); // Llama al método package-private
                    pedidoDAO.getEnvioDAO().insertTx(envio, conn); 

                    // El ID de Envío ahora está en el objeto 'envio' y será usado como FK en el Pedido
                } else {
                    // Si el Envío ya tenía ID, se asume que existe y solo se actualiza (o no se hace nada)
                    envioServiceImpl.actualizar(envio); 
                }
            }

            // 3. Insertar el Pedido, usando el ID del Envío (o NULL si no hay envío)
            pedidoDAO.insertTx(pedido, conn);

            // 4. Commit si todo fue exitoso
            tx.commit();
        } catch (Exception e) {
            // El TransactionManager se encarga del rollback() automático al salir del try (AutoCloseable)
            throw new Exception("Error transaccional al insertar el Pedido y/o Envío: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza un Pedido existente.
     * * @param pedido Pedido con los datos actualizados
     * @throws Exception Si la validación falla o el Pedido no existe
     */
    @Override
    public void actualizar(Pedido pedido) throws Exception {
        validatePedido(pedido);
        if (pedido.getId() == null || pedido.getId() <= 0) {
            throw new IllegalArgumentException("El ID del Pedido debe ser mayor a 0 para actualizar");
        }
        validateNumeroUnique(pedido.getNumero(), pedido.getId());
        pedidoDAO.actualizar(pedido);
    }

    /**
     * Elimina lógicamente un Pedido (soft delete).
     * * @param id ID del Pedido a eliminar
     * @throws Exception Si id es inválido o no existe el Pedido
     */
    @Override
    public void eliminar(Long id) throws Exception {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        // En esta etapa, solo se elimina lógicamente el Pedido (A).
        // El Envío (B) asociado puede permanecer activo, cumpliendo la baja lógica del Pedido.
        pedidoDAO.eliminar(id);
    }

    @Override
    public Pedido getById(Long id) throws Exception {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        return pedidoDAO.getById(id);
    }

    @Override
    public List<Pedido> getAll() throws Exception {
        return pedidoDAO.getAll();
    }
   
    /**
     * Valida que un Pedido tenga datos correctos.
     * * Reglas de negocio aplicadas:
     * - Número, Cliente y Total son obligatorios.
     * - Estado es obligatorio.
     * * @param pedido Pedido a validar
     * @throws IllegalArgumentException Si alguna validación falla
     */
    private void validatePedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("El Pedido no puede ser null");
        }
        if (pedido.getNumero() == null || pedido.getNumero().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de pedido es obligatorio");
        }
        if (pedido.getClienteNombre() == null || pedido.getClienteNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        if (pedido.getTotal() <= 0) {
            throw new IllegalArgumentException("El total del pedido debe ser positivo");
        }
        if (pedido.getEstado() == null) {
            throw new IllegalArgumentException("El estado del pedido es obligatorio");
        }
        if (pedido.getFecha() == null) {
            throw new IllegalArgumentException("La fecha del pedido es obligatoria");
        }
    }

    /**
     * Valida que el número de Pedido sea único en el sistema.
     * @param numero Número de pedido a validar
     * @param pedidoId ID del Pedido (null para INSERT, != null para UPDATE)
     * @throws IllegalArgumentException Si el número ya existe
     * @throws Exception Si hay error de BD al buscar
     */
    private void validateNumeroUnique(String numero, Long pedidoId) throws Exception {
        Pedido existente = pedidoDAO.buscarPorNumero(numero); 
        
        if (existente != null) {
            if (pedidoId == null || !existente.getId().equals(pedidoId)) {
                throw new IllegalArgumentException("Ya existe un Pedido con el número: " + numero);
            }
        }
    }
    
    /**
     * Expone el servicio de envíos para que MenuHandler pueda usarlo directamente.
     * @return Instancia de EnvioServiceImpl inyectada
     */
    public EnvioServiceImpl getEnvioService() {
        return this.envioServiceImpl;
    }
    
    /**
     * Busca un Pedido por su número exacto (campo UNIQUE).
     * @param numero Número exacto a buscar
     * @return Pedido con ese número, o null
     * @throws Exception Si hay error de BD
     */
    public Pedido buscarPorNumero(String numero) throws Exception {
        if (numero == null || numero.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de pedido no puede estar vacío");
        }
        return pedidoDAO.buscarPorNumero(numero);
    }
}
