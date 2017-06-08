package org.kevoree.reflect;

import org.kevoree.KevoreeCoreException;

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

    /**
     * Injector usage:
     * <code>
     *     // create an injector and register services
     *     Injector injector = new Injector(Inject.class);
     *     MyService service = new MyServiceImpl();
     *     MyOtherService otherService = new MyOtherServiceImpl();
     *     injector.register(MyService.class, service);
     *     injector.register(MyOtherService.class, otherService);
     * </code>
     * Then use it for your own classes, eg:
     *
     * <code>
     *     public class MyObject {
     *         \@Inject
     *         private MyService theService; // does not have to be the same name
     *
     *         \@Inject
     *         private MyOtherService oService;
     *     }
     * </code>
     * <code>
     *     MyObject obj = new MyObject();
     *     injector.inject(obj);
     * </code>
     *
     * Now your MyObject instance will have its fields <strong>theService</strong> and <strong>oService</strong>
     * bound to their respective registered services in the Injector.
     *
     * @param injectAnnotationClass the Annotation class used by this injector to inject service instances into fields
     */
    public Injector(Class<? extends Annotation> injectAnnotationClass) {
        this.injectAnnotationClass = injectAnnotationClass;
    }

    /**
     * Registers an instance of the given
     * @param clazz    the service class to register
     * @param instance the implementation instance of that service to inject
     * @param <T>      the service type
     */
    public <T> void register(Class<T> clazz, T instance) {
        registry.put(clazz, instance);
    }

    /**
     *
     * @param clazz the service class to unregister
     * @param <T>   the service type
     */
    public <T> void unregister(Class<T> clazz) {
        registry.remove(clazz);
    }

    /**
     * Removes the given services if found
     * @param classes list of services to remove
     */
    public void unregister(Class ...classes) {
        for (Class c : classes) {
            registry.remove(c);
        }
    }

    /**
     * Injects all available registered services on the field annotated with the Annotation class given in this injector
     * @param instance the instance to inject services to
     *
     * @throws KevoreeCoreException when unable to inject service into field
     */
    public void inject(Object instance) throws KevoreeCoreException {
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
                    throw new KevoreeCoreException("Unable to to set implementation to service " + field.getType().getSimpleName() + " in " + instance.getClass(), e);
                } finally {
                    if (!isAccessible) {
                        field.setAccessible(false);
                    }
                }
            } else {
                throw new KevoreeCoreException("Unable to find implementation of type "+field.getType().getName()+" to inject");
            }
        }
    }

    public <T> T get(Class<T> clazz) {
        return (T) this.registry.get(clazz);
    }
}
