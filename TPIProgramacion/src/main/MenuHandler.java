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
    // VALIDADORES GENERALES
    // ============================================================

    private String leerStringObligatorio(String prompt) {
        while (true) {
            String s = display.leerString(prompt);
            if (s != null && !s.trim().isEmpty()) return s;
            display.mostrarMensaje("El valor no puede estar vacio.");
        }
    }

    private String leerStringOpcional(String prompt) {
        while (true) {
            String s = display.leerString(prompt);
            if (s.trim().isEmpty()) return ""; 
            return s;
        }
    }

    private Double leerDoublePositivo(String prompt) {
        while (true) {
            Double d = display.leerDouble(prompt);
            if (d != null && d > 0) return d;
            display.mostrarMensaje("Debe ingresar un numero valido mayor a 0.");
        }
    }

    private Double leerDoubleOpcional(String prompt) {
        while (true) {
            String input = display.leerString(prompt);
            if (input.trim().isEmpty()) return null;
            try {
                return Double.parseDouble(input);
            } catch (Exception e) {
                display.mostrarMensaje("Numero invalido.");
            }
        }
    }

    private Long leerLongObligatorio(String prompt) {
        while (true) {
            Long l = display.leerLong(prompt);
            if (l != null) return l;
            display.mostrarMensaje("Debe ingresar un numero valido.");
        }
    }

    private LocalDate leerFechaObligatoria(String prompt) {
        while (true) {
            LocalDate f = display.leerFecha(prompt);
            if (f != null) return f;
            display.mostrarMensaje("Debe ingresar una fecha valida.");
        }
    }

    // ============================================================
    // MENÚ NUMÉRICO PARA ENUMS
    // ============================================================

    private <T extends Enum<T>> T seleccionarEnum(Class<T> enumClass, String prompt) {
        T[] valores = enumClass.getEnumConstants();

        display.mostrarMensaje(prompt);
        for (int i = 0; i < valores.length; i++) {
            System.out.println((i + 1) + ") " + valores[i]);
        }

        while (true) {
            Long opcion = leerLongObligatorio("Elija opcion: ");

            if (opcion >= 1 && opcion <= valores.length) {
                return valores[opcion.intValue() - 1];
            }

            display.mostrarMensaje("Opcion invalida.");
        }
    }

    // Versión opcional (permite Enter)
    private <T extends Enum<T>> T seleccionarEnumOpcional(Class<T> enumClass, String prompt) {
        T[] valores = enumClass.getEnumConstants();

        display.mostrarMensaje(prompt + " (Enter para omitir)");
        for (int i = 0; i < valores.length; i++) {
            System.out.println((i + 1) + ") " + valores[i]);
        }

        while (true) {
            String input = display.leerString("Opcion: ");
            if (input.trim().isEmpty()) return null;

            try {
                int idx = Integer.parseInt(input);
                if (idx >= 1 && idx <= valores.length) {
                    return valores[idx - 1];
                }
            } catch (Exception e) {
                display.mostrarMensaje("Ingrese un numero o Enter para omitir.");
            }
        }
    }

    // ============================================================
    // 1) CREAR PEDIDO
    // ============================================================
    public void crearPedidoSimple() {

        String numero = leerStringObligatorio("Numero: ");
        LocalDate fecha = leerFechaObligatoria("Fecha (yyyy-MM-dd): ");
        String cliente = leerStringObligatorio("Cliente: ");
        Double total = leerDoublePositivo("Total: ");

        Pedido p = new Pedido(null, false, numero, fecha, cliente, total, EstadoPedido.NUEVO, null);

        try {
            pedidoService.insertar(p);
            display.mostrarMensaje("Pedido creado.");
        } catch (Exception e) {
            display.mostrarMensaje("Error: " + e.getMessage());
        }
    }

    // ============================================================
    // 2) CREAR ENVÍO PARA PEDIDO
    // ============================================================
    public void crearEnvioParaPedido() {

        Long id = leerLongObligatorio("ID del pedido: ");

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

        try {
            envioService.insertar(envio);
            p.setEnvio(envio);
            pedidoService.actualizar(p);
            display.mostrarMensaje("Envio creado y asociado.");
        } catch (Exception e) {
            display.mostrarMensaje("Error: " + e.getMessage());
        }
    }

    // ============================================================
    // 3) CREAR PEDIDO + ENVÍO
    // ============================================================
    public void crearPedidoConEnvio() {

        String numero = leerStringObligatorio("Numero: ");
        LocalDate fecha = leerFechaObligatoria("Fecha (yyyy-MM-dd): ");
        String cliente = leerStringObligatorio("Cliente: ");
        Double total = leerDoublePositivo("Total: ");

        boolean quiereEnvio = display.confirmar("¿Crear tambien el envio?");
        Envio envio = null;

        if (quiereEnvio) {
            envio = leerEnvio();
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
        Long id = leerLongObligatorio("ID: ");
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
        String numero = leerStringObligatorio("Numero: ");
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

        Long id = leerLongObligatorio("ID del pedido: ");

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

        String nuevoNumero = leerStringOpcional("Nuevo numero (enter para omitir): ");
        if (!nuevoNumero.isEmpty()) p.setNumero(nuevoNumero);

        String nuevoCliente = leerStringOpcional("Nuevo cliente (enter para omitir): ");
        if (!nuevoCliente.isEmpty()) p.setClienteNombre(nuevoCliente);

        Double nuevoTotal = leerDoubleOpcional("Nuevo total (enter para omitir): ");
        if (nuevoTotal != null) p.setTotal(nuevoTotal);

        EstadoPedido nuevoEstado = seleccionarEnumOpcional(
                EstadoPedido.class,
                "Seleccione nuevo estado"
        );
        if (nuevoEstado != null) p.setEstado(nuevoEstado);

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

        Long id = leerLongObligatorio("ID del envio: ");

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

        String tr = leerStringOpcional("Tracking (enter para omitir): ");
        if (!tr.isEmpty()) e.setTracking(tr);

        Double costo = leerDoubleOpcional("Nuevo costo (enter para omitir): ");
        if (costo != null) e.setCosto(costo);

        EstadoEnvio nuevoEstado = seleccionarEnumOpcional(
                EstadoEnvio.class,
                "Seleccione nuevo estado"
        );
        if (nuevoEstado != null) e.setEstado(nuevoEstado);

        try {
            envioService.actualizar(e);
            display.mostrarMensaje("Envio actualizado.");
        } catch (Exception ex) {
            display.mostrarMensaje("Error: " + ex.getMessage());
        }
    }

    // ============================================================
    // 9) ELIMINAR PEDIDO
    // ============================================================
    public void eliminarPedido() {

        Long id = leerLongObligatorio("ID del pedido: ");

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
                display.mostrarMensaje("No hay envios.");
                return;
            }
            lista.forEach(System.out::println);
        } catch (Exception e) {
            display.mostrarMensaje("Error: " + e.getMessage());
        }
    }

    // ============================================================
    // ARMAR ENVÍO COMPLETO CON VALIDACIONES NUMÉRICAS
    // ============================================================
    private Envio leerEnvio() {

        String tracking = leerStringObligatorio("Tracking: ");
        EmpresaEnvio empresa = seleccionarEnum(EmpresaEnvio.class, "Seleccione empresa de envío:");
        TipoEnvio tipo = seleccionarEnum(TipoEnvio.class, "Seleccione tipo de envío:");
        Double costo = leerDoublePositivo("Costo: ");
        LocalDate fd = leerFechaObligatoria("Fecha despacho: ");
        LocalDate fe = leerFechaObligatoria("Fecha estimada: ");

        return new Envio(null, false, tracking, empresa, tipo, costo, fd, fe, EstadoEnvio.EN_PREPARACION);
    }
}