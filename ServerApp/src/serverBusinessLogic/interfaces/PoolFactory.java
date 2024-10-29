/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverBusinessLogic.interfaces;

import dataAccess.PoolConnections;

/**
 *
 * @author 2dam
 */
public class PoolFactory {
    private static PoolConnections pool;

    public PoolFactory() {
    }
    
    public static synchronized Closable getPool(){
        if (pool == null){
            pool = new PoolConnections();
        }
        return pool;
    }
}
