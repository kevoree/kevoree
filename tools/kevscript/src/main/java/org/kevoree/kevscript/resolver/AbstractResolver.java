package org.kevoree.kevscript.resolver;

import org.kevoree.KevScriptException;

/**
 * Chain-of-responsibility pattern
 *
 * Created by leiko on 3/8/17.
 */
public abstract class AbstractResolver implements Resolver {

    private Resolver next;

    public AbstractResolver(Resolver next) {
        this.next = next;
    }

    protected final Resolver next() throws KevScriptException {
        if (this.next != null) {
            return this.next;
        } else {
            throw new KevScriptException("No successor found");
        }
    }
}
