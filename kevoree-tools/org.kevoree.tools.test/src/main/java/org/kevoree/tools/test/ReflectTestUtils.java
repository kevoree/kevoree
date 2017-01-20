package org.kevoree.tools.test;

import org.kevoree.api.helper.ReflectUtils;

import java.lang.reflect.Field;

/**
 *
 * Created by leiko on 1/16/17.
 */
public class ReflectTestUtils {

    public static void setField(Object instance, String fieldName, Object value) {
        Field field = ReflectUtils.getField(fieldName, instance.getClass());
        if (field == null) {
            throw new KevoreeSetFieldException(fieldName, instance.getClass());
        }

        try {
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);
            field.set(instance, value);
            field.setAccessible(isAccessible);
        } catch (IllegalAccessException ignore) {
            /* should never happen as we force setAccessible to true */
        }
    }
}
