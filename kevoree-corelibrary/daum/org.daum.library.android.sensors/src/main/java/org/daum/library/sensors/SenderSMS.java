package org.daum.library.sensors;


import org.daum.common.genmodel.SMS;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 26/09/12
 * Time: 14:10
 * To change this template use File | Settings | File Templates.
 *
 */

@Library(name = "Android")
@Provides({
        @ProvidedPort(name = "message", type = PortType.MESSAGE)
})
@ComponentType
public class SenderSMS   extends AbstractComponentType {

    @Start
    public void start()
    {

    }

    @Stop
    public void stop()
    {

    }

    @Update
    public void update()
    {

    }

    @Port(name = "message")
    public void sms(Object o)
    {
        System.out.println(o);
        if( o instanceof  SMS)
        {

            SMS current = ((SMS)o);

            if(current.getNumber().length()>= 4)
            {
                android.telephony.gsm.SmsManager.getDefault().sendTextMessage(current.getNumber(), null, current.getMsg(), null, null);
                System.out.println("SENT "+current.getNumber());
            }else {
                System.err.println("the phone number is wrong");
            }


        }   else
        {
            System.err.println("the message need to class of SMS");
        }
    }

}
