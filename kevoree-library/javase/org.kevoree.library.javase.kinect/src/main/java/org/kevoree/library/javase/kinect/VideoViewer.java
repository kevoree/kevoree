package org.kevoree.library.javase.kinect;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/08/11
 * Time: 10:29
 */
@MessageTypes({
        @MessageType(name = "BufferedImage", elems = {@MsgElem(name = "image", className = BufferedImage.class)})
})
@Provides({
		@ProvidedPort(name = "image", type = PortType.MESSAGE, messageType ="BufferedImage")
})
@Library(name = "JavaSE")
@ComponentType
public class VideoViewer extends AbstractComponentType {
	private static final Logger logger = LoggerFactory.getLogger(VideoViewer.class);

	private boolean isAlreadyInitialized;
	private BufferStrategy bufferStrategy;
	private JFrame frame;
	private int width;
	private int height;

	@Start
	public void start () {
		isAlreadyInitialized = false;
	}

	@Stop
	public void stop () {
		if (isAlreadyInitialized) {
			frame.dispose();
			bufferStrategy.dispose();
		}
	}


	@Update
	public void update () {
		stop();
		start();
	}

	private void init () {
		frame.createBufferStrategy(2);
		bufferStrategy = frame.getBufferStrategy();
	}

	@Port(name = "image")
	public void onReceiveImage (Object message) {
//		logger.debug("image received by " + this.getName());
		if (message instanceof BufferedImage) {
			final BufferedImage image = (BufferedImage) message;
			if (!isAlreadyInitialized) {
				frame = new JFrame(this.getName());
				frame.setVisible(true);
				init();
				isAlreadyInitialized = true;
			}
			if (width != ((BufferedImage) message).getWidth(null) || height != ((BufferedImage) message).getHeight(null)) {
				width = ((BufferedImage) message).getWidth(null);
				height = ((BufferedImage) message).getHeight(null);
				frame.setSize(width, height);
			}
			try {

				Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
				g.drawImage(image, 0, 0, image.getWidth(null), image.getHeight(null), null);
				g.dispose();
				bufferStrategy.show();
			} catch (Exception e) {
				logger.debug("Something wrong appears, maybe the viewer fails", e);
			}

		}
	}
}
