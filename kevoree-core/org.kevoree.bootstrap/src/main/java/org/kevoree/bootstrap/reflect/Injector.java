package org.kevoree.bootstrap.reflect;

import org.kevoree.api.helper.ReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class Injector {

    private Class<? extends Annotation> injectAnnotationClass;
    private Map<Class<?>, Object> registry = new HashMap<>();

    public Injector(Class<? extends Annotation> injectAnnotationClass) {
        this.injectAnnotationClass = injectAnnotationClass;
    }

    /**
     *
     * @param ctxType the service class to register
     * @param impl    the implementation of that service to bind
     * @param <T>     the service type
     */
    public <T> void register(Class<T> ctxType, T impl) {
        registry.put(ctxType, impl);
    }

    /**
     *
     * @param ctxType the service class to unregister
     * @param <T>     the service type
     */
    public <T> void unregister(Class<T> ctxType) {
        registry.remove(ctxType);
    }

    /**
     * Injects any available service registered if asked by the instance with
     * the Annotation class given in the Injector constructor
     * @param instance the instance to inject services to
     */
    public void inject(Object instance) {
        List<Field> fields = ReflectUtils.getAllFieldsWithAnnotation(instance.getClass(), injectAnnotationClass);
        for (Field field : fields) {
            Object impl = this.registry.get(field.getType());
            if (impl != null) {
                boolean isAccessible = field.isAccessible();
                if (!isAccessible) {
                    field.setAccessible(true);
                }
                try {
                    field.set(instance, impl);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } finally {
                    if (!isAccessible) {
                        field.setAccessible(false);
                    }
                }
            } else {
                throw new RuntimeException("Unable to find implementation of type "+field.getType().getName()+" to inject");
            }
        }
    }
}
