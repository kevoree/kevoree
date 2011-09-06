package org.kevoree.library.android.fakeDomo;

import android.widget.ImageView;
import android.widget.Toast;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.osgi.framework.Bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 02/09/11
 * Time: 16:23
 */
@Provides({
		@ProvidedPort(name = "on", type = PortType.MESSAGE),
		@ProvidedPort(name = "off", type = PortType.MESSAGE),
		@ProvidedPort(name = "toggle", type = PortType.SERVICE, className = ToggleLightService.class)
})
@DictionaryType({
		@DictionaryAttribute(name = "COLOR_ON", defaultValue = "GREEN", optional = true,
				vals = {"GREEN", "RED", "BLUE", "YELLOW"}),
		@DictionaryAttribute(name = "COLOR_OFF", defaultValue = "RED", optional = true,
				vals = {"GREEN", "RED", "BLUE", "YELLOW"})
})
@Library(name = "Kevoree-Android")
@ComponentType
public class FakeSimpleLight extends AbstractComponentType implements ToggleLightService {

	private KevoreeAndroidService uiService = null;
	private ImageView view = null;
	private Boolean on;

	private static Map<String, Integer> colors = defineColors();

	private static Map<String, Integer> defineColors () {
		Map<String, Integer> colors = new HashMap<String, Integer>(4);
		colors.put("GREEN", 0X00ff00);
		colors.put("RED", 0Xff0000);
		colors.put("BLUE", 0X0000ff);
		colors.put("YELLOW", 0Xfff600);
		return colors;
	}

	@Start
	public void start () {
		uiService = UIServiceHandler.getUIService((Bundle) this.getDictionary().get("osgi.bundle"));
		view = new ImageView(uiService.getRootActivity().getApplication().getApplicationContext());
		applyColor(view, (String)this.getDictionary().get("COLOR_OFF"));
		on = false;
		uiService.addToGroup("kevLight", view);
	}

	@Stop
	public void stop () {

	}

	private void applyColor (ImageView view, String color) {
		if (color != null && !color.equals("") && colors.get(color) != null) {
			view.setBackgroundColor(colors.get(color));
		}
	}

	@Port(name = "on")
	public void lightOn(Object message) {
		applyColor(view, (String)this.getDictionary().get("COLOR_ON"));
		 Toast.makeText(uiService.getRootActivity(), "Light on!", Toast.LENGTH_SHORT).show();
	}
	@Port(name = "off")
	public void lightOff(Object message) {
		applyColor(view, (String)this.getDictionary().get("COLOR_OFF"));
		Toast.makeText(uiService.getRootActivity(), "Light off!", Toast.LENGTH_SHORT).show();
	}

	@Port(name = "toggle",method = "toggle")
    public String toggle(){
        if(on){
            this.lightOff("");
		on = !on;
			return "on";
        } else {
            this.lightOn("");
		on = !on;
			return "off";
        }
    }

}
