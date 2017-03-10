package org.kevoree.kevscript.resolver;

/**
 *
 * Created by leiko on 3/8/17.
 */
public abstract class AbstractResolver implements Resolver {

    private Resolver next;

    public AbstractResolver(Resolver next) {
        this.next = next;
    }

    protected final Resolver next() {
        return this.next;
    }
}
