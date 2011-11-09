package org.kevoree.library.ui.player;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.message.StdKevoreeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/08/11
 * Time: 10:29
 */
@MessageTypes({
		@MessageType(name = "BufferedImage", elems = {@MsgElem(name = "image", className = BufferedImage.class)}),
		@MessageType(name = "bytes", elems =
				{@MsgElem(name = "bytes", className = byte[].class),
						@MsgElem(name = "width", className = Integer.class),
						@MsgElem(name = "height", className = Integer.class),
						@MsgElem(name = "chroma", className = String.class),
						@MsgElem(name = "fps", className = Integer.class)}
		)
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

	byte[] bytes;

	@Port(name = "image_bytes")
	public void onReceiveImageBytes (Object message) {
		if (!isAlreadyInitialized) {
			init();
		}
		if (message instanceof int[]) {
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
				g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
				g.dispose();
				bufferStrategy.show();
			} catch (Exception e) {
				logger.debug("Something wrong appears, maybe the viewer fails", e);
			}
		}

		if (message instanceof StdKevoreeMessage) {
			StdKevoreeMessage msg = new StdKevoreeMessage();
			if (bytes == null) {
				bytes = new byte[((byte[]) msg.getValue("bytes").get()).length];
			}
			System.arraycopy((byte[]) msg.getValue("bytes").get(), 0, bytes, 0, bytes.length);
			height = (Integer) msg.getValue("height").get();
			width = (Integer) msg.getValue("width").get();
			/*chroma = msg.getValue("chroma").get();
			fps = msg.getValue("fps").get();*/

			ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			try {
				image = ImageIO.read(in);
				Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
				g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
				g.dispose();
				bufferStrategy.show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
