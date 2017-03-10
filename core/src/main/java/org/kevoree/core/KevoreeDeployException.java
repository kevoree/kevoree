package org.kevoree.core;

/**
 *
 * Created by leiko on 2/28/17.
 */
public class KevoreeDeployException extends Exception {

    public KevoreeDeployException(Throwable e) {
        super(e);
    }

    public KevoreeDeployException(Exception e) {
        super(e);
    }

    public KevoreeDeployException(String msg) {
        super(msg);
    }

    public KevoreeDeployException(String msg, Exception e) {
        super(msg, e);
    }
}
