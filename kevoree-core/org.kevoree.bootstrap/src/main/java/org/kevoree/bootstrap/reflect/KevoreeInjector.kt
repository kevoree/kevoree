package org.kevoree.bootstrap.reflect;

import java.util.HashMap
import org.kevoree.annotation.KevoreeInject

/**
 * Created by duke on 24/06/13.
 */
public class KevoreeInjector {

    fun clone() : KevoreeInjector {
        val newInjector = KevoreeInjector();
        newInjector.clazzList = clazzList.clone() as HashMap<Class<out Any>, Any>
        return newInjector
    }

    private var clazzList: HashMap<java.lang.Class<out Any>, Any> = HashMap<java.lang.Class<out Any>, Any>();

    fun addService(clazz: java.lang.Class<out Any>obj: Any) {
        clazzList.put(clazz, obj)
    }

    fun process(instance: Any) {
        val fieldResolver = FieldAnnotationResolver(instance.javaClass);
        for(clazz in clazzList.keySet()){
            val modelServiceFields = fieldResolver.resolve(javaClass<KevoreeInject>(), clazz)!!
            for(mserv in modelServiceFields){
                if(!mserv.isAccessible()){
                    mserv.setAccessible(true);
                }
                mserv.set(instance, clazzList.get(clazz))
            }
        }
    }

}