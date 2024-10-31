package serverBusinessLogic.threads;

import static java.lang.System.exit;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import serverBusinessLogic.interfaces.PoolFactory;

/**
 * Thread to liberate resources when no active threads are present.
 *
 * This thread monitors user input, waiting for a command to shut down the
 * server. When the user types "exit," it releases resources and closes the
 * server system.
 *
 * @author Olaia, Irati, Meylin and Elbire
 */
public class LiberatingThread extends Thread {

    private static final Logger logger = Logger.getLogger(LiberatingThread.class.getName());
    private Scanner scanner = new Scanner(System.in);

    /**
     * Constructor de LiberatingThread.
     *
     * The constructor is empty, as this thread does not require any initial
     * parameters.
     */
    public LiberatingThread() {
        //Empty constructor
    }

    /**
     * Starts the thread, logging that the server is running and waiting for an
     * "exit" command from the user.
     *
     * This method runs in a loop, continuously checking user input. When the
     * "exit" command is received, it attempts to release resources through
     * PoolFactory#getPool() and then terminates the server by calling exit(0).
     * If an error occurs during resource release, it logs the exception.
     */
    @Override
    public void run() {
        String input;
        logger.info("Server is currently running. If you want to finish it write: exit");

        do {
            input = scanner.nextLine();
        } while (!input.equalsIgnoreCase("exit"));

        if ("exit".equalsIgnoreCase(input)) {
            try {
                PoolFactory.getPool().close();
                logger.info("Closing server...");
                exit(0); //Once the resources are released, close the system
            } catch (Exception ex) {
                Logger.getLogger(LiberatingThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                Thread.sleep(100); // Wait before checking again
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt state
                logger.log(Level.WARNING, "The liberating thread was interrupted", e);
            }
        }
    }
}
