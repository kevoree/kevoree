package org.kevoree.tools.test;

/**
 *
 * Created by leiko on 1/16/17.
 */
public class KevoreeSetFieldException extends RuntimeException {

    public KevoreeSetFieldException(String fieldName, Class<?> clazz) {
        super("Unable to set \""+fieldName+"\" in \""+clazz.getSimpleName()+"\"");
    }
}
