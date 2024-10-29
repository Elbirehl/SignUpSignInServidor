/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataAccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import serverBusinessLogic.interfaces.Closable;

/**
 * Class for managing a pool of database connections. Irati cariño comenta el
 * codigo cuando tengas tiempo libre Author: Irati
 */
public class PoolConnections implements Closable {

    private final String databaseUrl;
    private final String database;
    private final String password;
    private final int maxPoolSize;
    private int connNum = 0;
    private final String sqlVerifyConn;
    private static final Logger logger = Logger.getLogger(PoolConnections.class.getName());

    private static final String CONFIGDATA = "config.config";

    private final Stack<Connection> freePool;
    private final Stack<Connection> occupiedPool; 

    /**
     * Constructor that initializes the connection pool.
     */
    public PoolConnections() {
        this.freePool= new Stack<>();
        this.occupiedPool= new Stack<>();
        ResourceBundle resourceBundle = ResourceBundle.getBundle(CONFIGDATA);
        this.databaseUrl = resourceBundle.getString("URL");
        this.database = resourceBundle.getString("db_user");
        this.password = resourceBundle.getString("db_password");
        this.maxPoolSize = Integer.parseInt(resourceBundle.getString("maxConnections"));
        this.sqlVerifyConn = resourceBundle.getString("sqlVerifyConn"); // Cargar la consulta SQL
    }

    public Stack<Connection> getOccupiedPool() {
        return occupiedPool;
    }

    /**
     * Get an available connection from the pool.
     *
     * @return An available connection
     * @throws SQLException Fail to get an available connection
     */
    public synchronized Connection getConnection() throws SQLException {
        Connection conn;

        // Check if the pool is full
        if (freePool.isEmpty() && connNum >= maxPoolSize) {
            throw new SQLException("The connection pool is full.");
        }

        // Get an available connection or create a new one
        if (!freePool.isEmpty()) {
            conn = freePool.pop();
        } else {
            conn = DriverManager.getConnection(databaseUrl, database, password);
            connNum++;
        }
        occupiedPool.add(conn);

        // Ensure the connection is active
        if (!isConnectionActive(conn)) {
            occupiedPool.remove(conn);
            connNum--;
            conn.close();
            conn = DriverManager.getConnection(databaseUrl, database, password);
        }
        return conn;
    }

    //Se utiliza para verificar que una conexión de la base de datos esté activa y funcionando correctamente antes de entregarla a una solicitud. 
    //Es un mecanismo de validación para asegurarse de que las conexiones que están inactivas en el pool no se hayan desconectado o dañado debido a razones externas, como un tiempo de espera prolongado o problemas de red.
    private boolean isConnectionActive(Connection conn) {
        try (Statement st = conn.createStatement()) {
            st.executeQuery(sqlVerifyConn);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public int getActiveConnections() {
        return occupiedPool.size();
    }

    /**
     * Return a connection to the pool.
     *
     * @param conn The connection
     * @throws SQLException When the connection is returned already or it isn't
     * gotten from the pool.
     */
    public synchronized void returnConnection(Connection conn) throws SQLException {
        if (conn == null) {
            throw new NullPointerException("Connection cannot be null.");
        }
        if (conn != null) {
            freePool.push(conn);
            logger.info("Returning the connection");

        }
    }

    @Override
    public void close() throws Exception {
        // Cerrar todas las conexiones ocupadas
        for (Connection conn : occupiedPool) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, null, e);
            }
        }
        occupiedPool.clear();

        // Cerrar todas las conexiones libres
        while (!freePool.isEmpty()) {
            Connection conn = freePool.pop();
            try {
                conn.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, null, e);
            }
        }
        connNum = 0; // Restablecer el número de conexiones activas
    }
}
