/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverBusinessLogic.threads;

import dataAccess.DataAccessObject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Olaia
 */
public class LiberatingThread extends Thread{

    private final List<DataAccessObject> resources;
    private static final Logger logger = Logger.getLogger(LiberatingThread.class.getName());

    /**
     * Constructor de LiberatingThread que acepta una lista de recursos a cerrar.
     *
     * @param resources Los recursos que deben ser cerrados (DataAccessObject).
     */
    public LiberatingThread(List<DataAccessObject> resources) {
        this.resources = resources;
    }

    @Override
    public void run() {
        try {
            for (DataAccessObject resource : resources) {
                resource.closeConnection(); // Cerrar la conexión de cada recurso
            }
            logger.info("Recursos liberados correctamente.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al liberar recursos", e);
        }
    }
}
