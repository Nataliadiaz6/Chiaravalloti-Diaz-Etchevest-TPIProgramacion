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
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class MenuDisplay {
     private final Scanner scanner = new Scanner(System.in);
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // IMPRIME LAS OPCIONES DEL MENÚ
    public void mostrarMenuPrincipal() {
        System.out.println("\n===== MENU PRINCIPAL =====");
        System.out.println("1 - Crear Pedido");
        System.out.println("2 - Crear Envio para Pedido");
        System.out.println("3 - Crear Pedido + Envio");
        System.out.println("4 - Listar Pedidos");
        System.out.println("5 - Buscar Pedido por ID");
        System.out.println("6 - Buscar Pedido por Numero");
        System.out.println("7 - Actualizar Pedido");
        System.out.println("8 - Actualizar Envio");
        System.out.println("9 - Eliminar Pedido");
        System.out.println("10 - Listar Envios");
        System.out.println("0 - Salir");
        System.out.print("Opcion: ");
    }

    public String leerOpcion() { return scanner.nextLine().trim(); }

    // LEE UN STRING NORMAL
    public String leerString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    // LEE UN LONG VALIDADO
    public Long leerLong(String prompt) {
        System.out.print(prompt);
        try { return Long.parseLong(scanner.nextLine().trim()); }
        catch (Exception e) {
            System.out.println("Numero invalido.");
            return null;
        }
    }

    // LEE UN DOUBLE VALIDADO
    public Double leerDouble(String prompt) {
        System.out.print(prompt);
        try { return Double.parseDouble(scanner.nextLine().trim()); }
        catch (Exception e) {
            System.out.println("Numero invalido.");
            return null;
        }
    }

    // LEE UNA FECHA — valida formato
    public LocalDate leerFecha(String prompt) {
        System.out.print(prompt);
        try { return LocalDate.parse(scanner.nextLine().trim(), df); }
        catch (Exception e) {
            System.out.println("Fecha invalida (formato yyyy-MM-dd).");
            return null;
        }
    }

    public void mostrarMensaje(String msg) { System.out.println(msg); }

    public boolean confirmar(String prompt) {
        System.out.print(prompt + " (S/N): ");
        return scanner.nextLine().trim().equalsIgnoreCase("S");
    }

    public void cerrar() { scanner.close(); }
    
}
