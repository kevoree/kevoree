package org.kevoree.framework;

import java.util.HashMap
import java.util.concurrent.Callable
import org.kevoree.ContainerRoot
import org.kevoree.annotation.LocalBindingUpdated
import org.kevoree.annotation.RemoteBindingUpdated
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.framework.internal.MethodAnnotationResolver
import org.kevoree.framework.message.FragmentBindMessage
import org.kevoree.framework.message.FragmentUnbindMessage
import org.kevoree.framework.message.Message
import org.kevoree.framework.message.PortBindMessage
import org.kevoree.framework.message.PortUnbindMessage
import org.kevoree.framework.port.PausablePortThreadPoolExecutor
import org.slf4j.LoggerFactory
import org.kevoree.framework.internal.FieldAnnotationResolver
import org.kevoree.annotation.KevoreeInject
import java.lang.reflect.Modifier
import org.kevoree.api.Bootstraper
import org.kevoree.api.service.core.script.KevScriptEngineFactory

class ChannelTypeFragmentThread(val target: AbstractChannelFragment, val _nodeName: String, val _name: String, val modelService: KevoreeModelHandlerService,val bootService:Bootstraper,val kevsEngine : KevScriptEngineFactory): KevoreeChannelFragment, KInstance, ChannelFragment {

    public fun initChannel(){
        target.delegate = this
    }

    public override fun dispatch(msg: Message?): Any? {
        if(msg!!.getInOut()){
            return sendWait(msg)
        } else {
            send(msg)
            return null
        }
    }
    public override fun createSender(remoteNodeName: String?, remoteChannelName: String?): ChannelFragmentSender? {
        return target.createSender(remoteNodeName, remoteChannelName)
    }
    public override fun getNodeName(): String {
        return _nodeName
    }
    public override fun getName(): String {
        return _name
    }

    val kevoree_internal_logger = LoggerFactory.getLogger(this.javaClass)!!
    var pool: PausablePortThreadPoolExecutor? = null
    val portsBinded: MutableMap<String, KevoreePort> = HashMap<String, KevoreePort>()
    val fragementBinded: MutableMap<String, KevoreeChannelFragment> = HashMap<String, KevoreeChannelFragment>()
    var isStarted: Boolean = false
    val resolver: MethodAnnotationResolver = MethodAnnotationResolver(target.javaClass)
    private val fieldResolver = FieldAnnotationResolver(target.javaClass);


    override fun kInstanceStart(tmodel: ContainerRoot): Boolean {
        if (!isStarted) {
            try {

                val modelServiceFields = fieldResolver.resolve(javaClass<KevoreeInject>(), javaClass<KevoreeModelHandlerService>())!!
                for(mserv in modelServiceFields){
                    if(Modifier.isPrivate(mserv.getModifiers())){
                        mserv.setAccessible(true);
                    }
                    mserv.set(target, modelService)
                }
                val bootServiceField = fieldResolver.resolve(javaClass<KevoreeInject>(), javaClass<Bootstraper>())!!
                for(loopField in bootServiceField){
                    if(Modifier.isPrivate(loopField.getModifiers())){
                        loopField.setAccessible(true);
                    }
                    loopField.set(target, bootService)
                }
                val kevSField = fieldResolver.resolve(javaClass<KevoreeInject>(), javaClass<KevScriptEngineFactory>())!!
                for(loopField in kevSField){
                    if(Modifier.isPrivate(loopField.getModifiers())){
                        loopField.setAccessible(true);
                    }
                    loopField.set(target, kevsEngine)
                }

                target.setName(_name)
                target.setNodeName(_nodeName)


                (target.getModelService() as ModelHandlerServiceProxy).setTempModel(tmodel)
                val met = resolver.resolve(javaClass<org.kevoree.annotation.Start>())
                met?.invoke(target)
                (target.getModelService() as ModelHandlerServiceProxy).unsetTempModel()
                isStarted = true
                pool = PausablePortThreadPoolExecutor.newPausableThreadPool(1)
                return true
            } catch(e: Exception) {
                kevoree_internal_logger.error("Kevoree Channel Instance Start Error !", e)
                return false
            }
        } else {
            return false
        }
    }

    override fun kInstanceStop(tmodel: ContainerRoot): Boolean {
        if (isStarted) {
            try {
                if (pool != null) {
                    pool!!.shutdownNow()
                    pool = null
                }
                //TODO CHECK QUEUE SIZE AND SAVE STATE
                (target.getModelService() as ModelHandlerServiceProxy).setTempModel(tmodel)
                val met = resolver.resolve(javaClass<org.kevoree.annotation.Stop>())
                met?.invoke(target)
                (target.getModelService() as ModelHandlerServiceProxy).unsetTempModel()
                isStarted = false
                return true
            } catch(e: Exception) {
                kevoree_internal_logger.error("Kevoree Channel Instance Stop Error !", e)
                return false
            }
        } else {
            return false
        }
    }

    public override fun kUpdateDictionary(d: Map<String, Any>, cmodel: ContainerRoot): Map<String, Any>? {
        try {
            val previousDictionary = target.getDictionary()!!.clone()
            for(v in d.keySet()) {
                target.getDictionary()!!.put(v, d.get(v)!!)
            }
            if (isStarted) {
                (target.getModelService() as ModelHandlerServiceProxy).setTempModel(cmodel)
                val met = resolver.resolve(javaClass<org.kevoree.annotation.Update>())
                met?.invoke(target)
                (target.getModelService() as ModelHandlerServiceProxy).unsetTempModel()
            }
            return previousDictionary as Map<String, Any>?
        } catch(e: Exception) {
            kevoree_internal_logger.error("Kevoree Group Instance Update Error !", e)
            return null
        }
    }

    override fun send(o: Any?) {
        pool!!.submit(AsyncCall(o))
    }

    override fun sendWait(o: Any?): Any? {
        return pool!!.submit(SyncCall(o)).get()
    }

    inner class AsyncCall(val o: Any?): Runnable {
        override fun run() {
            when(o) {
                is Message -> {
                    target.dispatch(o)
                }
                else -> {
                    val msg2 = Message()
                    msg2.setInOut(false)
                    msg2.setContent(o!!)
                    target.dispatch(msg2)
                }
            }
        }
    }

    inner class SyncCall(val o: Any?): Callable<Any> {
        override fun call(): Any? {
            when(o) {
                is Message -> {
                    return dispatch(o)
                }
                is MethodCallMessage -> {
                    val msg2 = Message()
                    msg2.setInOut(true)
                    msg2.setContent(o)
                    return target.dispatch(msg2)
                }
                else -> {
                    val msg2 = Message()
                    msg2.setInOut(true)
                    msg2.setContent(o!!)
                    return target.dispatch(msg2)
                }
            }
        }
    }

    public fun forward(delegate: KevoreeChannelFragment?, inmsg: Message?): Any? {
        val msg = inmsg!!.clone()
        msg.setDestChannelName(delegate!!.getName())
        msg.setDestNodeName(delegate!!.getNodeName())
        if (msg.getInOut()) {
            return delegate.sendWait(msg)
        } else {
            delegate.send(msg)
            return null
        }
    }

    public fun forward(delegate: KevoreePort?, inmsg: Message?): Any? {
        val msg = inmsg!!.clone()
        msg.setDestChannelName(delegate!!.getName()!!)
        if (msg.getInOut()) {
            return delegate.sendWait(msg.getContent())
        } else {
            delegate.send(msg.getContent())
            return null
        }
    }

    override fun processAdminMsg(o: Any): Boolean {
        pool?.pause()
        val res = when(o) {
            is FragmentBindMessage -> {
                kevoree_internal_logger.debug("FragmentBindMessage=>" + createPortKey(o))
                val sender = this.createSender((o as FragmentBindMessage).fragmentNodeName, (o as FragmentBindMessage).channelName)
                val proxy = KevoreeChannelFragmentThreadProxy((o as FragmentBindMessage).fragmentNodeName, (o as FragmentBindMessage).channelName)
                proxy.channelSender = sender
                fragementBinded.put(createPortKey(o), proxy)
                //proxy.startC()
                val met = resolver.resolve(javaClass<RemoteBindingUpdated>())
                if (met != null) {
                    met.invoke(target)
                }
                true
            }
            is FragmentUnbindMessage -> {
                kevoree_internal_logger.debug("Try to unbind channel ")
                val actorPort: KevoreeChannelFragment? = fragementBinded.get(createPortKey(o))
                if (actorPort != null) {
                    //actorPort.stopC()
                    fragementBinded.remove(createPortKey(o))
                    val met = resolver.resolve(javaClass<RemoteBindingUpdated>())
                    if (met != null) {
                        met.invoke(target)
                    }
                    true
                } else {
                    kevoree_internal_logger.debug("Can't unbind Fragment " + createPortKey(o))
                    false
                }
            }
            is PortBindMessage -> {
                portsBinded.put(createPortKey(o), (o as PortBindMessage).proxy)
                val met = resolver.resolve(javaClass<LocalBindingUpdated>())
                if (met != null) {
                    met.invoke(target)
                }
                true
            }
            is PortUnbindMessage -> {
                portsBinded.remove(createPortKey(o))
                val met = resolver.resolve(javaClass<LocalBindingUpdated>())
                if (met != null) {
                    met.invoke(target)
                }
                true
            }
            else -> {
                false
            }
        }
        pool?.resume()
        return res
    }

    private fun createPortKey(a: Any): String {
        when(a) {
            is PortBindMessage -> {
                return (a as PortBindMessage).nodeName + "-" + (a as PortBindMessage).componentName + "-" + (a as PortBindMessage).portName
            }
            is PortUnbindMessage -> {
                return (a as PortUnbindMessage).nodeName + "-" + (a as PortUnbindMessage).componentName + "-" + (a as PortUnbindMessage).portName
            }
            is FragmentBindMessage -> {
                return (a as FragmentBindMessage).channelName + "-" + (a as FragmentBindMessage).fragmentNodeName
            }
            is FragmentUnbindMessage -> {
                return (a as FragmentUnbindMessage).channelName + "-" + (a as FragmentUnbindMessage).fragmentNodeName
            }
            else -> {
                return ""
            }
        }
    }


    fun getBindedPorts(): List<KevoreePort> {
        return portsBinded.values().toList()
    }

    //OVERRIDE BY FACTORY
    fun getOtherFragments(): List<KevoreeChannelFragment> {
        return fragementBinded.values().toList()
    }

}





