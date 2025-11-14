/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.time.LocalDate;
import java.util.Objects;

/**
* Entidad que representa un Pedido (Clase A) en el sistema.
 * Hereda de Base para obtener id (Long) y eliminado (Boolean).
 *
 * Relación con Envio:
 * - Un Pedido TIENE 0 o 1 Envío (asociación unidireccional 1 a 1)
 * - Esta clase contiene el atributo 'envio' que referencia a la Clase B.
 *
 * Tabla BD: pedidos
 * Campos: numero (UNIQUE), clienteNombre, total, estado.
 */
public class Pedido extends Base {
    
    /** Número único del pedido. NOT NULL, UNIQUE */
    private String numero;
    
    /** Fecha de creación del pedido. NOT NULL */
    private LocalDate fecha;

    /** Nombre del cliente. NOT NULL*/
    private String clienteNombre;

    /** Total del pedido. NOT NULL, tipo double */
    private double total;

    /** Estado del pedido (ej: NUEVO, FACTURADO, ENVIADO). */
    private EstadoPedido estado;

    /**
     * Envío asociado al pedido.
     * Referencia unidireccional 1-1 a la Clase B (Envio).
     * Puede ser null.
     */
    private Envio envio;

    /**
     * Constructor completo para reconstruir un Pedido desde la BD.
     */
    public Pedido(Long id, Boolean eliminado, String numero, LocalDate fecha, String clienteNombre, double total, EstadoPedido estado, Envio envio) {
        super(id, eliminado);
        this.numero = numero;
        this.fecha = fecha;
        this.clienteNombre = clienteNombre;
        this.total = total;
        this.estado = estado;
        this.envio = envio;
    }

    /** Constructor por defecto para crear un Pedido nuevo sin ID. */
    public Pedido() {
        super();
    }

    public String getNumero() {
        return numero;
    }

    /** Establece el número de pedido. Validación en Service. */
    public void setNumero(String numero) {
        this.numero = numero;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    /** Establece la fecha del pedido. */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    /** Establece el nombre del cliente. */
    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public double getTotal() {
        return total;
    }

    /** Establece el monto total. */
    public void setTotal(double total) {
        this.total = total;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    /** Establece el estado del pedido. */
    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }
    
    public Envio getEnvio() {
        return envio;
    }

    /**
     * Asocia o desasocia un Envío al Pedido.
     */
    public void setEnvio(Envio envio) {
        this.envio = envio;
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + getId() +
                ", numero='" + numero + '\'' +
                ", total=" + total +
                ", estado='" + estado + '\'' +
                ", envio=" + (envio != null ? envio.getTracking() : "N/A") +
                ", eliminado=" + getEliminado() +
                '}';
    }

    /** Compara dos pedidos por el campo 'numero' (identificador único de negocio). */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(numero, pedido.numero);
    }

    /** Hash code basado en el número de pedido. */
    @Override
    public int hashCode() {
        return Objects.hash(numero);
    }
}