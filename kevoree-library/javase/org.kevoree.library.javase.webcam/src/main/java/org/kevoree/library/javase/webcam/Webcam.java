package org.kevoree.library.javase.webcam;

/*import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import org.kevoree.annotation.*;
import org.kevoree.extra.vlcj.VLCNativeLibraryLoader;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;

import java.awt.*;
import java.awt.image.BufferedImage;*/

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import org.kevoree.annotation.*;
import org.kevoree.extra.vlcj.VLCNativeLibraryLoader;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/10/11
 * Time: 07:53
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@MessageTypes({
        @MessageType(name = "BufferedImage", elems = {@MsgElem(name = "image", className = BufferedImage.class)})
})
@Requires({
		@RequiredPort(name = "image", type = PortType.MESSAGE, optional = true, messageType = "BufferedImage")
})
@DictionaryType({
		@DictionaryAttribute(name = "DEVICE", defaultValue = "v4l2:///dev/video0", vals = {"v4l2:///dev/video0","qtcapture://"}),
		@DictionaryAttribute(name = "LOG", defaultValue = "NONE", vals = {"NONE", "DEBUG"}, optional = false),
		@DictionaryAttribute(name = "FORMAT", defaultValue = "800x600",vals = {"1280x1024","1024x768","800x600", "640x480", "400x300", "200x150"})
})
@Library(name = "JavaSE")
@ComponentType
public class Webcam extends AbstractComponentType {

	private MediaPlayerFactory factory;
	private DirectMediaPlayer mediaPlayer;
	private static NativeLibrary instance;
	private static int nbComponent;

	@Start
	public void start () throws Exception {
		if (isPortBinded("image")) {
			if (instance == null) {
				String path = VLCNativeLibraryLoader.configure();
				//NativeLibrary.addSearchPath("vlccore", path);
				//NativeLibrary.getInstance("vlccore");
				NativeLibrary.addSearchPath("vlc", path);
				instance = NativeLibrary.getInstance("vlc");
				nbComponent++;
				Native.register(LibVlc.class, instance);
			}

			System.setProperty("vlcj.check", "no");
			System.setProperty("vlcj.log", (String) this.getDictionary().get("LOG"));
			String device = (String) this.getDictionary().get("DEVICE");
			factory = new MediaPlayerFactory("--no-video-title-show");
			mediaPlayer = factory
					.newDirectMediaPlayer(getWidth(), getHeight(), new OwnRenderCallback(getWidth(), getHeight()));

            mediaPlayer.playMedia(device, null);
		}

	}

	@Stop
	public void stop () {
		if (isPortBinded("image")) {
			mediaPlayer.stop();
			factory.release();
			if (instance != null && nbComponent == 0) {
				Native.unregister(LibVlc.class);
				instance.dispose();
			}
		}
	}

	@Update
	public void update () throws Exception {
		stop();
		start();
	}

	private int getHeight () {
		try {
			String format = (String) this.getDictionary().get("FORMAT");
			String[] values = format.split("x");
			return Integer.parseInt(values[1]);
		} catch (Exception e) {
			return 600;
		}
	}

	private int getWidth () {
		try {
			String format = (String) this.getDictionary().get("FORMAT");
			String[] values = format.split("x");
			return Integer.parseInt(values[0]);
		} catch (Exception e) {
			return 800;
		}
	}

	private final class OwnRenderCallback extends RenderCallbackAdapter {

		private final BufferedImage image;
		private int width;
		private int height;

		public OwnRenderCallback (int width, int height) {
			super(new int[width * height]);
			image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
					.createCompatibleImage(width, height);
			this.width = width;
			this.height = height;
		}

		@Override
		public void onDisplay (int[] data) {
			// The image data could be manipulated here...
			image.setRGB(0, 0, width, height, data, 0, width);
			getPortByName("image", MessagePort.class).process(image);
		}
	}
}
