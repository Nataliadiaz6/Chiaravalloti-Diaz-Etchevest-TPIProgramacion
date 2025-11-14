/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

import java.sql.Connection;
import java.sql.SQLException;
/**
 *
 * @author Natalia
 */
/**
 * Clase utilitaria para gestionar transacciones JDBC.
 * Permite agrupar múltiples operaciones DAO bajo un solo commit/rollback.
 * * Patrón:
 * - Proxy / Wrapper: Envuelve una Connection y maneja su estado transaccional.
 * - AutoCloseable: Asegura que la conexión se cierre y se restaure el autoCommit al salir del try-with-resources.
 */
public class TransactionManager implements AutoCloseable {
    private Connection conn;
    private boolean transactionActive;

    /**
     * Constructor. Recibe y envuelve la conexión que será gestionada.
     * @param conn Conexión JDBC obtenida de DatabaseConnection.getConnection()
     * @throws IllegalArgumentException si la conexión es null
     */
    public TransactionManager(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("La conexión no puede ser null");
        }
        this.conn = conn;
        this.transactionActive = false;
    }

    /**
     * Obtiene la conexión gestionada.
     * Los DAOs usarán esta conexión para ejecutar sus consultas transaccionales (insertTx, updateTx).
     * @return La conexión JDBC
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * Inicia la transacción desactivando el modo auto-commit en la conexión.
     * @throws SQLException Si hay un problema con la conexión o su estado
     */
    public void startTransaction() throws SQLException {
        if (conn == null || conn.isClosed()) {
            throw new SQLException("No se puede iniciar la transacción: conexión no disponible o cerrada.");
        }
        
        // Desactiva el auto-commit para que los DAOs no confirmen los cambios inmediatamente
        conn.setAutoCommit(false);
        transactionActive = true;
    }

    /**
     * Confirma todos los cambios pendientes en la base de datos desde que se inició la transacción.
     * @throws SQLException Si hay un problema al ejecutar el commit
     */
    public void commit() throws SQLException {
        if (!transactionActive) {
            throw new SQLException("No hay una transacción activa para hacer commit.");
        }
        conn.commit();
        transactionActive = false;
    }

    /**
     * Deshace todos los cambios pendientes desde que se inició la transacción.
     * Se usa típicamente en bloques catch si ocurre una excepción.
     */
    public void rollback() {
        if (conn != null && transactionActive) {
            try {
                conn.rollback();
                transactionActive = false;
            } catch (SQLException e) {
                // Se registra el error, pero no se relanza, ya que se está en un proceso de manejo de errores
                System.err.println("Error durante el rollback: " + e.getMessage());
            }
        }
    }

    /**
     * Implementación del contrato AutoCloseable.
     * Este método se llama automáticamente al salir de un bloque try-with-resources.
     * * Acciones:
     * 1. Si la transacción aún está activa (falló o no se hizo commit), llama a rollback.
     * 2. Restaura el modo autoCommit a true (estado por defecto).
     * 3. Cierra la conexión.
     */
    @Override
    public void close() {
        if (conn != null) {
            try {
                // Si la transacción no se terminó (commit/rollback), se hace rollback
                if (transactionActive) {
                    rollback();
                }
                
                // Restaura el modo por defecto para no afectar a futuros usos de la conexión (aunque se va a cerrar)
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar o restablecer la conexión: " + e.getMessage());
            }
        }
    }

    /**
     * Verifica si la transacción está actualmente activa (autoCommit=false).
     * @return true si la transacción está activa
     */
    public boolean isTransactionActive() {
        return transactionActive;
    }
}
