package org.daum.library.sensors;

import android.content.Context;
import android.os.Vibrator;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 01/10/12
 * Time: 11:24
 * To change this template use File | Settings | File Templates.
 */

@Library(name = "Android")
@Provides({
        @ProvidedPort(name = "tick", type = PortType.MESSAGE)
})
@DictionaryType({
        @DictionaryAttribute(name = "timer", defaultValue = "5", optional = true)
})
@ComponentType
public class Vibreur  extends AbstractComponentType {

    Vibrator move;

    @Start
    public void start() {
        move = (Vibrator) UIServiceHandler.getUIService().getRootActivity().getSystemService(Context.VIBRATOR_SERVICE);
    }


    @Stop
    public void stop() {

    }

    @Update
    public void update() {


    }

    @Port(name = "tick")
    public void trigger(final Object textMsg) {
        int time=Integer.parseInt(getDictionary().get("timer").toString())*100;
        move.vibrate(time);
    }
}
