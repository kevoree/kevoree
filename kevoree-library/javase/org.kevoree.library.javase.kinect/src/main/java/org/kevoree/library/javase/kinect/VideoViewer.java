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
		@MessageType(name = "BufferedImage", elems = {@MsgElem(name = "image", className = BufferedImage.class)}),
		@MessageType(name = "bytes", elems = {@MsgElem(name = "image", className = int[].class)})
})
@Provides({
		@ProvidedPort(name = "image", type = PortType.MESSAGE, messageType = "BufferedImage"),
		@ProvidedPort(name = "image_bytes", type = PortType.MESSAGE, messageType = "bytes")
})
@Library(name = "JavaSE")
@ComponentType
public class VideoViewer extends AbstractComponentType {
	private static final Logger logger = LoggerFactory.getLogger(VideoViewer.class);

	private boolean isAlreadyInitialized;
	private BufferStrategy bufferStrategy;
	private BufferedImage image;
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

		frame = new JFrame(this.getName());
		frame.setVisible(true);
		frame.createBufferStrategy(2);
		bufferStrategy = frame.getBufferStrategy();
		isAlreadyInitialized = true;
	}

	@Port(name = "image")
	public void onReceiveImage (Object message) {
		System.out.println(message);
		if (!isAlreadyInitialized) {
			init();
		}
		if (message instanceof BufferedImage) {
			image = (BufferedImage) message;
			if (!isAlreadyInitialized) {
				frame = new JFrame(this.getName());
				frame.setVisible(true);
				init();
				isAlreadyInitialized = true;
			}
			if (width != ((BufferedImage) message).getWidth(null) || height != ((BufferedImage) message)
					.getHeight(null)) {
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

	@Port(name = "image_bytes")
	public void onReceiveImageBytes (Object message) {
		if (!isAlreadyInitialized) {
			init();
		}
		if (message instanceof int[]) {
			System.out.println(((int[]) message).length);
			int imageWidth = ((int[]) message)[0];
			int imageHeight = ((int[]) message)[1];
			if (width != imageWidth || height != imageHeight) {
				width = imageWidth;
				height = imageHeight;
				frame.setSize(width, height);
				image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
						.getDefaultConfiguration()
						.createCompatibleImage(width, height);
			}
			try {
				image.setRGB(0, 0, width, height, ((int[]) message), 2, width);
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
