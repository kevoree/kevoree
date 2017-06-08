package org.kevoree.api.helper;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.kevoree.KevoreeParamException;
import org.kevoree.annotation.Param;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * Created by leiko on 3/7/17.
 */
public class ParamInjector {

    public static String get(String fieldName, Object instanceObj)
            throws InvocationTargetException, IllegalAccessException {
        String result = null;
        String getterName = "get";
        boolean found = false;

        getterName = getterName + fieldName.substring(0, 1).toUpperCase();
        if (fieldName.length() > 1) {
            getterName = getterName + fieldName.substring(1);
        }

        Method getter = lookupMethod(getterName, instanceObj.getClass());
        if (getter != null) {
            if (getter.getReturnType() != null) {
                boolean isAccessible = getter.isAccessible();
                if (!isAccessible) {
                    getter.setAccessible(true);
                }

                Object resultObj = getter.invoke(instanceObj);
                if (resultObj != null) {
                    result = resultObj.toString();
                }
                found = true;

                // reset to default boolean is accessible
                getter.setAccessible(isAccessible);
            }
        }

        if (!found) {
            Field field = lookup(fieldName, instanceObj.getClass());
            if (field != null) {
                boolean isAccessible = field.isAccessible();
                if (!isAccessible) {
                    field.setAccessible(true);
                }

                Object resultObj = field.get(instanceObj);
                if (resultObj != null) {
                    result = resultObj.toString();
                }

                field.setAccessible(isAccessible);
            }
        }

        return result;
    }

    public static void inject(String fieldName, String value, Object instanceObj)
            throws KevoreeParamException, InvocationTargetException, IllegalAccessException {
        boolean isSet = false;
        String setterName = "set";
        setterName = setterName + fieldName.substring(0, 1).toUpperCase();
        if (fieldName.length() > 1) {
            setterName = setterName + fieldName.substring(1);
        }
        Method setter = lookupMethod(setterName, instanceObj.getClass());
        if (setter != null && setter.getParameterTypes().length == 1) {
            boolean isAccessible = setter.isAccessible();
            if (!isAccessible) {
                setter.setAccessible(true);
            }
            Class pClazz = setter.getParameterTypes()[0];
            if (pClazz.equals(boolean.class) || pClazz.equals(Boolean.class)) {
                setter.invoke(instanceObj, Boolean.parseBoolean(value));
                isSet = true;
            } else if (pClazz.equals(int.class) || pClazz.equals(Integer.class)) {
                setter.invoke(instanceObj, Integer.parseInt(value));
                isSet = true;
            } else if (pClazz.equals(long.class) || pClazz.equals(Long.class)) {
                setter.invoke(instanceObj, Long.parseLong(value));
                isSet = true;
            } else if (pClazz.equals(double.class) || pClazz.equals(Double.class)) {
                setter.invoke(instanceObj, Double.parseDouble(value));
                isSet = true;
            } else if (pClazz.equals(short.class) || pClazz.equals(Short.class)) {
                setter.invoke(instanceObj, Short.parseShort(value));
                isSet = true;
            } else if (pClazz.equals(float.class) || pClazz.equals(Float.class)) {
                setter.invoke(instanceObj, Float.parseFloat(value));
                isSet = true;
            } else if (pClazz.equals(byte.class) || pClazz.equals(Byte.class)) {
                setter.invoke(instanceObj, Byte.parseByte(value));
                isSet = true;
            } else {
                setter.invoke(instanceObj, value);
                isSet = true;
            }
            // reset to default boolean is accessible
            setter.setAccessible(isAccessible);
        }

        if (!isSet) {
            Field f = lookup(fieldName, instanceObj.getClass());
            if (f != null) {
                boolean isAccessible = f.isAccessible();
                if (!isAccessible) {
                    f.setAccessible(true);
                }
                if (f.getType().equals(boolean.class)) {
                    f.setBoolean(instanceObj, Boolean.parseBoolean(value));
                    isSet = true;
                } else if (f.getType().equals(int.class)) {
                    f.setInt(instanceObj, Integer.parseInt(value));
                    isSet = true;
                } else if (f.getType().equals(long.class)) {
                    f.setLong(instanceObj, Long.parseLong(value));
                    isSet = true;
                } else if (f.getType().equals(double.class)) {
                    f.setDouble(instanceObj, Double.parseDouble(value));
                    isSet = true;
                } else if (f.getType().equals(short.class)) {
                    f.setShort(instanceObj, Short.parseShort(value));
                    isSet = true;
                } else if (f.getType().equals(float.class)) {
                    f.setFloat(instanceObj, Float.parseFloat(value));
                    isSet = true;
                } else if (f.getType().equals(byte.class)) {
                    f.setByte(instanceObj, Byte.parseByte(value));
                    isSet = true;
                } else if (f.getType().equals(Boolean.class)) {
                    f.set(instanceObj, Boolean.parseBoolean(value));
                    isSet = true;
                } else if (f.getType().equals(Integer.class)) {
                    f.set(instanceObj, Integer.parseInt(value));
                    isSet = true;
                } else if (f.getType().equals(Long.class)) {
                    f.set(instanceObj, Long.parseLong(value));
                    isSet = true;
                } else if (f.getType().equals(Double.class)) {
                    f.set(instanceObj, Double.parseDouble(value));
                    isSet = true;
                } else if (f.getType().equals(Short.class)) {
                    f.set(instanceObj, Short.parseShort(value));
                    isSet = true;
                } else if (f.getType().equals(Float.class)) {
                    f.set(instanceObj, Float.parseFloat(value));
                    isSet = true;
                } else if (f.getType().equals(Byte.class)) {
                    f.set(instanceObj, Byte.parseByte(value));
                    isSet = true;
                } else {
                    f.set(instanceObj, value);
                    isSet = true;
                }
                if (value.length() == 1) {
                    if (f.getType().equals(char.class)) {
                        f.set(instanceObj, value.charAt(0));
                        isSet = true;
                    }
                }
                // reset accessible boolean to previous value
                f.setAccessible(isAccessible);
            }
        }

        if (!isSet) {
            throw new KevoreeParamException("Unable to find a setter/field named \"" + fieldName + "\" in " + instanceObj.getClass().getName());
        }
    }

    private static Method lookupMethod(String name, Class clazz) {
        Method f = null;
        for (Method loopMethod : clazz.getDeclaredMethods()) {
            if (name.equals(loopMethod.getName())) {
                f = loopMethod;
            }
        }
        if (f != null) {
            return f;
        } else {
            for (Class loopClazz : clazz.getInterfaces()) {
                f = lookupMethod(name, loopClazz);
                if (f != null) {
                    return f;
                }
            }
            if (clazz.getSuperclass() != null) {
                f = lookupMethod(name, clazz.getSuperclass());
                if (f != null) {
                    return f;
                }
            }
        }
        return f;
    }

    private static Field lookup(String name, Class clazz) {
        if (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                return lookup(name, clazz.getSuperclass());
            }
        }

        return null;
    }
}
