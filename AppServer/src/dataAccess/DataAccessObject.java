/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataAccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import logicalModel.interfaces.Signable;
import logicalModel.model.User;

/**
 *
 * @author 2dam
 */
public class DataAccessObject implements Signable {

    private Connection con;
    private PreparedStatement stmt;
    private ResultSet rs;

    public Connection openConnection() {
        // TODO Auto-generated method stub
        con = null;
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
        //signin
        final String USEREXISTS = "SELECT * FROM public.res_users WHERE login=? AND password=?";
        con = openConnection();
        stmt = null;
        rs = null;

        try {
            stmt = con.prepareStatement(USEREXISTS);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswd());
            rs = stmt.executeQuery();

            if (rs.next()) {
                boolean isActive = rs.getBoolean("active");
                if (isActive == true) {
                    user = new User();
                    user.setActive(isActive);
                    user.setCity(rs.getString("city"));
                    user.setEmail(rs.getString("email"));
                    user.setMobile(rs.getInt("mobile"));
                    user.setName(rs.getString("name"));
                    user.setPasswd(rs.getString("password"));
                    user.setStreet(rs.getString("street"));
                    user.setZip(rs.getInt("zip"));
                    return user;
                } else {
                    System.out.println("Usuario inactivo. No puede inicair sesión");
                    return null;
                }
            } else {
                System.out.println("Usuario no encontrado");
                return null;
            }

        } catch (SQLException e) {
            System.out.println("Error de SQL");
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null){
                    stmt.close();
                }
                if(con !=  null){
                    con.close();
                }
            } catch (SQLException ex){
                ex.printStackTrace();
            }
        }

    }

    @Override
    public User signUp(User user) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void closeApp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void closeSession() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
