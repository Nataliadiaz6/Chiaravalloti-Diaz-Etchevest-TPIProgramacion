/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import config.DatabaseConnection;
import models.Envio;
import models.EstadoEnvio;
import models.TipoEnvio;
import models.EmpresaEnvio;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Envío (Clase B).
 * Gestiona todas las operaciones de persistencia de envíos.
 *
 * Características:
 * - Implementa GenericDAO<Envio> para operaciones CRUD estándar
 * - NO maneja directamente la relación 1:1, solo sus propios datos (es la clase B)
 * - Soporta transacciones mediante insertTx()
 * - Usa Long para IDs
 * * Patrón: DAO con try-with-resources para manejo automático de recursos JDBC
 */
public class EnvioDAO implements GenericDAO<Envio> {
    
    // Nombres de tablas y campos específicos del dominio Envío
    private static final String TABLE = "envios";

    /** Query de inserción. Inserta todos los campos de Envío. */
    private static final String INSERT_SQL = "INSERT INTO " + TABLE + 
            " (tracking, empresa, tipo, costo, fechaDespacho, fechaEstimada, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";

    /** Query de actualización. Actualiza todos los campos de Envío, excepto el ID. */
    private static final String UPDATE_SQL = "UPDATE " + TABLE + 
            " SET tracking = ?, empresa = ?, tipo = ?, costo = ?, fechaDespacho = ?, fechaEstimada = ?, estado = ? WHERE id = ?";

    /** Query de soft delete. Marca eliminado=TRUE sin borrar físicamente. */
    private static final String DELETE_SQL = "UPDATE " + TABLE + " SET eliminado = TRUE WHERE id = ?";

    /** Query para obtener Envío por ID. Solo retorna envíos activos (eliminado=FALSE). */
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM " + TABLE + " WHERE id = ? AND eliminado = FALSE";

    /** Query para obtener todos los envíos activos. */
    private static final String SELECT_ALL_SQL = "SELECT * FROM " + TABLE + " WHERE eliminado = FALSE";
    
    /** Query de búsqueda exacta por Tracking. Usado para validar unicidad. */
    private static final String SEARCH_BY_TRACKING_SQL = "SELECT * FROM " + TABLE + " WHERE tracking = ? AND eliminado = FALSE";
    
    /**
     * Inserta un Envío en la base de datos (versión sin transacción).
     * Flujo: Abre/cierra conexión, ejecuta INSERT, obtiene y asigna el ID generado.
     * * @param envio Envío a insertar
     * @throws SQLException Si falla la inserción o no se obtiene ID
     */
    @Override
    public void insertar(Envio envio) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            setEnvioParameters(stmt, envio);
            stmt.executeUpdate();
            setGeneratedId(stmt, envio);
        }
    }
    /**
     * Inserta un Envío dentro de una transacción existente (insertTx).
     * Es crucial para que PedidoService pueda crear el Envío y el Pedido en una sola transacción.
     * @param envio Envío a insertar
     * @param conn Conexión transaccional externa (NO se cierra aquí)
     * @throws Exception Si falla la inserción
     */
    @Override
    public void insertTx(Envio envio, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setEnvioParameters(stmt, envio);
            stmt.executeUpdate();
            setGeneratedId(stmt, envio);
        }
    }
    /**
     * Actualiza un Envío existente.
     * @param envio Envío con los datos actualizados (id debe ser > 0)
     * @throws SQLException Si el Envío no existe o hay error de BD
     */
    @Override
    public void actualizar(Envio envio) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            setEnvioParameters(stmt, envio);
            stmt.setLong(8, envio.getId()); // ID es el último parámetro

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar el Envío con ID: " + envio.getId());
            }
        }
    }
    
    /**
     * Elimina lógicamente un Envío (soft delete).
     * Marca eliminado=TRUE sin borrar físicamente la fila.
     * * @param id ID del Envío a eliminar
     * @throws SQLException Si el Envío no existe o hay error de BD
     */
    @Override
    public void eliminar(Long id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("No se encontró Envío con ID: " + id);
            }
        }
    }
    
    /**
     * Obtiene un Envío por su ID.
     * @param id ID del Envío a buscar
     * @return Envío encontrado, o null si no existe o está eliminado
     * @throws SQLException Si hay error de BD
     */
    @Override
    public Envio getById(Long id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEnvio(rs);
                }
            }
        }
        return null;
    }
    /**
     * Busca un Envío por su código de Tracking exacto.
     * Es la implementación requerida para la validación de unicidad en la capa Service.
     * * @param tracking Código de seguimiento (UNIQUE)
     * @return Envío encontrado, o null si no existe o está eliminado
     * @throws SQLException Si hay error de BD
     */
    public Envio buscarPorTracking(String tracking) throws SQLException {
        if (tracking == null || tracking.trim().isEmpty()) {
            throw new IllegalArgumentException("El tracking no puede estar vacío");
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_BY_TRACKING_SQL)) {

            stmt.setString(1, tracking.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEnvio(rs);
                }
            }
        }
        return null;
    }
    /**
     * Obtiene todos los Envíos activos (eliminado=FALSE).
     * @return Lista de Envíos activos
     * @throws SQLException Si hay error de BD
     */
    @Override
    public List<Envio> getAll() throws SQLException {
        List<Envio> envios = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

            while (rs.next()) {
                envios.add(mapResultSetToEnvio(rs));
            }
        }
        return envios;
    }

    /**
     * Setea los parámetros del Envío en un PreparedStatement para INSERT/UPDATE.
     * * @param stmt PreparedStatement (INSERT_SQL o UPDATE_SQL)
     * @param envio Envío con los datos a persistir
     * @throws SQLException Si hay error al setear parámetros
     */
    private void setEnvioParameters(PreparedStatement stmt, Envio envio) throws SQLException {
        stmt.setString(1, envio.getTracking());
        stmt.setString(2, envio.getEmpresa().name());
        stmt.setString(3, envio.getTipo().name());
        stmt.setDouble(4, envio.getCosto());
        stmt.setObject(5, envio.getFechaDespacho()); 
        stmt.setObject(6, envio.getFechaEstimada());
        stmt.setString(7, envio.getEstado().name());
    }

    /**
     * Obtiene el ID autogenerado por la BD y lo asigna al objeto Envío.
     * @param stmt PreparedStatement que ejecutó el INSERT
     * @param envio Objeto Envío a actualizar con el ID generado
     * @throws SQLException Si no se pudo obtener el ID
     */
    private void setGeneratedId(PreparedStatement stmt, Envio envio) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                envio.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("La inserción del Envío falló, no se obtuvo ID generado");
            }
        }
    }
    /**
     * Mapea un ResultSet a un objeto Envío.
     * Reconstruye el objeto usando el constructor completo.
     * * Mapeo de Enums: Los Strings de la BD son convertidos a objetos Enum usando Enum.valueOf().
     * * @param rs ResultSet posicionado en una fila
     * @return Envío reconstruido
     * @throws SQLException Si hay error al leer columnas
     */
    private Envio mapResultSetToEnvio(ResultSet rs) throws SQLException {
        // Mapeo de fechas
        LocalDate fechaDespacho = rs.getDate("fechaDespacho") != null ? rs.getDate("fechaDespacho").toLocalDate() : null;
        LocalDate fechaEstimada = rs.getDate("fechaEstimada") != null ? rs.getDate("fechaEstimada").toLocalDate() : null;
        
        // Mapeo de Strings de BD a Enums de Java:
        EmpresaEnvio empresaEnum = EmpresaEnvio.valueOf(rs.getString("empresa"));
        TipoEnvio tipoEnum = TipoEnvio.valueOf(rs.getString("tipo"));
        EstadoEnvio estadoEnum = EstadoEnvio.valueOf(rs.getString("estado"));
        
        return new Envio(
            rs.getLong("id"),
            rs.getBoolean("eliminado"),
            rs.getString("tracking"),
            empresaEnum, 
            tipoEnum, 
            rs.getDouble("costo"),
            fechaDespacho,
            fechaEstimada,
            estadoEnum 
        );
    }
}