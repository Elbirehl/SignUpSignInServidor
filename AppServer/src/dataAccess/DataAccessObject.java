/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataAccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import logicalModel.interfaces.Signable;
import logicalModel.model.User;

/**
 *
 * @author 2dam
 */
public class DataAccessObject implements Signable{ 
    
    
    public Connection openConnection() {
        // TODO Auto-generated method stub
        Connection con = null;
        try {
            String url = "jdbc:postgresql://192.168.21.44:5432/Desarrollo?serverTimezone=Europe/Madrid&useSSL=false";
            con = DriverManager.getConnection(url, "odoo", "odoo");
        } catch (SQLException e) {
            //Logger.getLogger("DBConnection").severe(e.getLocalizedMessage());
            System.out.println("Error al intentar abrir la BD: " + e.getMessage());
            System.exit(1);
        }
        return con;
    }
    
    public void closeConnection(PreparedStatement stmt, Connection con) {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataAccessObject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public User signIn(User user) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public User signUp(User user) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void closeApp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void closeSession() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
