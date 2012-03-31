/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.freepastry;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.PortType;
import org.kevoree.annotation.RequiredPort;
import org.kevoree.annotation.Requires;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.annotation.Update;
import org.kevoree.framework.AbstractComponentType;

/**
 *
 * @author sunye
 */
@Library(name = "Freepastry")
@ComponentType
@Requires({
    @RequiredPort(name = "dht", type = PortType.SERVICE, className = DHTNode.class)
})
public class FreepastryClient extends AbstractComponentType {

    private DHTNode dht;

    @Start
    public void start() {
        //pour un port de type service :

        if (this.isPortBinded("dht")) {
            dht = this.getPortByName("dht", DHTNode.class);
        }
        
        
        if (null != dht) {
            
            dht.put("aaa", "aaa");
            dht.put("bbb", "bbb");
            dht.put("ccc", "ccc");
            dht.put("ddd", "ddd");
            
            System.out.println(dht.get("aaa"));
        }
        
        /*
        pour un port de type message :
        if (isPortBinded("on")) {
        getPortByName("on", MessagePort.class).process(<données à envoyer sous la forme d'un seul objet qui peut être un StdKevoreeMessage>);
        }*/
    }
    
    @Stop
    public void stop() {
    }

    @Update
    public void update() {
    }
}
