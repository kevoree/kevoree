package org.kevoree.library.javase.kinect;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import org.openkinect.freenect.*;
import org.openkinect.freenect.util.Jdk14LogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/08/11
 * Time: 17:42
 */
@Requires({
		@RequiredPort(name = "image", type = PortType.MESSAGE, optional = true,
				filter = "java.awt.image.BufferedImage")//,
		//@RequiredPort(name = "raw", type = PortType.MESSAGE, optional = true, filter = "java.nio.ByteBuffer")//,
		//@RequiredPort(name = "imageDepth", type = PortType.MESSAGE, optional = true)
})
@Provides({
		@ProvidedPort(name = "motor", type = PortType.MESSAGE, filter = {"java.lang.Integer", "java.lang.String"})//
		//@ProvidedPort(name = "led", type = PortType.MESSAGE, filter = {"java.lang.Integer", "java.lang.String"}) TODO
		//@ProvidedPort(name = "log", type = PortType.MESSAGE, filter = {"java.lang.Integer", "java.lang.String"}) TODO
})
@DictionaryType({
		@DictionaryAttribute(name = "FORMAT", defaultValue = "RGB", optional = true,
				vals = {"DEPTH_11BIT", "DEPTH_10BIT", "DEPTH_11BIT_PACKED", "DEPTH_10BIT_PACKED", "RGB",
						"IR_8BIT", "IR_10BIT", "IR_10BIT_PACKED", "BAYER", "YUV_RGB", "YUV_RAW"}),
		@DictionaryAttribute(name = "FPS", defaultValue = "30", optional = true, vals = {"1", "10", "15", "24", "30"}),
		@DictionaryAttribute(name = "LOG_LEVEL", defaultValue = "ERROR", optional = true,
				vals = {"ERROR", "DEBUG", "INFO", "ALL"})
})
@Library(name = "JavaSE")
@ComponentType
public class Kinect extends AbstractComponentType {

	private Context ctx;
	private Device dev;

	private static final Logger logger = LoggerFactory.getLogger(Kinect.class);

	@Start
	public void start () {
		ctx = Freenect.createContext();
		ctx.setLogHandler(new Jdk14LogHandler());
		ctx.setLogLevel(getLogLevel());
		if (ctx.numDevices() > 0) { // TODO replace the numDevice (here 0) by a parameter value
			dev = ctx.openDevice(0);
			dev.setLed(LedStatus.GREEN);
			if (isDepth()) {
				dev.setDepthFormat(buildDepthFormat());
				dev.startDepth(new DepthHandler() {
					BufferedImage image;
					long lastTimeStamp = 0;
					long delay = getDelay();

					@Override
					public synchronized void onFrameReceived (DepthFormat format, ByteBuffer frame, int timestamp) {
						if (lastTimeStamp + delay < System.currentTimeMillis()) {
							/*if (isPortBinded("raw")) {
								Map<String, Object> dic = new HashMap<String, Object>();
								dic.put("frame", frame);
								dic.put("format", format);
								getPortByName("raw", MessagePort.class).process(dic);
							}*/
							if (isPortBinded("image")) {
								// FIXME the cost is too high
								//int[] pixels = new int[format.getWidth() * format.getHeight()];
								if (image == null) {
									image = DirectBufferedImage
											.getDirectImageRGB(format.getWidth(), format.getHeight());
								}
								//Graphics2D graphics = (Graphics2D)image.getGraphics();
								for (int y = 0; y < format.getHeight(); y++) {
									for (int x = 0; x < format.getWidth(); x++) {
										// FIXME is not totally good
										int offset = 2 * (y * format.getWidth() + x);

										short d0 = frame.get(offset);
										short d1 = frame.get(offset + 1);

										int pixel = d1 << 8 | d0;

										//pixel = depth2intensity(pixel);
										//int pixel = depth2rgb(d);
										//int pixel = buffer.get(offset);
										image.setRGB(x, y, pixel);
										// TODO use raster like the VideoHandler
									}
								}
								image.getGraphics().dispose();
								//image.setRGB(0, 0, format.getWidth(), format.getHeight(), pixels, 0, format.getWidth());
								getPortByName("image", MessagePort.class).process(image);
							}
							lastTimeStamp = System.currentTimeMillis();
						}
						frame.position(0);
					}
				});
			} else {
				dev.setVideoFormat(buildVideoFormat());
				dev.startVideo(new VideoHandler() {
					BufferedImage image;
					WritableRaster raster;
					long lastTimeStamp = 0;
					long delay = getDelay();
					int[] bgr = new int[3];

					@Override
					public synchronized void onFrameReceived (VideoFormat format, ByteBuffer frame, int timestamp) {
						// timestamp is not a timestamp like the one returned by System.currentTimeMillis()
						if (lastTimeStamp + delay < System.currentTimeMillis()) {
							/*if (isPortBinded("raw")) {
								Map<String, Object> dic = new HashMap<String, Object>();
								dic.put("frame", frame);
								dic.put("format", format);
								getPortByName("raw", MessagePort.class).process(dic);
							}*/
							if (isPortBinded("image")) {
								if (image == null) {
									image = new BufferedImage(format.getWidth(), format.getHeight(),
											BufferedImage.TYPE_3BYTE_BGR);
									raster = image.getRaster();
								}
								for (int y = 0; y < format.getHeight(); y++) {
									for (int x = 0; x < format.getWidth(); x++) {
										int offset = 3 * (y * format.getWidth() + x);
										bgr[2] = frame.get(offset + 2) & 0xFF;
										bgr[1] = frame.get(offset + 1) & 0xFF;
										bgr[0] = frame.get(offset) & 0xFF;

										raster.setPixel(x, y, bgr);
									}
								}
								image.getGraphics().dispose();

								getPortByName("image", MessagePort.class).process(image);
							}
							lastTimeStamp = System.currentTimeMillis();
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
		dev.setLed(LedStatus.OFF);
		if (dev != null) {
			if (isDepth()) {
				dev.stopDepth();
			} else {
				dev.stopVideo();
			}
		}
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
	public void onReceiveMessage (Object message) {
		int percentage = 50;
		if (message instanceof Integer) {
			percentage = (Integer) message;
			move(percentage);
		} else if (message instanceof String) {
			if (message.toString().startsWith("percent=")) {
				percentage = Integer.parseInt(message.toString().replace("percent=", ""));
			} else {
				percentage = Integer.parseInt((String) message);
			}
			move(percentage);
		} else {
			logger.info("message received has an unknown type !");
			// TODO log
		}
	}

	private LogLevel getLogLevel () {
		String logLevelAttribute = (String) this.getDictionary().get("LOG_LEVEL");
		if (logLevelAttribute.equals("ERROR")) {
			return LogLevel.ERROR;
		} else if (logLevelAttribute.equals("DEBUG")) {
			return LogLevel.DEBUG;
		} else if (logLevelAttribute.equals("INFO")) {
			return LogLevel.INFO;
		} else if (logLevelAttribute.equals("ALL")) {
			return LogLevel.SPEW;
		} else {
			return LogLevel.ERROR;
		}
	}

	private boolean isDepth () {
		String format = (String) this.getDictionary().get("FORMAT");
		return format != null && format.toLowerCase().contains("depth");
	}

	private DepthFormat buildDepthFormat () {
		String format = (String) this.getDictionary().get("FORMAT");
		if (format.equalsIgnoreCase("DEPTH_11BIT")) {
			return DepthFormat.D11BIT;
		} else if (format.equalsIgnoreCase("DEPTH_10BIT")) {
			return DepthFormat.D10BIT;
		} else if (format.equalsIgnoreCase("DEPTH_11BIT_PACKED")) {
			return DepthFormat.D11BIT_PACKED;
		} else if (format.equalsIgnoreCase("DEPTH_10BIT_PACKED")) {
			return DepthFormat.D10BIT_PACKED;
		} else {
			return DepthFormat.D11BIT;
		}
	}

	private VideoFormat buildVideoFormat () {
		String format = (String) this.getDictionary().get("FORMAT");
		if (format.equalsIgnoreCase("RGB")) {
			return VideoFormat.RGB;
		} else if (format.equalsIgnoreCase("IR_8BIT")) {
			return VideoFormat.IR_8BIT;
		} else if (format.equalsIgnoreCase("IR_10BIT")) {
			return VideoFormat.IR_10BIT;
		} else if (format.equalsIgnoreCase("IR_10BIT_PACKED")) {
			return VideoFormat.IR_10BIT_PACKED;
		} else if (format.equalsIgnoreCase("BAYER")) {
			return VideoFormat.BAYER;
		} else if (format.equalsIgnoreCase("YUV_RGB")) {
			return VideoFormat.YUV_RGB;
		} else if (format.equalsIgnoreCase("YUV_RAW")) {
			return VideoFormat.YUV_RAW;
		} else {
			return VideoFormat.RGB;
		}
	}

	private int getDelay () {
		String fps = (String) this.getDictionary().get("FPS");
		int delay = 0;
		if (!fps.equals("max")) {
			try {
				delay = Integer.parseInt(fps);
				delay = 1000 / delay;
			} catch (NumberFormatException e) {
				logger.warn("FPS attribute must be an int or \"max\"!");
			}
		}
		return delay;
	}

	private Image translateImage (VideoFormat format, ByteBuffer frame) {
		// TODO translate the various format
		return null;
	}

	private Image translateImage (DepthFormat format, ByteBuffer frame) {
		// TODO translate the various format
		return null;
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
