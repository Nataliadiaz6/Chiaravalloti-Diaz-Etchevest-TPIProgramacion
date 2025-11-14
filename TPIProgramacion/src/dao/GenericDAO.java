/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.Connection;
import java.util.List;

/**
 * Interfaz genérica que define métodos comunes para trabajar con cualquier entidad.
 * Tipo de dato T debe heredar de Base.
 * * Propósito:
 * - Servir como base para evitar repetir la firma de los métodos CRUD en distintas clases DAO.
 * - Soportar el patrón DAO genérico, permitiendo crear GenericService<T> más adelante.
 * * NOTA: Los IDs de la base de datos se manejan como Long (se requiere Long para el método getById).
 */
public interface GenericDAO<T> {
    /**
     * Inserta una nueva entidad en la base de datos (versión sin transacción).
     * @param entidad Entidad a insertar (su ID será actualizado al finalizar)
     * @throws Exception Si ocurre un error de persistencia
     */
    void insertar(T entidad) throws Exception;
    
    /**
     * Inserta una nueva entidad dentro de una transacción ya iniciada.
     * NO abre ni cierra la conexión; solo la utiliza.
     * @param entidad Entidad a insertar
     * @param conn Conexión transaccional externa (con setAutoCommit(false))
     * @throws Exception Si ocurre un error de persistencia
     */
    void insertTx(T entidad, Connection conn) throws Exception;
    
    /**
     * Actualiza una entidad existente en la base de datos.
     * @param entidad Entidad con los datos a actualizar (ID debe ser válido)
     * @throws Exception Si ocurre un error de persistencia
     */
    void actualizar(T entidad)throws Exception;
    
    /**
     * Elimina lógicamente una entidad (soft delete: setea eliminado=TRUE).
     * @param id ID de la entidad a eliminar
     * @throws Exception Si la entidad no existe o hay un error de BD
     */
    void eliminar(Long id)throws Exception; 
    
    /**
     * Obtiene una entidad por su identificador.
     * @param id ID de la entidad a buscar
     * @return Entidad encontrada, o null si no existe o está eliminada (eliminado=FALSE)
     * @throws Exception Si ocurre un error de persistencia
     */
    T getById(Long id)throws Exception; 
    
    /**
     * Obtiene todas las entidades activas del sistema.
     * @return Lista de entidades activas (eliminado=FALSE)
     * @throws Exception Si ocurre un error de persistencia
     */
    List<T> getAll()throws Exception;

}