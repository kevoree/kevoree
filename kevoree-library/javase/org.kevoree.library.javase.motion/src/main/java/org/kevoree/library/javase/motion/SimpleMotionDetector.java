package org.kevoree.library.javase.motion;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/08/11
 * Time: 14:55
 */
@Provides({
		@ProvidedPort(name = "image", type = PortType.MESSAGE, filter = "java.awt.image.BufferedImage")
})
@Library(name = "JavaSE")
@ComponentType
public class SimpleMotionDetector extends AbstractComponentType {

	private boolean isAlreadyInitialized;
	private BufferStrategy bufferStrategy;
	private JFrame frame;

	private IplImage prevImage;

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
				frame.setSize(((Image) message).getWidth(null), ((Image) message).getHeight(null));
				frame.setVisible(true);
				//final Canvas canvas = new KCanvasDepth();
				//f.add(canvas, BorderLayout.CENTER);
				init();
				isAlreadyInitialized = true;
			}
			/*OpenKinectFrameGrabber grabber = new OpenKinectFrameGrabber(0);
			grabber.start();*/

			opencv_core.IplImage frame = opencv_core.IplImage.createFrom((BufferedImage) message);
			opencv_core.IplImage image;
			//opencv_core.IplImage prevImage = null;
			opencv_core.IplImage diff;

			CanvasFrame canvasFrame = new CanvasFrame("Some Title"); // TODO to remove and add a port to send the image result
			canvasFrame.setCanvasSize(frame.width(), frame.height());

			opencv_core.CvMemStorage storage = opencv_core.CvMemStorage.create();

//			while (canvasFrame.isVisible() && (frame = grabber.grab()) != null) {
				cvSmooth(frame, frame, CV_GAUSSIAN, 9, 9, 2, 2);
//				if (image == null) {
					image = opencv_core.IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
					cvCvtColor(frame, image, CV_RGB2GRAY);
//				} else {
//					prevImage = opencv_core.IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
//					prevImage = image;
//					image = opencv_core.IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
//					cvCvtColor(frame, image, CV_RGB2GRAY);
//				}

//				if (diff == null) {
					diff = opencv_core.IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
//				}

				if (prevImage != null) {
					// perform ABS difference
					cvAbsDiff(image, prevImage, diff);
					// do some threshold for wipe away useless details
					cvThreshold(diff, diff, 64, 255, CV_THRESH_BINARY);

					canvasFrame.showImage(diff);

					// recognize contours
					opencv_core.CvSeq contour = new opencv_core.CvSeq(null);
					cvFindContours(diff, storage, contour, Loader.sizeof(opencv_core.CvContour.class), CV_RETR_LIST,
							CV_CHAIN_APPROX_SIMPLE);

					while (contour != null && !contour.isNull()) {
						if (contour.elem_size() > 0) {
							opencv_core.CvBox2D box = cvMinAreaRect2(contour, storage);
							// test intersection
							if (box != null) {
								opencv_core.CvPoint2D32f center = box.center();
								opencv_core.CvSize2D32f size = box.size();
								System.out.println(System.currentTimeMillis());
/*                            for (int i = 0; i < sa.length; i++) {
                                if ((Math.abs(center.x - (sa[i].offsetX + sa[i].width / 2))) < ((size.width / 2) + (sa[i].width / 2)) &&
                                    (Math.abs(center.y - (sa[i].offsetY + sa[i].height / 2))) < ((size.height / 2) + (sa[i].height / 2))) {

                                    if (!alarmedZones.containsKey(i)) {
                                        alarmedZones.put(i, true);
                                        activeAlarms.put(i, 1);
                                    } else {
                                        activeAlarms.remove(i);
                                        activeAlarms.put(i, 1);
                                    }
                                    System.out.println("Motion Detected in the area no: " + i +
                                            " Located at points: (" + sa[i].x + ", " + sa[i].y+ ") -"
                                            + " (" + (sa[i].x +sa[i].width) + ", "
                                            + (sa[i].y+sa[i].height) + ")");
                                }
                            }
*/
							}
						}
						contour = contour.h_next();
					}
					prevImage = image;
				}
			//}
			/*grabber.stop();
			canvasFrame.dispose();*/


			/*if (!isAlreadyInitialized) {
				frame = new JFrame(this.getName());
				frame.setSize(((Image) message).getWidth(null), ((Image) message).getHeight(null));
				frame.setVisible(true);
				//final Canvas canvas = new KCanvasDepth();
				//f.add(canvas, BorderLayout.CENTER);
				init();
				isAlreadyInitialized = true;
			}
			Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
			g.drawImage((Image) message, 0, 0, ((Image) message).getWidth(null), ((Image) message).getHeight(null),
					null);
			g.dispose();
			bufferStrategy.show();*/
		}
	}
}
