package org.kevoree.library.javase.kinect;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/08/11
 * Time: 10:29
 */

@Provides({
		@ProvidedPort(name = "image", type = PortType.MESSAGE, filter = "java.awt.image.BufferedImage")
})
@Library(name = "JavaSE")
@ComponentType
public class VideoViewer extends AbstractComponentType {

	private boolean isAlreadyInitialized;
	private BufferStrategy bufferStrategy;
	private JFrame frame;

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
		if (message instanceof BufferedImage) {
			if (!isAlreadyInitialized) {
				frame = new JFrame(this.getName());
				frame.setSize(((BufferedImage) message).getWidth(null), ((BufferedImage) message).getHeight(null));
				frame.setVisible(true);
				//final Canvas canvas = new KCanvasDepth();
				//f.add(canvas, BorderLayout.CENTER);
				init();
				isAlreadyInitialized = true;
			}
			Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
			g.drawImage((Image) message, 0, 0, ((BufferedImage) message).getWidth(null), ((BufferedImage) message).getHeight(null),
					null);
			g.dispose();
			bufferStrategy.show();
		}
	}
}
