package org.kevoree.library.kotlin

import org.kevoree.framework.ChannelFragment
import org.kevoree.framework.osgi.KevoreeInstanceActivator

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 09/10/12
 * Time: 17:07
 * To change this template use File | Settings | File Templates.
 */

class MyTest {
    fun titi(t : String): Int {
        return 3
    }

}

trait t1 {
    fun tamereEnshort(): String {
        return "wtf"
    }
}

class Composed : t1,DummyJavaClass(){



}

        /*
class KotlinHelloWorldFactory : org.kevoree.framework.osgi.KevoreeInstanceFactory {

    public override fun createInstanceActivator(): KevoreeInstanceActivator? {
        throw UnsupportedOperationException()
    }
    public override fun remove(instanceName: String?): KevoreeInstanceActivator? {
        throw UnsupportedOperationException()
    }
    public override fun registerInstance(instanceName: String?, nodeName: String?): KevoreeInstanceActivator? {
        throw UnsupportedOperationException()
    }
    //override def registerInstance(instanceName : String, nodeName : String)=KotlinHelloWorldFactory.registerInstance(instanceName,nodeName)
//override def remove(instanceName : String)=KotlinHelloWorldFactory.remove(instanceName)
//def createInstanceActivator = KotlinHelloWorldFactory.createInstanceActivator
}  */




