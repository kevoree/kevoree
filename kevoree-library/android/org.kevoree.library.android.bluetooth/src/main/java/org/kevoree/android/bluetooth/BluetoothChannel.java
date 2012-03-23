package org.kevoree.android.bluetooth;

import android.bluetooth.BluetoothDevice;
import eu.powet.android.rfcomm.BluetoothEvent;
import eu.powet.android.rfcomm.BluetoothEventListener;
import eu.powet.android.rfcomm.IRfcomm;
import eu.powet.android.rfcomm.Rfcomm;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 23/03/12
 * Time: 09:03
 */

@Library(name = "android")
@ChannelTypeFragment
@DictionaryType({
        @DictionaryAttribute(name="name",defaultValue="*",optional=false, fragmentDependant = true)
}
)
public class BluetoothChannel extends AbstractChannelFragment
{
    private Logger logger = LoggerFactory.getLogger(BluetoothChannel.class);
    private KevoreeAndroidService uiservice=null;
    private IRfcomm libbluetooth;
    private  BluetoothManager manager;
    @Start
    public void startChannel(){

        manager = new BluetoothManager();
        manager.make(libbluetooth);

        uiservice =  UIServiceHandler.getUIService();
        libbluetooth = new Rfcomm(uiservice.getRootActivity());

        libbluetooth.addEventListener(new BluetoothEventListener()
        {
            @Override
            public void incomingDataEvent(BluetoothEvent evt)
            {
                Message msg = new Message();
                msg.setContent(new String(libbluetooth.read()));
                msg.setDestNodeName(getNodeName());

                /*   Local Node  */
                for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
                    forward(p, msg) ;
                }
            }

            @Override
            public void discoveryFinished(BluetoothEvent evt)
            {
                String name = getDictionary().get("name").toString();
                boolean found = false;
                for( BluetoothDevice device : libbluetooth.getDevices())
                {
                    System.out.println("Device found ="+device.getName()+" "+device.getAddress());

                    if(device.getName().equals(name))
                    {
                        libbluetooth.open(device.getAddress());
                        found = true;
                        break;
                    }

                    if(name.equals("*")){
                        if(device.getName().contains("node"))
                        {
                            libbluetooth.open(device.getAddress());
                            found = true;
                            break;
                        }
                    }
                }
                if(!found)
                {
                    libbluetooth.discovering();
                }
            }

            @Override
            public void disconnected()
            {
                System.out.println("Disconnected");
                libbluetooth.close();
                libbluetooth.discovering();
            }
        });

    }

    public  void  doopen(){

    }
    @Stop
    public  void stopChannel(){

    }

    public void dummy() {}

    @Override
    public Object dispatch(Message msg) {
        /*   Remote Node */
        for (KevoreeChannelFragment cf : getOtherFragments()) {
            if (!msg.getPassedNodes().contains(cf.getNodeName())) {
                forward(cf, msg);
            }
        }
        return null;
    }

    @Override
    public ChannelFragmentSender createSender(final String remoteNodeName, String remoteChannelName) {

        return new ChannelFragmentSender()
        {
            @Override
            public Object sendMessageToRemote(Message message) {

                logger.debug("Sending message to " + remoteNodeName);
                manager.sendMessage(message);
                return null;
            }
        } ;

    }


}
