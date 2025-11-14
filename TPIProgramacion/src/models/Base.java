/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author Natalia
 */
/**
 * Clase base abstracta para todas las entidades del sistema.
 * Implementa el patrón de soft delete (baja lógica) mediante el campo 'eliminado'.
 *
 * Propósito:
 * - Proporcionar campos comunes a todas las entidades (id: Long, eliminado: Boolean)
 * - Implementar el patrón de herencia para evitar duplicación de código
 * - Soportar eliminación lógica en lugar de eliminación física 
 */
public abstract class Base {
    /**
     * Identificador único de la entidad.
     * Tipo Long, usado como clave primaria (PK) en la base de datos.
     * Generado automáticamente por la base de datos (AUTO_INCREMENT).
     */
    private Long id;

    /**
     * Flag de eliminación lógica (Soft Delete).
     * - true: La entidad está marcada como eliminada (ocultar en listados)
     * - false: La entidad está activa
     *
     * Esto permite mantener integridad referencial y datos históricos.
     */
    private Boolean eliminado;

    /**
     * Constructor completo con todos los campos.
     * Usado por los DAOs al reconstruir entidades desde la base de datos.
     *
     * @param id Identificador de la entidad
     * @param eliminado Estado de eliminación (baja lógica)
     */
    protected Base(Long id, Boolean eliminado) {
        this.id = id;
        this.eliminado = eliminado;
    }

    /**
     * Constructor por defecto.
     * Inicializa una entidad nueva sin ID (será asignado por la BD).
     * Por defecto, las entidades nuevas NO están eliminadas.
     */
    protected Base() {
        this.eliminado = false;
    }

    /**
     * Obtiene el ID de la entidad.
     * @return ID de la entidad, null si aún no ha sido persistida
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece el ID de la entidad.
     * Típicamente llamado por el DAO después de insertar en la BD.
     *
     * @param id Nuevo ID de la entidad
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Verifica si la entidad está marcada como eliminada.
     * @return true si está eliminada, false si está activa
     */
    public Boolean getEliminado() {
        return eliminado;
    }

    /**
     * Marca o desmarca la entidad como eliminada.
     *
     * @param eliminado true para marcar como eliminada, false para reactivar
     */
    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
    }
}