/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sevice;

import java.util.List;


/**
 *
 * @author Natalia
 */
/**
 * Interfaz genérica que define los métodos de la capa de servicio (negocio).
 * * Propósito:
 * - Define las operaciones CRUD que deben ser públicas para la UI/AppMenu.
 * - Asegura que todas las entidades sigan un contrato de servicio estandarizado.
 * * NOTA: Los IDs de la entidad se manejan como Long.
 */
public interface GenericService<T> {
    
    /**
     * Inserta una nueva entidad, aplicando validaciones de negocio.
     * @param entidad Entidad a insertar
     * @throws Exception Si la validación falla o hay error transaccional/BD
     */
    void insertar(T entidad) throws Exception;
    
    /**
     * Actualiza una entidad existente, aplicando validaciones de negocio.
     * @param entidad Entidad con los datos actualizados
     * @throws Exception Si la validación falla o hay error transaccional/BD
     */
    void actualizar(T entidad) throws Exception;
    
    /**
     * Elimina lógicamente una entidad (soft delete).
     * @param id ID de la entidad a eliminar
     * @throws Exception Si el ID es inválido o hay error transaccional/BD
     */
    void eliminar(Long id) throws Exception;
    
    /**
     * Obtiene una entidad por su identificador.
     * @param id ID de la entidad
     * @return Entidad encontrada o null
     * @throws Exception Si el ID es inválido o hay error de BD
     */
    T getById(Long id) throws Exception; 
    
    /**
     * Obtiene todas las entidades activas.
     * @return Lista de entidades activas (eliminado=FALSE)
     * @throws Exception Si hay error de BD
     */
    List<T> getAll() throws Exception;
}