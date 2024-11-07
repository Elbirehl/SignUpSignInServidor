package serverBusinessLogic.interfaces;

import dataAccess.PoolConnections;

/**
 * PoolFactory is a factory class responsible for providing a singleton instance
 * of PoolConnections. This ensures that there is a single pool of
 * database connections used throughout the application, which can be accessed
 * via the getPool() method.
 *
 * @author Irati
 */
public class PoolFactory {

    private static PoolConnections pool;

    /**
     * Constructs a new PoolFactory. This constructor is empty as the class is
     * mainly used for its static methods and does not need to be instantiated.
     */
    public PoolFactory() {
    }

    /**
     * Returns a synchronized instance of {@code PoolConnections}. If no
     * instance exists, it creates a new one and returns it; otherwise, it
     * returns the existing instance. This method ensures thread safety for
     * accessing the connection pool.
     *
     * @return a PoolConnection
     */
    public static synchronized Closable getPool() {
        if (pool == null) {
            pool = new PoolConnections();
        }
        return pool;
    }
}
