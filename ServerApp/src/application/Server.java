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
 *
 * @author 2dam
 */
public class Server {

    private ServerSocket serverSocket;
    private static final ResourceBundle configFile = ResourceBundle.getBundle("config.config");
    private static final int maxConnections = Integer.parseInt(configFile.getString("maxConnections"));
    private static int connections = 0;
    private static final Logger logger = Logger.getLogger(Worker.class.getName());

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
        closeWorker(); //ñie
    }

    public void startServer() {
        try {

            int port = Integer.parseInt(configFile.getString("PORT"));
            serverSocket = new ServerSocket(port);
            System.out.println("Servidor iniciado en el puerto " + port);

            // Calls the method waitClose that creates a thread that is in charge of clossing the server.
            waitClose();

            while (true) {
                // Espera una conexión de cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde: " + clientSocket.getInetAddress());
                
                // Crear y lanzar un nuevo Worker para el cliente
                if (connections < Integer.parseInt(ResourceBundle.getBundle("config.config").getString("maxConnections"))) {
                    System.out.println("Numero connections" + connections + " Numero maxconncecion" + Integer.parseInt(ResourceBundle.getBundle("config.config").getString("maxConnections")));
                    Worker worker = new Worker(clientSocket);
                    worker.start();
                    connections++;
                } else {
                    try {
                        // Gets an ObjectOutputStream to write.
                        ObjectOutputStream write = new ObjectOutputStream(clientSocket.getOutputStream());
                        // Creates a responde for the client.
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

    public synchronized static void closeWorker() {
        logger.info("Closing the connection.");
        // Decrease the connections' counter
        connections--;
    }

    public static void waitClose() {
        logger.info("Initializing the thread that waits for closing the server");
        LiberatingThread close = new LiberatingThread();
        close.start();
    }
}
