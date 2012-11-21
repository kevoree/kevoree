package org.daum.library.sensors;


import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.daum.common.genmodel.SMS;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 02/10/12
 * Time: 14:08
 * To change this template use File | Settings | File Templates.
 */
import android.app.Activity;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.kevoree.framework.MessagePort;

@Library(name = "Android")
@Requires({
        @RequiredPort(name = "message", type = PortType.MESSAGE,optional = true)
})
@ComponentType
public class EnvoiSmsGui   extends AbstractComponentType {


    Button btnEnvoie=null;

    //On récupère les deux EditText correspondant aux champs pour entrer le numéro et le message
    EditText numero =null;
    EditText message = null;
    LinearLayout layout=null;

    @Start
    public void start()
    {
        layout = new LinearLayout(UIServiceHandler.getUIService().getRootActivity());
        btnEnvoie = new Button(UIServiceHandler.getUIService().getRootActivity());
        numero = new EditText(UIServiceHandler.getUIService().getRootActivity());
        numero.setWidth(100);
        message.setWidth(200);
        message.setHeight(500);
        message = new EditText(UIServiceHandler.getUIService().getRootActivity());

        layout.addView(numero);
        layout.addView(message);
        layout.addView(btnEnvoie);


        btnEnvoie.setOnClickListener(new OnClickListener() {

            @SuppressWarnings("deprecation")
            public void onClick(View v) {
                //On récupère ce qui a été entré dans les EditText
                String num = numero.getText().toString();
                String msg = message.getText().toString();
                //Si le numéro est supérieur à 4 charactère et que le message n'est pas vide on lance la procédure d'envoi
                if(num.length()>= 4 && msg.length() > 0){

                    SmsManager.getDefault().sendTextMessage(num, null, msg, null, null);
                    SMS t = new SMS();
                    t.setNumber(num);
                    t.setMsg(msg);

                    getPortByName("message", MessagePort.class).process(t);
                }else{
                    //On affiche un petit message d'erreur dans un Toast
                    Toast.makeText(UIServiceHandler.getUIService().getRootActivity(), "Enter le numero et/ou le message", Toast.LENGTH_SHORT).show();
                }

            }
        });
        UIServiceHandler.getUIService().addToGroup("Sms", layout);
    }

    @Stop
    public void stop()
    {
        if(layout !=null){
            UIServiceHandler.getUIService().remove(layout);
        }

    }

    @Update
    public void update()
    {

    }



}
