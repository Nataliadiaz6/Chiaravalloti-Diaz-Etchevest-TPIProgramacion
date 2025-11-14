/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Natalia
 */
/**
 * Clase utilitaria para gestionar conexiones a la base de datos MySQL.
 * * Patrón: Factory con configuración estática
 * - No se puede instanciar (constructor privado)
 * - Proporciona conexiones mediante método estático getConnection()
 */
public final class DatabaseConnection {
    /** URL de conexión JDBC. */
    // Mantengo tpi como nombre de la BD de ejemplo
    private static final String URL = System.getProperty("db.url", "jdbc:mysql://localhost:3306/tpi");

    /** Usuario de la base de datos. */
    private static final String USER = System.getProperty("db.user", "root");

    /** Contraseña del usuario.*/
    private static final String PASSWORD = System.getProperty("db.password", "");

    /**
     * Bloque de inicialización estática.
     * Se ejecuta UNA SOLA VEZ cuando la clase es cargada.
     * * Acciones:
     * 1. Carga el driver JDBC de MySQL.
     * 2. Valida que la configuración sea correcta (fail-fast).
     */
    static {
        try {
            // Carga explícita del driver para garantizar su disponibilidad
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Valida configuración primero
            validateConfiguration();
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Error: No se encontró el driver JDBC de MySQL. Asegúrese de que el JAR esté en el classpath. Mensaje: " + e.getMessage());
        } catch (IllegalStateException e) {
            throw new ExceptionInInitializerError("Error en la configuración de la base de datos: " + e.getMessage());
        }
    }

    /**
     * Constructor privado para prevenir la instanciación de esta clase utilitaria.
     */
    private DatabaseConnection() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria y no debe ser instanciada");
    }

    /**
     * Obtiene una nueva conexión a la base de datos.
     * - Cada llamada crea una NUEVA conexión.
     * - El DAO/Service es responsable de cerrar esta conexión (usar try-with-resources).
     * * @return Conexión JDBC activa
     * @throws SQLException Si no se puede establecer la conexión (credenciales o URL incorrectas)
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Valida que los parámetros de conexión estáticos sean válidos.
     * * @throws IllegalStateException Si la configuración es inválida
     */
    private static void validateConfiguration() {
        if (URL == null || URL.trim().isEmpty()) {
            throw new IllegalStateException("La URL de la base de datos no está configurada.");
        }
        if (USER == null || USER.trim().isEmpty()) {
            throw new IllegalStateException("El usuario de la base de datos no está configurado.");
        }
        // PASSWORD puede ser vacío, solo validamos que no sea null
        if (PASSWORD == null) {
            throw new IllegalStateException("La contraseña de la base de datos no está configurada.");
        }
    }
}
