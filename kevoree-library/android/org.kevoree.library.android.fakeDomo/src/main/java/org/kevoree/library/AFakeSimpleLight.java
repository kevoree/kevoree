package org.kevoree.library;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.Toast;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 02/09/11
 * Time: 16:23
 */
@Provides({
		@ProvidedPort(name = "on", type = PortType.MESSAGE),
		@ProvidedPort(name = "off", type = PortType.MESSAGE),
		@ProvidedPort(name = "toggle", type = PortType.SERVICE, className = AToggleLightService.class)
})
/*@DictionaryType({
		@DictionaryAttribute(name = "COLOR_ON", defaultValue = "GREEN", optional = true,
				vals = {"GREEN", "RED", "BLUE", "YELLOW"}),
		@DictionaryAttribute(name = "COLOR_OFF", defaultValue = "RED", optional = true,
				vals = {"GREEN", "RED", "BLUE", "YELLOW"})
})*/
@Library(name = "Android")
@ComponentType
public class AFakeSimpleLight extends AbstractComponentType implements AToggleLightService {
	private static final Logger logger = LoggerFactory.getLogger(AFakeSimpleLight.class);

	private KevoreeAndroidService uiService = null;
	private ImageView view = null;
	private Boolean on;

	/*private static Map<String, Integer> colors = defineColors();

	private static Map<String, Integer> defineColors () {
		Map<String, Integer> colors = new HashMap<String, Integer>(4);
		colors.put("GREEN", Color.GREEN);
		colors.put("RED", Color.RED);
		colors.put("BLUE", Color.BLUE);
		colors.put("YELLOW", Color.YELLOW);
		return colors;
	}*/

	@Start
	public void start () {
		//uiService = UIServiceHandler.getUIService((Bundle) this.getDictionary().get("osgi.bundle"));
		view = new ImageView(uiService.getRootActivity());
		on = false;
		uiService.addToGroup("kevLight", view);
		uiService.getRootActivity().runOnUiThread(new Runnable() {
			@Override
			public void run () {
				applyColor(view, false);
			}
		});
	}

	@Stop
	public void stop () {

	}

	@Update
	public void update () {
		stop();
		start();
	}

	private void applyColor (ImageView view, boolean on) {
		Bitmap image = null;
		if (on) {
			try {
				InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("ampAllume.png");
				image = BitmapFactory.decodeStream(inputStream);
			} catch (Exception e) {
			}
		} else {
			try {
				InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("ampEteinte.png");
				image = BitmapFactory.decodeStream(inputStream);
			} catch (Exception e) {
			}
		}
		if (image != null) {
			view.setImageBitmap(image);
		}
	}

	@Port(name = "on")
	public void lightOn (Object message) {
		uiService.getRootActivity().runOnUiThread(new Runnable() {
			@Override
			public void run () {
				applyColor(view, true);
				Toast.makeText(uiService.getRootActivity(), "Light on!", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Port(name = "off")
	public void lightOff (Object message) {
		uiService.getRootActivity().runOnUiThread(new Runnable() {
			@Override
			public void run () {
				applyColor(view, false);
				Toast.makeText(uiService.getRootActivity(), "Light off!", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Port(name = "toggle", method = "toggle")
	public String toggle () {
		if (on) {
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