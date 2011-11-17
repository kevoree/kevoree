package org.kevoree.library.ui.player;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.message.StdKevoreeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

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
		if (message instanceof StdKevoreeMessage && !((StdKevoreeMessage) message).getValue("image").isEmpty()
				&& ((StdKevoreeMessage) message).getValue("image").get() instanceof BufferedImage) {
			image = (BufferedImage) ((StdKevoreeMessage) message).getValue("image").get();
			/*if (message instanceof BufferedImage) {
						image = (BufferedImage) message;*/
			if (!isAlreadyInitialized) {
				frame = new JFrame(this.getName());
				frame.setVisible(true);
				init();
				isAlreadyInitialized = true;
			}
			if (width != image.getWidth(null) || height != image.getHeight(null)) {
				width = image.getWidth(null);
				height = image.getHeight(null);
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
	DataBufferByte buffer;
	SampleModel sampleModel;
	Raster raster ;

	@Port(name = "image_bytes")
	public void onReceiveImageBytes (Object message) {
		try {
			if (!isAlreadyInitialized) {
				init();
			}
			if (message instanceof StdKevoreeMessage) {
				StdKevoreeMessage msg = (StdKevoreeMessage) message;
				height = (Integer) msg.getValue("height").get();
				width = (Integer) msg.getValue("width").get();
				bytes = ((byte[]) msg.getValue("bytes").get());

				if (image == null || width != image.getWidth(null) || height != image.getHeight(null)) {
					frame.setSize(width, height);
					image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
							.getDefaultConfiguration()
							.createCompatibleImage(width, height);


					sampleModel = new ComponentSampleModel(DataBuffer.TYPE_BYTE, width, height, 4, width * 4,
							new int[]{2, 1, 0});
//					buffer = new DataBufferByte(bytes, bytes.length);
//					bytes = new byte[((byte[]) msg.getValue("bytes").get()).length];
				}
				buffer = new DataBufferByte(bytes, bytes.length);
//				System.arraycopy(((byte[]) msg.getValue("bytes").get()), 0, bytes, 0, bytes.length);
				raster = Raster.createRaster(sampleModel, buffer, null);
				image.setData(raster);

				Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
				g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
				g.dispose();
				bufferStrategy.show();
			}
		} catch (Exception e) {
			logger.debug("Something wrong appears, maybe the viewer fails", e);
		}
	}
}
