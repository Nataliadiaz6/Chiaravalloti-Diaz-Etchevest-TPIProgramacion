/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.time.LocalDate;
import java.util.Objects;
/**
 *
 * @author Natalia
 */

/**
 * Entidad que representa la información detallada del Envío (Clase B).
 * Hereda de Base para obtener id (Long) y eliminado (Boolean).
 *
 * Relación con Pedido:
 * - Un Envío pertenece a un solo Pedido (relación 1:1 inversa).
 * - La regla 1:1 unidireccional se implementa con una clave foránea ÚNICA a Pedido en la tabla 'envios' (BD).
 *
 * Tabla BD: envios
 * Campos principales: tracking, empresa, costo, fechas y estado.
 */
public class Envio extends Base {
    
    /** Tracking único del envío */
    private String tracking;

    /** Empresa de logística (ej: ANDREANI, OCA, CORREO_ARG) */
    private EmpresaEnvio empresa;

    /** Tipo de servicio de envío (ej: ESTANDAR, EXPRES)*/
    private TipoEnvio tipo;

    /** Costo del envío. */
    private double costo;

    /** Fecha real en que el envío fue despachado. */
    private LocalDate fechaDespacho;

    /** Fecha estimada de entrega. */
    private LocalDate fechaEstimada;

    /** Estado actual del envío (ej: EN_PREPARACION, EN_TRANSITO, ENTREGADO). */
    private EstadoEnvio estado;

    /**
     * Constructor completo para reconstruir un Envío desde la base de datos.
     * Usado por EnvíoDAO al mapear ResultSet.
     */
    public Envio(Long id, Boolean eliminado, String tracking, EmpresaEnvio empresa, TipoEnvio tipo, double costo, LocalDate fechaDespacho, LocalDate fechaEstimada, EstadoEnvio estado) {
        super(id, eliminado);
        this.tracking = tracking;
        this.empresa = empresa;
        this.tipo = tipo;
        this.costo = costo;
        this.fechaDespacho = fechaDespacho;
        this.fechaEstimada = fechaEstimada;
        this.estado = estado;
    }

    /**
     * Constructor por defecto.
     * Para crear un Envío nuevo antes de la persistencia.
     */
    public Envio() {
        super();
    }

    // --- Getters y Setters ---

    public String getTracking() {
        return tracking;
    }

    /** Establece el código de seguimiento. Validación en Service. */
    public void setTracking(String tracking) {
        this.tracking = tracking;
    }

    public EmpresaEnvio getEmpresa() {
        return empresa;
    }

    /** Establece la empresa de envío. */
    public void setEmpresa(EmpresaEnvio empresa) {
        this.empresa = empresa;
    }

    public TipoEnvio getTipo() {
        return tipo;
    }

    /** Establece el tipo de servicio. */
    public void setTipo(TipoEnvio tipo) {
        this.tipo = tipo;
    }

    public double getCosto() {
        return costo;
    }

    /** Establece el costo. */
    public void setCosto(double costo) {
        this.costo = costo;
    }

    public LocalDate getFechaDespacho() {
        return fechaDespacho;
    }

    /** Establece la fecha de despacho. */
    public void setFechaDespacho(LocalDate fechaDespacho) {
        this.fechaDespacho = fechaDespacho;
    }

    public LocalDate getFechaEstimada() {
        return fechaEstimada;
    }

    /** Establece la fecha estimada de llegada. */
    public void setFechaEstimada(LocalDate fechaEstimada) {
        this.fechaEstimada = fechaEstimada;
    }

    public EstadoEnvio getEstado() {
        return estado;
    }

    /** Establece el estado del envío. */
    public void setEstado(EstadoEnvio estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Envio{" +
                "id=" + getId() +
                ", tracking='" + tracking + '\'' +
                ", empresa='" + empresa + '\'' +
                ", estado='" + estado + '\'' +
                ", eliminado=" + getEliminado() +
                '}';
    }

    /** Compara dos envíos por igualdad SEMÁNTICA (tracking). */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Envio envio = (Envio) o;
        return Objects.equals(tracking, envio.tracking);
    }

    /** Calcula el hash code basado en el tracking. */
    @Override
    public int hashCode() {
        return Objects.hash(tracking);
    }
}