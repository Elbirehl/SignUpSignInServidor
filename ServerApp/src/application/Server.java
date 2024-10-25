package application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.Scanner;
import serverBusinessLogic.threads.Worker;

/**
 *
 * @author 2dam
 */
public class Server {

    private ServerSocket serverSocket; // Mover serverSocket a nivel de clase

    public static void main(String[] args) {

        Server server = new Server();
        server.startServer();
    }

    public void startServer() {
        // ServerSocket serverSocket = null;

        try {
            ResourceBundle configFile = ResourceBundle.getBundle("config.config");
            int port = Integer.parseInt(configFile.getString("PORT"));
            serverSocket = new ServerSocket(port);

            System.out.println("Servidor iniciado en el puerto " + port);

            // Hilo para leer teclado
            Thread readKeyboardThread = new Thread(this::readKeyboard);
            readKeyboardThread.start();

            do {
                // Espera una conexión de cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde: " + clientSocket.getInetAddress());

                // Crear y lanzar un nuevo Worker para el cliente
                Worker worker = new Worker(clientSocket);
                worker.start();

                //bucle tecla 
            } while (true); //usar lo del key = esc
            //Llamar el hilo liberador que tiene lo del contador de hilos = a 0
        } catch (IOException e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
        } finally {
            stopServer();
        }
    }

    private void readKeyboard() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if ("ESC".equalsIgnoreCase(input)) {
                stopServer();
                break; // Salir del bucle al cerrar el servidor
            }
        }
    }

    private void stopServer() {
        try {
            // Si el serverSocket no es nulo, cierra la conexión del servidor
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("ServerSocket cerrado.");
            }
        } catch (IOException e) {
            System.out.println("Error al cerrar el ServerSocket: " + e.getMessage());
        }

        System.out.println("Servidor cerrado.");
        System.exit(0); // Cerrar la aplicación
    }

}
