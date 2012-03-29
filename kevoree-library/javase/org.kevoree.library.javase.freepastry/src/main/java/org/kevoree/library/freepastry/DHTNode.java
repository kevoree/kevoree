/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.freepastry;

/**
 *
 * @author sunye
 */
public interface DHTNode {
    
    void put(String key ,String value);
    
    String get(String key);
    
}
