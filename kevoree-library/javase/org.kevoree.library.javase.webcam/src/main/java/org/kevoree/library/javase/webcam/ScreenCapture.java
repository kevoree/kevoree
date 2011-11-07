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

import org.kevoree.annotation.*;
import org.kevoree.extra.vlcj.VLCNativeLibraryLoader;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
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
		@MessageType(name = "BufferedImage", elems = {@MsgElem(name = "image", className = BufferedImage.class)}),
		@MessageType(name = "bytes", elems = {@MsgElem(name = "image", className = int[].class)})
})
@Requires({
		@RequiredPort(name = "image", type = PortType.MESSAGE, optional = true, messageType = "BufferedImage"),
		@RequiredPort(name = "image_bytes", type = PortType.MESSAGE, optional = true, messageType = "bytes")
})
@DictionaryType({
		@DictionaryAttribute(name = "LOG", defaultValue = "NONE", vals = {"NONE", "DEBUG"}, optional = false),
		@DictionaryAttribute(name = "FORMAT", defaultValue = "800x600",
				vals = {"1280x1024", "1024x768", "800x600", "640x480", "400x300", "200x150"})
})
@Library(name = "JavaSE")
@ComponentType
public class ScreenCapture extends AbstractComponentType {

	private DirectMediaPlayer mediaPlayer;
	private static final String MRL = "screen://";
//	private static final String SOUT = "";//:sout=#transcode{vcodec=FLV1,vb=%d,scale=%f}";
	private static final String FPS = ":screen-fps=%d";
	private static final String CACHING = ":screen-caching=%d";
	private static final int fps = 20;
	private static final int caching = 500;
//	private static final int bits = 4096;
//	private static final float scale = 0.5f;

	@Start
	public void start () throws Exception {
		VLCNativeLibraryLoader.initialize();
		System.setProperty("vlcj.check", "no");
		System.setProperty("vlcj.log", (String) this.getDictionary().get("LOG"));
		mediaPlayer = MediaPlayerHelper.getInstance().getFactory(this.getName())
				.newDirectMediaPlayer(getWidth(), getHeight(), new OwnRenderCallback(getWidth(), getHeight()));
		mediaPlayer.playMedia(MRL, getMediaOptions());
	}

	@Stop
	public void stop () {
		mediaPlayer.stop();
		MediaPlayerHelper.getInstance().releaseKey(this.getName());
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

	private String[] getMediaOptions () {
		return new String[]{
				// String.format(SOUT, bits, scale),
				String.format(FPS, fps),
				String.format(CACHING, caching)
		};
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
			if (isPortBinded("image")) {
				image.setRGB(0, 0, width, height, data, 0, width);
				getPortByName("image", MessagePort.class).process(image);
			}

			if (isPortBinded("image_bytes")) {
				int[] newData = new int[data.length + 2];
				newData[0] = width;
				newData[1] = height;
				System.arraycopy(data, 0, newData, 2, data.length);
				getPortByName("image_bytes", MessagePort.class).process(newData);
			}
		}


	}
}
