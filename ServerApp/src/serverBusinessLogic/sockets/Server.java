package serverBusinessLogic.sockets;

import dataAccess.DataAccessObject;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import serverBusinessLogic.threads.Worker;

/**
 *
 * @author 2dam
 */
public class Server {

    public void startServer() {
        ServerSocket serverSocket = null;

        try {
            ResourceBundle configFile = ResourceBundle.getBundle("config.config");
            int port = Integer.parseInt(configFile.getString("PORT"));
            serverSocket = new ServerSocket(port);

            System.out.println("Servidor iniciado en el puerto " + port);

            while (true) {
                // Espera una conexión de cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde: " + clientSocket.getInetAddress());

                // Crear y lanzar un nuevo Worker para el cliente
                Worker worker = new Worker(clientSocket);
                worker.start();
            }
        } catch (IOException e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.out.println("Error al cerrar el servidor: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }
}
