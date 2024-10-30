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
        //Empty constructor
    }

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
