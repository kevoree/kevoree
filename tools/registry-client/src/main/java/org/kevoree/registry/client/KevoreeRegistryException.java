package org.kevoree.registry.client;

/**
 *
 * Created by leiko on 8/7/17.
 */
public class KevoreeRegistryException extends Exception {

    KevoreeRegistryException(String message) {
        super(message);
    }

    KevoreeRegistryException(String message, Exception e) {
        super(message, e);
    }
}
