package org.kevoree.library.javase.kinect;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import org.openkinect.freenect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/08/11
 * Time: 17:42
 */
@Requires({
		@RequiredPort(name = "image", type = PortType.MESSAGE, optional = true, filter = "java.awt.image.BufferedImage")//,
		//@RequiredPort(name = "imageDepth", type = PortType.MESSAGE, optional = true)
})
@Provides({
		@ProvidedPort(name = "motor", type = PortType.MESSAGE, filter = {"java.lang.Integer", "java.lang.String"})//
		//@ProvidedPort(name = "led", type = PortType.MESSAGE, filter = {"java.lang.Integer", "java.lang.String"}) TODO
})
@DictionaryType({
		@DictionaryAttribute(name = "depth", defaultValue = "false", optional = true,vals={"false","true"})
})
@Library(name = "JavaSE")
@ComponentType
public class Kinect extends AbstractComponentType {

	private Context ctx;
	private Device dev;

	private Logger logger = LoggerFactory.getLogger(Kinect.class);

	@Start
	public void start () {
		ctx = Freenect.createContext();
		/*ctx.setLogHandler(new Jdk14LogHandler());
		ctx.setLogLevel(LogLevel.SPEW);*/
		if (ctx.numDevices() > 0) { // TODO replace the numDevice (here 0) by a parameter value
			dev = ctx.openDevice(0);
			if (getDictionary().get("depth").equals("true")) {
				dev.startDepth(new DepthHandler() {
					int frameCount = 0;
					BufferedImage image;

					@Override
					public synchronized void onFrameReceived (DepthFormat format, ByteBuffer frame, int timestamp) {
						if (isPortBinded("image")) {
							image = DirectBufferedImage.getDirectImageRGB(format.getWidth(), format.getHeight());
							for (int y = 0; y < format.getHeight(); y++) {
								for (int x = 0; x < format.getWidth(); x++) {
									int offset = 2 * (y * format.getWidth() + x);

									short d0 = frame.get(offset);
									short d1 = frame.get(offset + 1);

									int pixel = d1 << 8 | d0;

									//pixel = depth2intensity(pixel);
									//int pixel = depth2rgb(d);
									//int pixel = buffer.get(offset);
									image.setRGB(x, y, pixel);
								}
							}
							getPortByName("image", MessagePort.class).process(image);
						}
					}
				});
			} else {
				dev.startVideo(new VideoHandler() {
					int frameCount = 0;
					DirectBufferedImage image;

					@Override
					public synchronized void onFrameReceived (VideoFormat format, ByteBuffer frame, int timestamp) {
						if (isPortBinded("image")) {
							image = DirectBufferedImage.getDirectImageRGB(format.getWidth(), format.getHeight());
							for (int y = 0; y < format.getHeight(); y++) {
								for (int x = 0; x < format.getWidth(); x++) {
									int offset = 3 * (y * format.getWidth() + x);
									int r = frame.get(offset + 2) & 0xFF;
									int g = frame.get(offset + 1) & 0xFF;
									int b = frame.get(offset) & 0xFF;

									int pixel = (0xFF) << 24
											| (b & 0xFF) << 16
											| (g & 0xFF) << 8
											| (r & 0xFF);
									image.setRGB(x, y, pixel);

								}
							}
							getPortByName("image", MessagePort.class).process(image);
						}
					}
				});
			}
		} else {
			logger.info("Kinect not connected !");
			// TODO log
		}
	}

	@Stop
	public void stop () {
		if (ctx != null) {
			if (dev != null) {
				dev.close();
			}
			ctx.shutdown();
		}
	}

	@Update
	public void update () {
		stop();
		start();
	}

	@Port(name = "motor")
	public void onReceiveImage (Object message) {
		int percentage = 50;
		if (message instanceof Integer) {
			percentage = (Integer) message;
			move(percentage);
		} else if (message instanceof String) {
			percentage = Integer.parseInt((String) message);
			move(percentage);
		} else {
			logger.info("message received has an unknown type !");
			// TODO log
		}
	}

	private void move (int percentage) {
		if (percentage > 100) {
			percentage = 100;
		} else if (percentage < 0) {
			percentage = 0;
		}
		int angle = (int) ((percentage * ((float) 58 / 100)) - 29);
		dev.setTiltAngle(angle);
		while (dev.getTiltStatus() == TiltStatus.MOVING) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.debug("sleep the thread is not allowed here !");
			}
			dev.refreshTiltState();
		}
		dev.refreshTiltState();
		// TODO log
	}
}
