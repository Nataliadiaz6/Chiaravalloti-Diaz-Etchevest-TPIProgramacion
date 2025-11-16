/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main;

/**
 *
 * @author Jorgelina
 */

import dao.EnvioDAO;
import dao.PedidoDAO;
import sevice.PedidoServiceImpl;
import sevice.EnvioServiceImpl;

public class AppMenu {

    private final MenuDisplay display;
    private final MenuHandler handler;

    public AppMenu() {

        // DAOs instanciados correctamente
        EnvioDAO envioDAO = new EnvioDAO();
        PedidoDAO pedidoDAO = new PedidoDAO(envioDAO);

        // Services instanciados correctamente
        EnvioServiceImpl envioService = new EnvioServiceImpl(envioDAO);
        PedidoServiceImpl pedidoService = new PedidoServiceImpl(pedidoDAO, envioService);

        // Display y Handler
        this.display = new MenuDisplay();
        this.handler = new MenuHandler(pedidoService, envioService, display);
    }

    public void run() {
        boolean salir = false;

        while (!salir) {
            display.mostrarMenuPrincipal();
            String opcion = display.leerOpcion();

            switch (opcion) {
                case "1": handler.crearPedidoSimple(); break;
                case "2": handler.crearEnvioParaPedido(); break;
                case "3": handler.crearPedidoConEnvio(); break;
                case "4": handler.listarPedidos(); break;
                case "5": handler.buscarPedidoPorId(); break;
                case "6": handler.buscarPedidoPorNumero(); break;
                case "7": handler.actualizarPedido(); break;
                case "8": handler.actualizarEnvio(); break;
                case "9": handler.eliminarPedido(); break;
                case "10": handler.listarEnvios(); break;
                case "0": salir = true; break;
                default:
                    display.mostrarMensaje("Opción invalida.");
            }
        }

        display.mostrarMensaje("Hasta luego.");
        display.cerrar();
    }
}