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
<<<<<<< HEAD
 * Class for managing a pool of database connections. PoolConnections is
 * responsible for managing a pool of database connections to optimize resource
 * usage by reusing connections rather than creating and closing them
 * repeatedly. It supports a configurable maximum pool size, and tracks active
 * and available connections using stacks for free and occupied connections.
 *
 *
 * The connection pool is configured via a properties file specified in , where
 * database URL, credentials, and maximum connections can be set.
 *
 * @author Irati, Elbire, Meylin, Olaia
=======
 * PoolConnections is responsible for managing a pool of database connections to
 * optimize resource usage by reusing connections rather than creating and
 * closing them repeatedly. It supports a configurable maximum pool size, and
 * tracks active and available connections using stacks for free and occupied
 * connections.
 *
 * <p>
 * The connection pool is configured via a properties file specified in
 * {@code CONFIGDATA}, where database URL, credentials, and maximum connections
 * can be set.</p>
 *
 * @author Irati, Elbire, Meylin and Olaia
>>>>>>> 122c380ee2608f37d705cc88538466f9e4617ba8
 *
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
<<<<<<< HEAD
     * Constructor that initializes the connection pool.
=======
>>>>>>> 122c380ee2608f37d705cc88538466f9e4617ba8
     *
     * Initializes the connection pool by loading database configuration from
     * the properties file and setting up the free and occupied connection
     * stacks.
     */
    public PoolConnections() {
        this.freePool = new Stack<>();
        this.occupiedPool = new Stack<>();
        ResourceBundle resourceBundle = ResourceBundle.getBundle(CONFIGDATA);
        this.databaseUrl = resourceBundle.getString("URL");
        this.database = resourceBundle.getString("db_user");
        this.password = resourceBundle.getString("db_password");
        this.maxPoolSize = Integer.parseInt(resourceBundle.getString("maxConnections"));
        this.sqlVerifyConn = resourceBundle.getString("sqlVerifyConn");
    }

    /**
<<<<<<< HEAD
     * Get an available connection from the pool. Provides an available
     * connection from the pool. If no free connection is available, a new one
     * is created unless the pool has reached its maximum size
=======
     * Provides an available connection from the pool. If no free connection is
     * available, a new one is created unless the pool has reached its maximum
     * size
>>>>>>> 122c380ee2608f37d705cc88538466f9e4617ba8
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

    /**
     * Checks if a database connection is active by executing a verification SQL
     * statement.
     *
<<<<<<< HEAD
     * @param conn the to verify
=======
     * @param conn the {@link Connection} to verify
>>>>>>> 122c380ee2608f37d705cc88538466f9e4617ba8
     * @return true if the connection is active; false otherwise
     */
    private boolean isConnectionActive(Connection conn) {
        try (Statement st = conn.createStatement()) {
            st.executeQuery(sqlVerifyConn);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
<<<<<<< HEAD
     * Returns a connection back to the pool after it is no longer in use. *
=======
     * Returns a connection back to the pool after it is no longer in use.
>>>>>>> 122c380ee2608f37d705cc88538466f9e4617ba8
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

    /**
     * Closes all connections in the pool, both occupied and free, effectively
     * releasing all resources used by the pool.
     *
     * @throws Exception if any connection fails to close
     */
    @Override
    public void close() throws Exception {
        // Close all busy connections
        for (Connection conn : occupiedPool) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, null, e);
            }
        }
        occupiedPool.clear();

        //Close all free connections
        while (!freePool.isEmpty()) {
            Connection conn = freePool.pop();
            try {
                conn.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, null, e);
            }
        }
        connNum = 0;
    }
}
