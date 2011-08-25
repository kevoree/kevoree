package org.kevoree.library.javase.motion;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

import java.awt.*;
import java.awt.image.BufferedImage;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/08/11
 * Time: 14:55
 */
@Provides({
		@ProvidedPort(name = "image", type = PortType.MESSAGE, filter = "java.awt.image.BufferedImage")/*,
		@ProvidedPort(name = "kinect_raw", type = PortType.MESSAGE, filter = "java.nio.ByteBuffer")*/
})
@Requires({
		@RequiredPort(name = "movement", type = PortType.MESSAGE, filter = "org.kevoree.library.javase.motion.Movement",
				optional = true, needCheckDependency = false)
})
@DictionaryType({
		@DictionaryAttribute(name = "PRECISION", optional = false, vals = {"0.01", "0.02", "0.1", "0.2"},
				defaultValue = "0.01")
})
@Library(name = "JavaSE")
@ComponentType
public class SimpleMotionDetector extends AbstractComponentType {

	private boolean isAlreadyInitialized;
	//private BufferStrategy bufferStrategy;
	//private JFrame frame;
	private IplImage frame;
	private IplImage prevImage;
	private IplImage image;
	private IplImage diff;
	private CvMemStorage storage;
	private CanvasFrame canvasFrame;

	private CvPoint2D32f prevCenter = null;

	private Movement[] move;
	private int index = 0;

	private double precision;

	@Start
	public void start () {
		isAlreadyInitialized = false;
		move = new Movement[10];
	}

	@Stop
	public void stop () {
		if (isAlreadyInitialized) {
//			frame.dispose();
//			bufferStrategy.dispose();
			canvasFrame.dispose();
			/*cvReleaseImage(diff);
			cvReleaseImage(image);
			cvReleaseImage(prevImage);
			cvReleaseImage(frame);
			cvReleaseMemStorage(storage);*/
			diff.release();
			image.release();
			prevImage.release();
			frame.release();
			cvReleaseMemStorage(storage);
		}
		isAlreadyInitialized = false;
		precision = 0;
	}

	@Update
	public void update () {
		stop();
		start();
	}

	@Port(name = "image")
	public void onReceiveImage (Object message) {
		if (message instanceof BufferedImage) {
//			System.out.println("image received");
			if (!isAlreadyInitialized) {
				isAlreadyInitialized = true;

				diff = IplImage.create(((BufferedImage) message).getWidth(), ((BufferedImage) message).getHeight(),
						IPL_DEPTH_8U, 1);
				image = IplImage.create(((BufferedImage) message).getWidth(), ((BufferedImage) message).getHeight(),
						IPL_DEPTH_8U, 1);
				prevImage = IplImage.create(((BufferedImage) message).getWidth(), ((BufferedImage) message).getHeight(),
						IPL_DEPTH_8U, 1);

				storage = CvMemStorage.create();


				// TODO remove and add a port to send the image result
				canvasFrame = new CanvasFrame(this.getName());
				canvasFrame.setCanvasSize(((Image) message).getWidth(null), ((Image) message).getHeight(null));
				//System.out.println("SimpleMotionDetector initialized!");
			}

			if (frame == null) {
				frame = IplImage.createFrom((BufferedImage) message);
			} else {
				frame.copyFrom((BufferedImage) message);
			}
			process(frame);
			//cvReleaseImage(frame);
		}
	}

	private void process (IplImage frame) {

		cvSmooth(frame, frame, CV_GAUSSIAN, 9, 9, 2, 2);
		cvCvtColor(frame, image, CV_RGB2GRAY);

		CvPoint2D32f center = null;
		CvSize2D32f size = null;
		if (prevImage != null) {
			// perform ABS difference
			cvAbsDiff(image, prevImage, diff);
			// do some threshold for wipe away useless details
			cvThreshold(diff, diff, 64, 255, CV_THRESH_BINARY);

			canvasFrame.showImage(diff);
			CvSeq contour = new CvSeq(null);
			// recognize contours
			cvFindContours(diff, storage, contour, Loader.sizeof(CvContour.class), CV_RETR_LIST,
					CV_CHAIN_APPROX_SIMPLE);

			while (contour != null && !contour.isNull()) {
				if (contour.elem_size() > 0) {
					CvBox2D box = cvMinAreaRect2(contour, storage);
					// test intersection
					if (box != null) {
						if (center == null || size.height() < box.size().height() && size.width() < box.size()
								.width()) {
							center = box.center();
							size = box.size();
						}
					}
				}
				contour = contour.h_next();
			}
		}
		if (prevCenter != null && center != null) {
//			System.out.println("x: " + prevCenter.x() + "\t" + center.x());
//			System.out.println("y: " + prevCenter.y() + "\t" + center.y());
			float diffX = center.x() - prevCenter.x();
			float diffY = center.y() - prevCenter.y();
			if (diffX > diffY) {
				if (center.x() > prevCenter.x() + getPrecision()) {
//					System.out.println("vers la droite");
					move[index % move.length] = Movement.MOVE_TO_RIGHT;
				} else if (center.x() < prevCenter.x() - getPrecision()) {
//					System.out.println("vers la gauche");
					move[index % move.length] = Movement.MOVE_TO_LEFT;
				}
			} else {
				if (center.y() > prevCenter.y() + getPrecision()) {
//					System.out.println("vers le bas");
					move[index % move.length] = Movement.MOVE_UP;
				} else if (center.y() < prevCenter.y() - getPrecision()) {
//					System.out.println("vers le haut");
					move[index % move.length] = Movement.MOVE_DOWN;
				}
			}
			index++;
			if (index == move.length) {
				System.out.println("send value ...");
				index = 0;
				sendMovement();
			}
		}
		prevCenter = center;
		cvReleaseImage(prevImage);
		prevImage = image.clone();
	}

	private void sendMovement () {
		if (isPortBinded("movement")) {
			int nbLeft = 0;
			int nbUp = 0;
			int nbRight = 0;
			int nbDown = 0;
			for (Movement value : move) {
				if (value == Movement.MOVE_TO_LEFT) {
					nbLeft++;
				} else if (value == Movement.MOVE_UP) {
					nbUp++;
				} else if (value == Movement.MOVE_TO_RIGHT) {
					nbRight++;
				} else if (value == Movement.MOVE_DOWN) {
					nbDown++;
				}
			}
			if (nbLeft >= move.length / 2) {
				getPortByName("movement", MessagePort.class).process(Movement.MOVE_TO_LEFT);
			} else if (nbUp >= move.length / 2) {
				getPortByName("movement", MessagePort.class).process(Movement.MOVE_UP);
			} else if (nbRight >= move.length / 2) {
				getPortByName("movement", MessagePort.class).process(Movement.MOVE_TO_RIGHT);
			} else if (nbDown >= move.length / 2) {
				getPortByName("movement", MessagePort.class).process(Movement.MOVE_DOWN);
			} else {
				getPortByName("movement", MessagePort.class).process(Movement.UNDEFINED);
			}
		}
	}


	private double getPrecision () {
		if (precision == 0) {
			String precisionValue = (String) this.getDictionary().get("precision");
			try {
				precision = Integer.parseInt(precisionValue);
			} catch (NumberFormatException e) {
				precision = 0.01;
			}
		}
		return precision;
	}
}
