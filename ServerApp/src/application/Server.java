package application;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import logicalModel.message.Message;
import logicalModel.message.MessageType;
import serverBusinessLogic.threads.LiberatingThread;
import serverBusinessLogic.threads.Worker;

/**
 * Represents a server application that listens for incoming client connections,
 * manages worker threads to handle each connection, and enforces a maximum
 * number of simultaneous connections. If the maximum limit is reached, the
 * server sends an error response to the client and maintains a connection
 * counter to manage active threads.
 *
 * The server configuration is specified in a config file, including settings
 * for port and maximum connections. This class also creates a dedicated thread
 * to listen for server shutdown requests.
 *
 * @author Irati, Elbire, Meylin and Olaia
 */
public class Server {

    private ServerSocket serverSocket;
    private static final ResourceBundle configFile = ResourceBundle.getBundle("config.config");
    private static final int maxConnections = Integer.parseInt(ResourceBundle.getBundle("config.config").getString("maxConnections"));
    private static int connections = 0;
    private static final Logger logger = Logger.getLogger(Worker.class.getName());

    /**
     * Main method to launch the server application.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }

    /**
     * Starts the server by initializing a ServerSocket to listen on the
     * configured port. Accepts incoming client connections and starts a new
     * Worker thread for each connection, provided the maximum number of
     * connections has not been reached. If the limit is exceeded, an error
     * message is sent to the client.
     */
    public void startServer() {
        try {

            int port = Integer.parseInt(configFile.getString("PORT"));
            serverSocket = new ServerSocket(port);
            logger.info("Server started on port " + port);

            // Calls the method waitClose that creates a thread that is in charge of clossing the server.
            waitClose();

            while (true) {
                // Waits for a client connection
                Socket clientSocket = serverSocket.accept();
                logger.info("Client conected from: " + clientSocket.getInetAddress());

                // Create and throw a new Worker for the Client 
                if (connections < maxConnections) {
                    Worker worker = new Worker(clientSocket);
                    //Just in case a Worker can't be created 
                    if (worker != null) {
                        worker.start();
                        openWorker();
                    } else {
                        try {
                            // Gets an ObjectOutputStream to write.
                            ObjectOutputStream write = new ObjectOutputStream(clientSocket.getOutputStream());
                            // Creates a responde for the client.
                            Message response = new Message(null, MessageType.SERVER_ERROR);
                            // Sends the response to the client.
                            write.writeObject(response);
                        } catch (NullPointerException e) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "Worker is null", e);
                        }

                    }
                } else {
                    try {
                        // Gets an ObjectOutputStream to write.
                        ObjectOutputStream write = new ObjectOutputStream(clientSocket.getOutputStream());
                        // Creates a response for the client.
                        Message response = new Message(null, MessageType.MAX_THREADS_ERROR);
                        // Sends the response to the client.
                        write.writeObject(response);
                    } catch (IOException ex1) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex1);
                    }

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Increments the active connections count. This method is synchronized to
     * ensure thread safety when modifying the connection counter. Logs an
     * informational message indicating that a new connection has been opened.
     */
    public synchronized static void openWorker() {
        logger.info("Opening the connection.");
        // Increase the connections' counter
        connections++;
    }

    /**
     * Decreases the active connections count. This method is synchronized to
     * ensure thread safety when modifying the connection counter.
     */
      public synchronized static void openWorker() {
        logger.info("Opening the connection.");
        // Decrease the connections' counter
        connections++;
    }
      
    public synchronized static void closeWorker() {
        logger.info("Closing the connection.");
        // Decrease the connections' counter
        connections--;
    }

    /**
     * Starts a LiberatingThread to handle server shutdown operations. This
     * thread listens for a specific signal to gracefully close the server.
     */
    public static void waitClose() {
        logger.info("Initializing the thread to listen for server shutdown requests");
        LiberatingThread close = new LiberatingThread();
        close.start();
    }
}
