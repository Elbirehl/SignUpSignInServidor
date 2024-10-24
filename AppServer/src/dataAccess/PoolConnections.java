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
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Stack;
import serverBusinessLogic.interfaces.Closable;

/**
 * Class for managing a pool of database connections.
 *
 * Author: Olaia
 */
public class PoolConnections implements Closable {

    private final String databaseUrl;
    private final String userName;
    private final String password;
    private final int maxPoolSize;
    private int connNum = 0;
    private final String sqlVerifyConn;

    private static final String CONFIGDATA = "config.config";

    private final Stack<Connection> freePool = new Stack<>();
    private final Set<Connection> occupiedPool = new HashSet<>();

    /**
     * Constructor that initializes the connection pool.
     */
    public PoolConnections() {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(CONFIGDATA);
        this.databaseUrl = resourceBundle.getString("databaseUrl");
        this.userName = resourceBundle.getString("userName");
        this.password = resourceBundle.getString("password");
        this.maxPoolSize = Integer.parseInt(resourceBundle.getString("maxPoolSize"));
        this.sqlVerifyConn = resourceBundle.getString("sqlVerifyConn"); // Cargar la consulta SQL
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
            conn = DriverManager.getConnection(databaseUrl, userName, password);
            connNum++;
        }
        occupiedPool.add(conn);

        // Ensure the connection is active
        if (!isConnectionActive(conn)) {
            occupiedPool.remove(conn);
            connNum--;
            conn.close();
            conn = DriverManager.getConnection(databaseUrl, userName, password);
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
        if (!occupiedPool.remove(conn)) {
            throw new SQLException("The connection is returned already or it isn't for this pool");
        }
        freePool.push(conn);
    }

    @Override
    public void close() throws Exception {
        // Cerrar todas las conexiones ocupadas
        for (Connection conn : occupiedPool) {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
        occupiedPool.clear();

        // Cerrar todas las conexiones libres
        while (!freePool.isEmpty()) {
            Connection conn = freePool.pop();
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
        connNum = 0; // Restablecer el número de conexiones activas
    }
}
