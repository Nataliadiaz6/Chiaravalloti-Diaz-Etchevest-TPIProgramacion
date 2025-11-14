/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sevice;

import dao.EnvioDAO;
import dao.GenericDAO;
import models.Envio;
import models.EmpresaEnvio;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementación del servicio de negocio para la entidad Envío.
 * (Clase B en la relación Pedido -> Envío)
 * * Responsabilidades:
 * - Validar que los datos del Envío sean correctos (tracking, fechas, Enums).
 * - Aplicar reglas de negocio simples sobre Envío (ej: tracking obligatorio).
 * - Delegar operaciones de persistencia al EnvíoDAO.
 * - NO maneja transacciones complejas, ya que PedidoService orquestará la creación de Envío/Pedido.
 * * Patrón: Service Layer con inyección de dependencias
 */
public class EnvioServiceImpl implements GenericService<Envio> {
    
    /** DAO para acceso a datos de envíos. Inyectado en el constructor. */
    private final EnvioDAO envioDAO;

    /**
     * Constructor con inyección de dependencias.
     * @param envioDAO DAO de envíos
     * @throws IllegalArgumentException si envioDAO es null
     */
    public EnvioServiceImpl(EnvioDAO envioDAO) {
        if (envioDAO == null) {
            throw new IllegalArgumentException("EnvioDAO no puede ser null");
        }
        this.envioDAO = envioDAO;
    }

    /**
     * Inserta un nuevo envío.
     * * Flujo:
     * 1. Valida el objeto Envío.
     * 2. Delega al DAO para insertar.
     * * NOTA: Esta versión sin transacción será usada por PedidoService.insertarTx()
     * para obtener el ID de Envío dentro de la transacción.
     * * @param envio Envío a insertar
     * @throws Exception Si la validación falla, el tracking está duplicado o hay error de BD
     */
    @Override
    public void insertar(Envio envio) throws Exception {
        validateEnvio(envio, true);
        
        // La validación de unicidad de Tracking se realiza aquí
        validateTrackingUnique(envio.getTracking(), null);
        
        envioDAO.insertar(envio);
    }

    /**
     * Actualiza un envío existente.
     * * @param envio Envío con los datos actualizados
     * @throws Exception Si la validación falla o el Envío no existe
     */
    @Override
    public void actualizar(Envio envio) throws Exception {
        validateEnvio(envio, false);
        if (envio.getId() == null || envio.getId() <= 0) {
            throw new IllegalArgumentException("El ID del Envío debe ser mayor a 0 para actualizar");
        }
        
        // Valida unicidad, excluyendo al propio Envío que se está actualizando
        validateTrackingUnique(envio.getTracking(), envio.getId());
        
        envioDAO.actualizar(envio);
    }

    /**
     * Elimina lógicamente un envío (soft delete).
     * * @param id ID del Envío a eliminar
     * @throws Exception Si id es inválido o no existe el Envío
     */
    @Override
    public void eliminar(Long id) throws Exception {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        envioDAO.eliminar(id);
    }

    @Override
    public Envio getById(Long id) throws Exception {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        return envioDAO.getById(id);
    }

    @Override
    public List<Envio> getAll() throws Exception {
        return envioDAO.getAll();
    }
    /**
     * Obtiene un Envío por su código de Tracking exacto.
     * Usado para la validación de unicidad.
     * @param tracking Código de seguimiento
     * @return Envío encontrado o null
     * @throws Exception Si hay error de BD
     */
    public Envio getByTracking(String tracking) throws Exception {
        // Implementación corregida: llama al método real del DAO
        return envioDAO.buscarPorTracking(tracking); 
    }

    /**
     * Valida que un Envío tenga datos correctos y completos.
     * * Reglas de negocio aplicadas:
     * - Tracking, Empresa, Tipo, Costo y Estado son obligatorios.
     * - Fechas no pueden ser en el pasado (se mantiene el ejemplo de validación).
     * * @param envio Envío a validar
     * @throws IllegalArgumentException Si alguna validación falla
     */
    public void validateEnvio(Envio envio, boolean isInsert) {
        if (envio == null) {
            throw new IllegalArgumentException("El Envío no puede ser null");
        }
        if (envio.getTracking() == null || envio.getTracking().trim().isEmpty()) {
            throw new IllegalArgumentException("El tracking es obligatorio");
        }
        if (envio.getEmpresa() == null) {
            throw new IllegalArgumentException("La empresa de envío es obligatoria");
        }
        if (envio.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de envío es obligatorio");
        }
        if (envio.getEstado() == null) {
            throw new IllegalArgumentException("El estado del envío es obligatorio");
        }
        if (envio.getCosto() <= 0) {
            throw new IllegalArgumentException("El costo del envío debe ser positivo");
        }
    
    // Ejemplo de validación de fecha
        if (envio.getFechaEstimada() != null && envio.getFechaEstimada().isBefore(LocalDate.now())) {
             // Aplicar regla de negocio:
             // throw new IllegalArgumentException("La fecha estimada no puede ser anterior a hoy");
        }
    }
    /**
     * Valida que el código de Tracking sea único en el sistema.
     * Implementa la regla UNIQUE para el campo tracking.
     * @param tracking Tracking a validar
     * @param envioId ID del Envío (null para INSERT, != null para UPDATE)
     * @throws IllegalArgumentException Si el tracking ya existe
     * @throws Exception Si hay error de BD al buscar
     */
    private void validateTrackingUnique(String tracking, Long envioId) throws Exception {
        // Llama al DAO para verificar la existencia
        Envio existente = envioDAO.buscarPorTracking(tracking); 
        
        if (existente != null) {
            // Verifica si el DNI existente pertenece a otra persona (no a la que estamos actualizando)
            if (envioId == null || !existente.getId().equals(envioId)) {
                throw new IllegalArgumentException("Ya existe un Envío con el tracking: " + tracking);
            }
        }
    }
}