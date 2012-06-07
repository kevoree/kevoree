package org.kevoree.library;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

import java.util.HashMap;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 02/09/11
 * Time: 16:24
 */
@Requires({
		@RequiredPort(name = "on", type = PortType.MESSAGE, needCheckDependency = false , optional = true),
		@RequiredPort(name = "off", type = PortType.MESSAGE, needCheckDependency = false ,  optional = true),
		@RequiredPort(name = "toggle", type = PortType.SERVICE, className = ToggleLightService.class, optional = true)
})
@Library(name = "Android")
@ComponentType
public class FakeSimpleSwitch extends AbstractComponentType {

	private KevoreeAndroidService uiService = null;
	private ImageView view = null;
	private Boolean on;
	private Button buttonToggle;

    private LinearLayout layout = null;

	@Start
	public void start () {
        uiService = UIServiceHandler.getUIService();

        //uiService = UIServiceHandler.getUIService((Bundle) this.getDictionary().get("osgi.bundle"));
		layout = new LinearLayout(uiService.getRootActivity());
		layout.setOrientation(LinearLayout.VERTICAL);

		Button buttonON = new Button(uiService.getRootActivity());
		buttonON.setText("ON");
		buttonON.setOnClickListener(new View.OnClickListener() {
			public void onClick (View v) {
				processOn();
			}
		});
		Button buttonOFF = new Button(uiService.getRootActivity());
		buttonOFF.setText("OFF");
		buttonOFF.setOnClickListener(new View.OnClickListener() {
			public void onClick (View v) {
				processOff();
			}
		});
		buttonToggle = new Button(uiService.getRootActivity());
		buttonToggle.setText("toggle");
		buttonToggle.setOnClickListener(new View.OnClickListener() {
			public void onClick (View v) {
				toggle();
			}
		});

		layout.addView(buttonON);
		layout.addView(buttonOFF);
		layout.addView(buttonToggle);
        uiService.addToGroup("kevSwitch"+getName(), layout);
	}

	@Stop
	public void stop () {
        uiService.remove(layout);
	}


	@Update
	public void update() {
		stop();
		start();
	}

	public void processOn () {
		if (isPortBinded("on")) {
			getPortByName("on", MessagePort.class).process(new HashMap<String, String>());
		}
	}

	public void processOff () {
		if (isPortBinded("off")) {
			getPortByName("off", MessagePort.class).process(new HashMap<String, String>());
		}
	}

	public void toggle () {
		if (isPortBinded("toggle")) {
			String state = getPortByName("toggle", ToggleLightService.class).toggle();
			buttonToggle.setText(state);
		}
	}
}
