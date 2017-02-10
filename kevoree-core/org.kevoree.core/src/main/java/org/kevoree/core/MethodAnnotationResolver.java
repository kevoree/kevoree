package org.kevoree.core;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 28/03/13
 * Time: 08:12
 */
public class MethodAnnotationResolver {

    private Class base = null;
    private HashMap<Class, Method> methods = new HashMap<Class, Method>();

    public MethodAnnotationResolver(Class _base) {
        base = _base;
    }

    public Method resolve(Class annotationClass) {
        return resolve(annotationClass, base);
    }

    private Method resolve(Class annotationClass, Class baseClazz) {
        Method met = methods.get(annotationClass);
        if (met == null) {
            for (Method metLoop : baseClazz.getDeclaredMethods()) {
                if (metLoop.getAnnotation(annotationClass) != null) {
                    met = metLoop;
                    methods.put(annotationClass, met);
                    return met;
                }
            }
        }
        if(baseClazz.getSuperclass() != null){
            met = resolve(annotationClass, baseClazz.getSuperclass());
            if (met != null) {
                return met;
            }
        }
        for (Class it : baseClazz.getInterfaces()) {
            met = resolve(annotationClass, it);
            if (met != null) {
                return met;
            }
        }
        return met;
    }


}
