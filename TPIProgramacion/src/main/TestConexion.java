/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main;

/**
 *
 * @author Jorgelina
 */


import java.sql.Connection;
import config.DatabaseConnection;

public class TestConexion {
    public static void test() {
        System.out.println("Probando conexion a la base...");

        try (Connection conn = DatabaseConnection.getConnection()) {

            if (conn != null && !conn.isClosed()) {
                System.out.println("Conexion OK.");
            } else {
                System.out.println("No se pudo establecer conexión.");
            }

        } catch (Exception e) {
            System.out.println("ERROR de conexion: " + e.getMessage());
        }
    }
    
}
