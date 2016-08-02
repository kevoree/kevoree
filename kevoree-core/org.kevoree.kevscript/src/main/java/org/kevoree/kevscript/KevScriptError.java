package org.kevoree.kevscript;

/**
 *
 */
public class KevScriptError extends Error {

    public KevScriptError(String msg, Error e) {
        super(msg, e);
    }

    public KevScriptError(String msg) {
        super(msg);
    }

    public KevScriptError(Error e) {
        super(e);
    }
}
