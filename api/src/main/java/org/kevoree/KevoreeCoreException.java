package org.kevoree;

/**
 *
 * Created by leiko on 3/8/17.
 */
public class KevoreeCoreException extends Exception {

    public KevoreeCoreException(String msg) {
        super(msg);
    }

    public KevoreeCoreException(String msg, Exception ex) {
        super(msg, ex);
    }

    public KevoreeCoreException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public KevoreeCoreException(Exception e) {
        super(e);
    }

    public KevoreeCoreException(Throwable e) {
        super(e);
    }
}
