/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main;

/**
 *
 * @author Jorgelina
 */
public class Main {
     public static void main(String[] args) {

        // Probar conexión para saber que la BD responde
        TestConexion.test();

        // Inicio la aplicación y el Menú principal
        AppMenu app = new AppMenu();
        app.run();
    }
    
}
