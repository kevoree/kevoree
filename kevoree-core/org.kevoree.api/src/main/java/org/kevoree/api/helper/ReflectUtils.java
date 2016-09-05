package org.kevoree.api.helper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * Java reflection utility class
 */
public class ReflectUtils {

    /**
     * Look for the annotationType in the class or one of its ancestors.
     *
     * @param clazz          The class to scan.
     * @param annotationType The annotation type to find.
     * @return The annotation, null if not found.
     */
    public static <T extends Annotation> T findAnnotation(Class<?> clazz, Class<T> annotationType) {
        T a = clazz.getAnnotation(annotationType);
        if (a != null) {
            return a;
        }

        Class<?> parentClass = clazz.getSuperclass();
        if (parentClass != null && !parentClass.equals(Object.class)) {
            a = findAnnotation(parentClass, annotationType);
        }

        if (a == null) {
            for (Class<?> i : clazz.getInterfaces()) {
                a = findAnnotation(i, annotationType);
                if (a != null) {
                    return a;
                }
            }
        }

        return a;
    }

    /**
     * Look for a field based on his name and a given Annotation in a class.
     *
     * @param fieldName      The field name.
     * @param clazz          The scanned class.
     * @param annotationType The expected annotation type.
     * @return The field if found, null if not.
     */
    public static Field findFieldWithAnnotation(String fieldName, Class<?> clazz, Class<? extends Annotation> annotationType) {
        Field field = getField(fieldName, clazz);
        if (field != null) {
            for (Annotation anno : field.getDeclaredAnnotations()) {
                if (anno.annotationType().getName().equals(annotationType.getName())) {
                    return field;
                }
            }
        }
        return null;
    }

    /**
     *
     * @param clazz           The class
     * @param annotationTypes The annotation types expected
     * @return True if one of the given annotation types is found, False otherwise
     */
    public static boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation>... annotationTypes) {
        for (Class<? extends Annotation> anno : annotationTypes) {
            if (findAnnotation(clazz, anno) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find a field with the given name and class annotation
     *
     * @param fieldName The field name
     * @param clazz     The annotation class.
     * @return the field, null if not found.
     */
    public static Field getField(String fieldName, Class<?> clazz) {
        List<Field> fields = getAllFields(clazz);
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    /**
     * Return all fields of the class, including inherited one.
     *
     * @param clazz The class.
     * @return The list of all fields.
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> currentClassFields = new ArrayList<Field>(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> parentClass = clazz.getSuperclass();

        if (parentClass != null && !(parentClass.equals(Object.class))) {
            List<Field> parentClassFields = getAllFields(parentClass);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

    /**
     * Return all fields of the class if annotated with the given type.
     *
     * @param clazz          The class.
     * @param annotationType The expected annotation type.
     * @return The list of fields.
     */
    public static List<Field> getAllFieldsWithAnnotation(final Class<?> clazz, final Class<? extends Annotation> annotationType) {
        final List<Field> ret = new ArrayList<Field>();
        for(Field f: getAllFields(clazz)) {
            if(f.isAnnotationPresent(annotationType)) {
                ret.add(f);
            }
        }
        return ret;
    }

    /**
     * Return all fields of the class if annotated
     *
     * @param clazz           The class.
     * @param annotationTypes The annotation types
     * @return The list of fields.
     */
    public static List<Field> getAllFieldsWithAnnotations(Class<?> clazz, List<Class<? extends Annotation>> annotationTypes) {
        List<Field> fields = new ArrayList<Field>();
        for (Class<? extends Annotation> annotationType : annotationTypes) {
            fields.addAll(getAllFieldsWithAnnotation(clazz, annotationType));
        }
        return fields;
    }

    /**
     * Return all fields of the class if annotated
     *
     * @param clazz           The class.
     * @param annotationTypes The annotation types
     * @return The list of fields.
     */
    public static List<Field> getAllFieldsWithAnnotations(Class<?> clazz, Class<? extends Annotation>... annotationTypes) {
        return getAllFieldsWithAnnotations(clazz, Arrays.asList(annotationTypes));
    }

    /**
     * Returns a Method annotated with the given Annotation class
     *
     * @param clazz the target object class
     * @param annoClass the annotation class that you are looking for
     * @return the annotated method if any; null otherwise
     */
    public static Method findMethodWithAnnotation(final Class<?> clazz, final Class<? extends Annotation> annoClass) {
        Method method;
        for (Method metLoop : clazz.getDeclaredMethods()) {
            if (metLoop.getAnnotation(annoClass) != null) {
                method = metLoop;
                return method;
            }
        }

        if (clazz.getSuperclass() != null) {
            method = findMethodWithAnnotation(clazz.getSuperclass(), annoClass);
            if (method != null) {
                return method;
            }
        }

        for (Class<?> iClazz : clazz.getInterfaces()) {
            method = findMethodWithAnnotation(iClazz, annoClass);
            if (method != null) {
                return method;
            }
        }
        return null;
    }
}
