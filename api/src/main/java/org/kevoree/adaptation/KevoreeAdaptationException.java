package org.kevoree.adaptation;

/**
 *
 * Created by leiko on 3/1/17.
 */
public class KevoreeAdaptationException extends Exception {

    public KevoreeAdaptationException(Exception e) {
        super(e);
    }

    public KevoreeAdaptationException(String msg) {
        super(msg);
    }

    public KevoreeAdaptationException(String msg, Throwable e) {
        super(msg, e);
    }

}
