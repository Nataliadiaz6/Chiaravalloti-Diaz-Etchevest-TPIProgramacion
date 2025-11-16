/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main;

/**
 *
 * @author Jorgelina
 */


import java.time.LocalDate;
import java.util.List;

import models.Pedido;
import models.Envio;
import models.EstadoPedido;
import models.EmpresaEnvio;
import models.TipoEnvio;
import models.EstadoEnvio;

import sevice.PedidoServiceImpl;
import sevice.EnvioServiceImpl;

public class MenuHandler {

    private final PedidoServiceImpl pedidoService;
    private final EnvioServiceImpl envioService;
    private final MenuDisplay display;

    public MenuHandler(PedidoServiceImpl pedidoService,
                       EnvioServiceImpl envioService,
                       MenuDisplay display) {

        this.pedidoService = pedidoService;
        this.envioService = envioService;
        this.display = display;
    }

    // ============================================================
    // 1) CREAR PEDIDO SIMPLE
    // ============================================================
    public void crearPedidoSimple() {

        String numero = display.leerString("Numero: ");
        LocalDate fecha = display.leerFecha("Fecha (yyyy-MM-dd): ");
        String cliente = display.leerString("Cliente: ");
        Double total = display.leerDouble("Total: ");

        if (fecha == null || total == null) {
            display.mostrarMensaje("Datos invalidos.");
            return;
        }

        Pedido p = new Pedido(null, false, numero, fecha, cliente, total, EstadoPedido.NUEVO, null);

        try {
            pedidoService.insertar(p);
            display.mostrarMensaje("Pedido creado.");
        } catch (Exception e) {
            display.mostrarMensaje("Error: " + e.getMessage());
        }
    }


    // ============================================================
    // 2) CREAR ENVÍO PARA UN PEDIDO EXISTENTE
    // ============================================================
    public void crearEnvioParaPedido() {

        Long id = display.leerLong("ID del pedido: ");
        if (id == null) return;

        Pedido p;
        try {
            p = pedidoService.getById(id);
        } catch (Exception e) {
            display.mostrarMensaje(e.getMessage());
            return;
        }

        if (p == null) {
            display.mostrarMensaje("Pedido no encontrado.");
            return;
        }

        Envio envio = leerEnvio();
        if (envio == null) return;

        try {
            envioService.insertar(envio);
            p.setEnvio(envio);
            pedidoService.actualizar(p);
            display.mostrarMensaje("Envío creado y asociado.");
        } catch (Exception e) {
            display.mostrarMensaje("Error: " + e.getMessage());
        }
    }


    // ============================================================
    // 3) CREAR PEDIDO + ENVÍO (TRANSACCIÓN REAL)
    // ============================================================
    public void crearPedidoConEnvio() {

        String numero = display.leerString("Numero: ");
        LocalDate fecha = display.leerFecha("Fecha (yyyy-MM-dd): ");
        String cliente = display.leerString("Cliente: ");
        Double total = display.leerDouble("Total: ");

        if (fecha == null || total == null) {
            display.mostrarMensaje("Datos invalidos.");
            return;
        }

        boolean quiereEnvio = display.confirmar("¿Crear tambien el envio?");
        Envio envio = null;

        if (quiereEnvio) {
            envio = leerEnvio();
            if (envio == null) {
                display.mostrarMensaje("Envio invalido. Operacion cancelada.");
                return;
            }
        }

        Pedido p = new Pedido(null, false, numero, fecha, cliente, total, EstadoPedido.NUEVO, envio);

        try {
            pedidoService.insertar(p);
            display.mostrarMensaje("Pedido creado correctamente" +
                    (envio != null ? " con envio asociado." : "."));

        } catch (Exception e) {
            display.mostrarMensaje("Error: " + e.getMessage());
        }
    }


    // ============================================================
    // 4) LISTAR PEDIDOS
    // ============================================================
    public void listarPedidos() {
        try {
            List<Pedido> lista = pedidoService.getAll();
            if (lista == null || lista.isEmpty()) {
                display.mostrarMensaje("No hay pedidos.");
                return;
            }
            lista.forEach(System.out::println);
        } catch (Exception e) {
            display.mostrarMensaje("Error: " + e.getMessage());
        }
    }


    // ============================================================
    // 5) BUSCAR PEDIDO POR ID
    // ============================================================
    public void buscarPedidoPorId() {
        Long id = display.leerLong("ID: ");
        if (id == null) return;

        try {
            Pedido p = pedidoService.getById(id);
            display.mostrarMensaje(p == null ? "No encontrado." : p.toString());
        } catch (Exception e) {
            display.mostrarMensaje(e.getMessage());
        }
    }


    // ============================================================
    // 6) BUSCAR PEDIDO POR NÚMERO
    // ============================================================
    public void buscarPedidoPorNumero() {
        String numero = display.leerString("Numero: ");
        try {
            Pedido p = pedidoService.buscarPorNumero(numero);
            display.mostrarMensaje(p == null ? "No encontrado." : p.toString());
        } catch (Exception e) {
            display.mostrarMensaje(e.getMessage());
        }
    }


    // ============================================================
    // 7) ACTUALIZAR PEDIDO
    // ============================================================
    public void actualizarPedido() {

        Long id = display.leerLong("ID del pedido: ");
        if (id == null) return;

        Pedido p;
        try {
            p = pedidoService.getById(id);
        } catch (Exception e) {
            display.mostrarMensaje(e.getMessage());
            return;
        }

        if (p == null) {
            display.mostrarMensaje("Pedido no encontrado.");
            return;
        }

        String nuevoNumero = display.leerString("Nuevo numero: ");
        if (!nuevoNumero.isEmpty()) p.setNumero(nuevoNumero);

        String nuevoCliente = display.leerString("Nuevo cliente: ");
        if (!nuevoCliente.isEmpty()) p.setClienteNombre(nuevoCliente);

        Double nuevoTotal = display.leerDouble("Nuevo total: ");
        if (nuevoTotal != null) p.setTotal(nuevoTotal);

        String nuevoEstado = display.leerString("Nuevo estado (NUEVO, PROCESADO, ENTREGADO): ");
        if (!nuevoEstado.isEmpty()) p.setEstado(EstadoPedido.valueOf(nuevoEstado));

        try {
            pedidoService.actualizar(p);
            display.mostrarMensaje("Pedido actualizado.");
        } catch (Exception e) {
            display.mostrarMensaje("Error: " + e.getMessage());
        }
    }


    // ============================================================
    // 8) ACTUALIZAR ENVÍO
    // ============================================================
    public void actualizarEnvio() {

        Long id = display.leerLong("ID del envio: ");
        if (id == null) return;

        Envio e;
        try {
            e = envioService.getById(id);
        } catch (Exception ex) {
            display.mostrarMensaje(ex.getMessage());
            return;
        }

        if (e == null) {
            display.mostrarMensaje("Envio no encontrado.");
            return;
        }

        String tr = display.leerString("Tracking: ");
        if (!tr.isEmpty()) e.setTracking(tr);

        Double costo = display.leerDouble("Nuevo costo: ");
        if (costo != null) e.setCosto(costo);

        String est = display.leerString("Nuevo estado (EN_PREPARACION, EN_CAMINO, ENTREGADO): ");
        if (!est.isEmpty()) e.setEstado(EstadoEnvio.valueOf(est));

        try {
            envioService.actualizar(e);
            display.mostrarMensaje("Envio actualizado.");
        } catch (Exception ex) {
            display.mostrarMensaje(ex.getMessage());
        }
    }


    // ============================================================
    // 9) ELIMINAR PEDIDO
    // ============================================================
    public void eliminarPedido() {

        Long id = display.leerLong("ID del pedido: ");
        if (id == null) return;

        if (!display.confirmar("¿Esta seguro?")) return;

        try {
            pedidoService.eliminar(id);
            display.mostrarMensaje("Pedido eliminado.");
        } catch (Exception e) {
            display.mostrarMensaje("Error: " + e.getMessage());
        }
    }


    // ============================================================
    // 10) LISTAR ENVÍOS
    // ============================================================
    public void listarEnvios() {
        try {
            List<Envio> lista = envioService.getAll();
            if (lista == null || lista.isEmpty()) {
                display.mostrarMensaje("No hay envíos.");
                return;
            }
            lista.forEach(System.out::println);
        } catch (Exception e) {
            display.mostrarMensaje("Error: " + e.getMessage());
        }
    }


    // ============================================================
    // MÉTODO AUXILIAR PARA ARMAR ENVÍOS
    // ============================================================
    private Envio leerEnvio() {

        String tracking = display.leerString("Tracking: ");
        EmpresaEnvio empresa = EmpresaEnvio.valueOf(display.leerString("Empresa: ").toUpperCase());
        TipoEnvio tipo = TipoEnvio.valueOf(display.leerString("Tipo: ").toUpperCase());
        Double costo = display.leerDouble("Costo: ");
        LocalDate fd = display.leerFecha("Fecha despacho: ");
        LocalDate fe = display.leerFecha("Fecha estimada: ");

        if (costo == null || fd == null || fe == null)
            return null;

        return new Envio(null, false, tracking, empresa, tipo, costo, fd, fe, EstadoEnvio.EN_PREPARACION);
    }
}