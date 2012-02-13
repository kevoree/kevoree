package org.kevoree.library;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import android.widget.Button;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.KevoreeMessage;
import org.kevoree.framework.MessagePort;


/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 09/11/11
 * Time: 14:58
 * To change this template use File | Settings | File Templates.
 */
@Provides({
        @ProvidedPort(name = "showText", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "textEntered", type = PortType.MESSAGE, optional = true)
})
@ComponentType
@Library(name = "Android")
public class AFakeConsole extends AbstractComponentType {
    KevoreeAndroidService uiService = null;
    Object bundle;
    LinearLayout layout;
    ImageView kevoreeimg = null;
    TextView textview=null;
    EditText texteditor=null;
    Button button = null;


    @Start
    public void start() {

        bundle = this.getDictionary().get("osgi.bundle");
       // uiService = UIServiceHandler.getUIService((Bundle) bundle);

        button = new Button(uiService.getRootActivity());
        button.setText("Send");
        button.setWidth(300);



        layout = new LinearLayout(uiService.getRootActivity());
        layout.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT,AbsListView.LayoutParams.FILL_PARENT));
        layout.setOrientation(LinearLayout.VERTICAL);

        textview = new TextView(uiService.getRootActivity());
        textview.setBackgroundColor(Color.WHITE);
        textview.setHeight(200);
        textview.setWidth(500);
        textview.setMovementMethod(new ScrollingMovementMethod());

        texteditor = new EditText(uiService.getRootActivity());
        texteditor.setWidth(200);

        layout.addView(textview);
        layout.addView(texteditor);
        layout.addView(button);

        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPortBinded("textEntered")) {
                    getPortByName("textEntered", MessagePort.class).process(texteditor.getText());
                }
            }
        });

        uiService.addToGroup("Fake Console", layout);
    }


    @Stop
    public void stop() {
        uiService.remove(textview);
    }


    @Port(name = "showText")
    public void appendIncoming(final Object text)
    {
        uiService.getRootActivity().runOnUiThread(new Runnable() {
            @Override
            public void run ()
            {
                if (text != null) {
                    textview.append("\n");
                    if (text instanceof KevoreeMessage) {
                        KevoreeMessage kmsg = (KevoreeMessage) text;
                        textview.append("->");
                        for(String key : kmsg.getKeys()){
                            textview.append(key+"="+kmsg.getValue(key).get());
                        }
                    } else {
                        textview.append("->"+text.toString());
                    }
                }

            }
        });

    }
}