package org.kevoree.library.frascati.components;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/03/12
 * Time: 13:39
 */

@Library(name = "Frascati")
@ComponentType
@Provides(
        {@ProvidedPort(name = "trig", type = PortType.MESSAGE)}
)
@Requires({
        @RequiredPort(name = "run", type = PortType.SERVICE, className = java.lang.Runnable.class)
})
public class RunnableWrapper extends AbstractComponentType {

    @Start
    @Stop
    @Update
    public void dummy() {

    }

    @Port(name = "trig")
    public void trigger(Object o){
        System.out.println("Hello I Will forward");
        getPortByName("run",java.lang.Runnable.class).run();
    }

}
