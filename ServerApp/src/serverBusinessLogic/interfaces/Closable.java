package serverBusinessLogic.interfaces;

/**
 * Interface defining a closeable resource with a single method to close
 * resources and release associated resources or connections.
 *
 * Implementing classes should ensure that the close method is called to avoid
 * resource leaks.
 *
 *
 * @author olaia
 */
public interface Closable {

    /**
     * Closes this resource, releasing any underlying resources or connections
     * associated with it.
     *
     * @throws Exception if an error occurs while attempting to close the
     * resource
     */
    public void close() throws Exception;

}
