/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataAccess;

import logicalModel.interfaces.Signable;

/**
 * * Factory class for creating instances of classes implementing the Signable
 * interface.
 *
 * This factory encapsulates the instantiation of the DataAccessObject class and
 * provides a single point of access for obtaining Signable instances.
 *
 * Using a factory pattern here allows for easy changes in the implementation of
 * the Signable interface if needed in the future.
 *
 * @author Elbire
 */
public class ServerFactory {

    /**
     * Provides an instance of a class that implements the Signable interface.
     *
     * @return a new instance of DataAccessObject, which implements the Signable
     * interface.
     */
    public static Signable getSignable() {
        return new DataAccessObject();
    }
}
