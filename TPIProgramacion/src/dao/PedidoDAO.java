/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import config.DatabaseConnection;
import models.*; // Incluye todo el paquete.
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Natalia
 */
/**
 * Data Access Object para la entidad Pedido (Clase A).
 * Gestiona todas las operaciones de persistencia de Pedidos.
 * * Características:
 * - Implementa GenericDAO<Pedido> para operaciones CRUD estándar
 * - Maneja LEFT JOIN con Envio para cargar la relación 1:1 unidireccional
 * - Soportar la búsqueda especializada por 'numero' (campo UNIQUE)
 * - Soporta transacciones mediante insertTx()
 * * Patrón: DAO con try-with-resources
 */
public class PedidoDAO implements GenericDAO<Pedido> {

    private static final String TABLE_A = "pedidos";
    private static final String TABLE_B = "envios";
    
    // El campo de la FK en la tabla B (envios) que apunta a A (pedidos)
    private static final String FK_FIELD = "pedido_id"; 

    /** Query de inserción de Pedido. Inserta numero, fecha, clienteNombre, total, estado, y la FK del Envío. */
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_A + 
            " (numero, fecha, clienteNombre, total, estado, envio_id) VALUES (?, ?, ?, ?, ?, ?)";

    /** Query de actualización de Pedido. Actualiza todos los campos, incluida la FK del Envío. */
    private static final String UPDATE_SQL = "UPDATE " + TABLE_A + 
            " SET numero = ?, fecha = ?, clienteNombre = ?, total = ?, estado = ?, envio_id = ? WHERE id = ?";

    /** Query de soft delete. Marca eliminado=TRUE en Pedido. */
    private static final String DELETE_SQL = "UPDATE " + TABLE_A + " SET eliminado = TRUE WHERE id = ?";

    /** * Query base para SELECTs (ByID y All).
     * LEFT JOIN es crucial para cargar el Envío (Clase B) asociado.
     * Nota: La FK que une es (pedidos.envio_id = envios.id).
     */
    private static final String SELECT_BASE_FIELDS = 
            "p.id, p.eliminado, p.numero, p.fecha, p.clienteNombre, p.total, p.estado, p.envio_id, " +
            "e.id AS envio_id_fk, e.eliminado AS e_eliminado, e.tracking, e.empresa, e.tipo, e.costo, e.fechaDespacho, e.fechaEstimada, e.estado AS e_estado ";
    
    private static final String LEFT_JOIN = " FROM " + TABLE_A + " p LEFT JOIN " + TABLE_B + " e ON p.envio_id = e.id ";

    private static final String SELECT_BY_ID_SQL = "SELECT " + SELECT_BASE_FIELDS + LEFT_JOIN + 
            " WHERE p.id = ? AND p.eliminado = FALSE";

    private static final String SELECT_ALL_SQL = "SELECT " + SELECT_BASE_FIELDS + LEFT_JOIN + 
            " WHERE p.eliminado = FALSE";
            
    /** Búsqueda especializada por el campo único 'numero'. */
    private static final String SEARCH_BY_NUMERO_SQL = "SELECT " + SELECT_BASE_FIELDS + LEFT_JOIN + 
            " WHERE p.eliminado = FALSE AND p.numero = ?";
    
    private final EnvioDAO envioDAO;

    public PedidoDAO(EnvioDAO envioDAO) {
        if (envioDAO == null) {
            throw new IllegalArgumentException("EnvioDAO no puede ser null");
        }
        this.envioDAO = envioDAO;
    }
    
    @Override
    public void insertar(Pedido pedido) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            setPedidoParameters(stmt, pedido);
            stmt.executeUpdate();
            setGeneratedId(stmt, pedido);
        }
    }

    /**
     * Inserta un Pedido dentro de una transacción.
     * CRUCIAL para que PedidoService pueda primero insertar el Envío (si existe), 
     * obtener el ID, e inmediatamente insertarlo aquí como FK.
     */
    @Override
    public void insertTx(Pedido pedido, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setPedidoParameters(stmt, pedido);
            stmt.executeUpdate();
            setGeneratedId(stmt, pedido);
        }
    }

    @Override
    public void actualizar(Pedido pedido) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            setPedidoParameters(stmt, pedido);
            stmt.setLong(7, pedido.getId()); // ID es el último parámetro

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar el Pedido con ID: " + pedido.getId());
            }
        }
    }

    /**
     * Elimina lógicamente un Pedido.
     * * IMPORTANTE: Este soft delete NO afecta al Envío asociado (B). 
     * La lógica de "desasociar" el Envío o eliminarlo (si es el último referente) 
     * recae en la capa Service (PedidoService).
     * * @param id ID del Pedido a eliminar
     * @throws Exception Si el Pedido no existe o hay error de BD
     */
    @Override
    public void eliminar(Long id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("No se encontró Pedido con ID: " + id);
            }
        }
    }

    @Override
    public Pedido getById(Long id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPedido(rs);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error al obtener Pedido por ID: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Pedido> getAll() throws Exception {
        List<Pedido> pedidos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

            while (rs.next()) {
                pedidos.add(mapResultSetToPedido(rs));
            }
        } catch (SQLException e) {
            throw new Exception("Error al obtener todos los Pedidos: " + e.getMessage(), e);
        }
        return pedidos;
    }
    
    /**
     * Busca un Pedido por su número exacto (campo UNIQUE).
     * @param numero Número exacto a buscar
     * @return Pedido con ese número, o null si no existe o está eliminado
     * @throws SQLException Si hay error de BD
     */
    public Pedido buscarPorNumero(String numero) throws SQLException {
        if (numero == null || numero.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de pedido no puede estar vacío");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_BY_NUMERO_SQL)) {

            stmt.setString(1, numero.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPedido(rs);
                }
            }
        }
        return null;
    }

    /**
     * Setea los parámetros del Pedido en un PreparedStatement para INSERT/UPDATE.
     * Incluye el manejo de la FK 'envio_id'.
     */
    private void setPedidoParameters(PreparedStatement stmt, Pedido pedido) throws SQLException {
        stmt.setString(1, pedido.getNumero());
        stmt.setObject(2, pedido.getFecha()); 
        stmt.setString(3, pedido.getClienteNombre());
        stmt.setDouble(4, pedido.getTotal());
        stmt.setString(5, pedido.getEstado().name()); 
        setEnvioId(stmt, 6, pedido.getEnvio());
    }

    /**
     * Setea la FK 'envio_id' en un PreparedStatement.
     * Maneja correctamente el caso NULL (Pedido sin Envío).
     */
    private void setEnvioId(PreparedStatement stmt, int parameterIndex, Envio envio) throws SQLException {
        if (envio != null && envio.getId() != null && envio.getId() > 0) {
            stmt.setLong(parameterIndex, envio.getId()); 
        } else {
            stmt.setNull(parameterIndex, Types.BIGINT);
        }
    }

    /**
     * Obtiene el ID autogenerado por la BD y lo asigna al objeto Pedido.
     */
    private void setGeneratedId(PreparedStatement stmt, Pedido pedido) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                pedido.setId(generatedKeys.getLong(1)); 
            } else {
                throw new SQLException("La inserción del Pedido falló, no se obtuvo ID generado");
            }
        }
    }
    /**
     * Mapea un ResultSet a un objeto Pedido, incluyendo su Envío (LEFT JOIN).
     */
    private Pedido mapResultSetToPedido(ResultSet rs) throws SQLException {
        // Mapeo de la Clase A (Pedido)
        Pedido pedido = new Pedido();
        pedido.setId(rs.getLong("id"));
        pedido.setEliminado(rs.getBoolean("eliminado"));
        pedido.setNumero(rs.getString("numero"));
        
        // Mapeo de fechas
        Date fechaSql = rs.getDate("fecha");
        if (fechaSql != null) {
            pedido.setFecha(fechaSql.toLocalDate());
        }

        pedido.setClienteNombre(rs.getString("clienteNombre"));
        pedido.setTotal(rs.getDouble("total"));
        // Mapeo de String a Enum para Pedido
        pedido.setEstado(EstadoPedido.valueOf(rs.getString("estado")));

        // Manejo de LEFT JOIN: verificar si el envío_id es NULL (Pedido sin Envío)
        Long envioIdFK = rs.getLong("envio_id");
        if (!rs.wasNull() && envioIdFK != null && envioIdFK > 0) {
            // Mapeo de la Clase B (Envio)
            Envio envio = new Envio();
            envio.setId(envioIdFK);
            envio.setEliminado(rs.getBoolean("e_eliminado"));
            envio.setTracking(rs.getString("tracking"));
            
            // Mapeo de Enums de Envío
            envio.setEmpresa(EmpresaEnvio.valueOf(rs.getString("empresa")));
            envio.setTipo(TipoEnvio.valueOf(rs.getString("tipo")));
            envio.setCosto(rs.getDouble("costo"));
            
            // Mapeo de fechas de Envío
            Date fechaDespachoSql = rs.getDate("fechaDespacho");
            if (fechaDespachoSql != null) {
                envio.setFechaDespacho(fechaDespachoSql.toLocalDate());
            }
            Date fechaEstimadaSql = rs.getDate("fechaEstimada");
            if (fechaEstimadaSql != null) {
                envio.setFechaEstimada(fechaEstimadaSql.toLocalDate());
            }
            
            envio.setEstado(EstadoEnvio.valueOf(rs.getString("e_estado")));
            
            pedido.setEnvio(envio);
        }

        return pedido;
    }

    public EnvioDAO getEnvioDAO() {
        return envioDAO;
    }
}