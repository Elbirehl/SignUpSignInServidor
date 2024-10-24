/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import logicalExceptions.MaxThreadsErrorException;
import logicalExceptions.ServerErrorException;
import logicalExceptions.SignInErrorException;
import logicalExceptions.UserExistErrorException;
import logicalExceptions.UserNotActiveException;
import logicalModel.interfaces.Signable;
import logicalModel.model.User;
import serverBusinessLogic.interfaces.Closable;

/**
 * DataAccessObject class that manages database connections and implements the
 * Signable interface for user operations.
 *
 * This class handles the interactions with the PostgreSQL database, including
 * user registration and login functionality.
 *
 * @author Irati, Meylin, Olaia and Elbire
 */
public class DataAccessObject implements Signable {

    private Connection con;
    private PreparedStatement stmt;
    private ResultSet rs;
    private static final Logger logger = Logger.getLogger(DataAccessObject.class.getName());

    // Crear una instancia del pool de conexiones
    private final PoolConnections pool = new PoolConnections();

    /**
     * 
     * @return 
     */
    public Connection openConnection() {
        con = null;
        try {
            con = pool.getConnection();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al intentar abrir la BD: {0}", e.getMessage());
        }
        return con;
    }

    /**
     * ESTO VA A TENER QUE IR CON LA CLASE DE POOL
     */
    public void closeConnection() {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                // En lugar de cerrar, devolver la conexión al pool
                pool.returnConnection(con);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataAccessObject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Signs in a user to the system.
     *
     * @param user the user to sign in
     * @return the signed-in user with updated information     
     */
    //Falta el maxthreadsErrorException que supongoq ue se manejara como tan el el worker que es el que extiende de hilos??
    @Override
    public User signIn(User user) throws MaxThreadsErrorException, ServerErrorException, SignInErrorException, UserNotActiveException {
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
                    throw new UserNotActiveException("El usuario está inactivo.");
                }
            } else {
                throw new SignInErrorException("Usuario no encontrado.");
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de SQL", e);
            throw new ServerErrorException("Error al acceder al servidor.");
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

    }

    /**
     * Registers a new user in the system.
     *
     * This method first checks if a user with the given email already exists.
     * If not, it creates a new partner in the res_partner table, retrieves the
     * partner ID, and then inserts the new user in the res_users table.
     *
     * @param user the user to sign up
     * @return the registered user with updated information or null if
     * registration fails
     * @throws logicalExceptions.ServerErrorException
     * @throws logicalExceptions.UserExistErrorException
     */
    @Override
    public User signUp(User user) throws ServerErrorException, UserExistErrorException {
        final String EMAILEXISTS = "SELECT * FROM public.res_users WHERE login = ?";
        final String INSERTPARTNER = "INSERT INTO public.res_partner(company_id, name, street, zip, city, email, active, mobile) VALUES('1',  ?,  ?,  ?,  ?,  ?, ?, ?)";
        final String SELECTPARTNER = "SELECT id FROM public.res_partner WHERE email = ?";
        final String INSERTUSER = "INSERT INTO public.res_users(company_id, partner_id, active, login, password) VALUES('1', ?, ?, ?, ?)";

        con = openConnection();
        stmt = null;
        rs = null;
        try {
            // Verificar si el usuario ya existe
            stmt = con.prepareStatement(EMAILEXISTS);
            stmt.setString(1, user.getEmail());
            rs = stmt.executeQuery();
            if (rs.next()) {
                throw new UserExistErrorException("El usuario ya está registrado.");
            }

            // Insertar el nuevo partner en la tabla res_partner
            stmt = con.prepareStatement(INSERTPARTNER);
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getStreet());
            stmt.setInt(3, user.getZip());
            stmt.setString(4, user.getCity());
            stmt.setString(5, user.getEmail());
            stmt.setBoolean(6, user.isActive());
            stmt.setInt(7, user.getMobile());
            stmt.executeUpdate();

            // Obtener el ID del partner recién creado
            stmt = con.prepareStatement(SELECTPARTNER);
            stmt.setString(1, user.getEmail());
            rs = stmt.executeQuery();
            int partnerId = -1;
            if (rs.next()) {
                partnerId = rs.getInt("id");
            }

            if (partnerId == -1) {
                throw new ServerErrorException("Error al obtener el ID del partner.");
            }

            // Insertar el nuevo usuario en la tabla res_users
            stmt = con.prepareStatement(INSERTUSER);
            stmt.setInt(1, partnerId);
            stmt.setBoolean(2, user.isActive());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPasswd());
            stmt.executeUpdate();

            logger.log(Level.INFO, "Usuario registrado exitosamente.");
            return user;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al registrar el usuario", e);
            throw new ServerErrorException("Error al registrar el usuario.");
        } finally {
            // Cerrar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}