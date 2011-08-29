package org.kevoree.library.javase.kinect;

import org.openkinect.freenect.*;
import org.openkinect.freenect.util.Jdk14LogHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 25/08/11
 * Time: 10:33
 */
public class VideoHandlerTest {

	//@Test
	public void videoHandling () throws InterruptedException {

		Context ctx = Freenect.createContext();
		ctx.setLogHandler(new Jdk14LogHandler());
		ctx.setLogLevel(LogLevel.INFO);
		if (ctx.numDevices() > 0) { // TODO replace the numDevice (here 0) by a parameter value
			Device dev = ctx.openDevice(0);
			dev.setVideoFormat(buildVideoFormat());
			dev.startVideo(new VideoHandler() {
				boolean isAlreadyInitialized;
				BufferStrategy bufferStrategy;
				JFrame jframe;
				BufferedImage image;
				WritableRaster raster;
				long lastTimeStamp = 0;
				long delay = 1000 / 10;
				int[] bgr = new int[3];

				@Override
				public synchronized void onFrameReceived (VideoFormat format, ByteBuffer frame, int timestamp) {
					// TODO try to reduce it again (the cost is too high)
					if (lastTimeStamp + delay < System.currentTimeMillis()) {
						System.out.println("data used ! ");
						if (image == null) {
							image = new BufferedImage(format.getWidth(), format.getHeight(),
									BufferedImage.TYPE_3BYTE_BGR);
							raster = image.getRaster();
							//viewer.start();
							jframe = new JFrame("Test");
							jframe.setSize(format.getWidth(), format.getHeight());
							jframe.setVisible(true);

							jframe.createBufferStrategy(2);
							bufferStrategy = jframe.getBufferStrategy();
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

						Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
						g.drawImage(image, 0, 0, image.getWidth(null),
								image.getHeight(null),
								null);
						g.dispose();
						bufferStrategy.show();

						lastTimeStamp = System.currentTimeMillis();
					}
				}
			});
		}
		while (true) {
			Thread.sleep(20000);
		}
	}

	private VideoFormat buildVideoFormat () {
		return VideoFormat.RGB;
	}
}
