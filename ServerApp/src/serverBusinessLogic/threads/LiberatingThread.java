package serverBusinessLogic.threads;

import static java.lang.System.exit;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import serverBusinessLogic.interfaces.PoolFactory;

/**
 * Thread to liberate resources when no active threads are present.
 */
public class LiberatingThread extends Thread {

    private static final Logger logger = Logger.getLogger(LiberatingThread.class.getName());
    private Scanner scanner = new Scanner(System.in);

    /**
     * Constructor de LiberatingThread.
     */
    public LiberatingThread() {
        // Constructor vacío
    }

    @Override
    public void run() {
        String input;
        logger.info("Server is currently running. If you want to finish it write: exit");
        
        do {
            input = scanner.nextLine();
        } while (!input.equalsIgnoreCase("exit"));

        if ("exit".equalsIgnoreCase(input)) {
            //if (Worker.contarHilosActivos() == 0) {
                try {
                    // No hay hilos activos
                    PoolFactory.getPool().close();
                    logger.info("Cerrando el servidor...");
                    exit(0); //Una vez liberados los recursos cierra el sistema
                } catch (Exception ex) {
                    Logger.getLogger(LiberatingThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            //}
        } else {
            System.out.println("No funciona");

            // Pausa breve para evitar un bucle apretado
            try {
                Thread.sleep(100); // Esperar un poco antes de volver a comprobar
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
                logger.log(Level.WARNING, "El hilo liberador fue interrumpido", e);
            }
        }
    }
}
