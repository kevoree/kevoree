package org.kevoree.resolver;

/**
 *
 * Created by leiko on 6/20/17.
 */
public class MavenResolverException extends Exception {

    public MavenResolverException(String message) {
        super(message);
    }

    public MavenResolverException(String message, Exception e) {
        super(message, e);
    }
}
