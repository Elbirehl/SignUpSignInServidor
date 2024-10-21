/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import dataAccess.DataAccessObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import logicalModel.model.User;
/**
 *
 * @author 2dam
 */
public class ServerApplication {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Crear una instancia de DataAccessObject
        DataAccessObject dao = new DataAccessObject();
        // Crear un usuario de prueba
        User user = new User(
            "testuser@example.com", // Email
            "password123",          // Contraseña
            "Test User",            // Nombre
            "123 Test St",          // Calle
            123456789,              // Móvil
            "Test City",            // Ciudad
            12345,                  // Código postal
            true                    // Activo
        );
        
        try {
            // Intentar registrar el usuario
            User registeredUser = dao.signUp(user);
            System.out.println("Usuario registrado con éxito: " + registeredUser.getEmail());
        } catch (Exception e) {
            // Capturar si hay algún error, por ejemplo, si el email ya existe
            System.out.println("Error al registrar el usuario: " + e.getMessage());
        } finally {
            // Cerrar la conexión y el PreparedStatement
            dao.closeConnection();
            System.out.println("Conexión cerrada con éxito.");
        }
    }
    
}
