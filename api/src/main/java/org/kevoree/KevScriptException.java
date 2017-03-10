package org.kevoree;

/**
 *
 * Created by leiko on 3/8/17.
 */
public class KevScriptException extends Exception {

    public KevScriptException(String msg) {
        super(msg);
    }

    public KevScriptException(String msg, Exception ex) {
        super(msg, ex);
    }

    public KevScriptException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public KevScriptException(Exception e) {
        super(e);
    }

    public KevScriptException(Throwable e) {
        super(e);
    }
}
