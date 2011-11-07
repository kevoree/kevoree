package org.kevoree.library.javase.javacv;

import com.googlecode.javacpp.Loader;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

import java.awt.image.BufferedImage;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/08/11
 * Time: 14:55
 */
@MessageTypes({
        @MessageType(name = "BufferedImage", elems = {@MsgElem(name = "image", className = BufferedImage.class)})
})
@Provides({
		@ProvidedPort(name = "image", type = PortType.MESSAGE, messageType = "BufferedImage")
})
@Requires({
		@RequiredPort(name = "image_diff", type = PortType.MESSAGE, filter = "java.awt.image.BufferedImage",
				optional = true, needCheckDependency = false),
		@RequiredPort(name = "movement", type = PortType.MESSAGE, filter = "", optional = true,
				needCheckDependency = false)
})
@Library(name = "JavaSE")
@ComponentType
public class MotionDetector extends AbstractComponentType {

	private boolean isAlreadyInitialized;
	private IplImage frame;
	private IplImage prevImage;
	private IplImage image;
	private IplImage diff;
	private CvMemStorage storage;

	@Start
	public void start () {
		isAlreadyInitialized = false;
	}

	@Stop
	public void stop () {
		if (isAlreadyInitialized) {
			if (diff != null) {
				diff.release();
			}
			if (image != null) {
				image.release();
			}
			if (prevImage != null) {
				prevImage.release();
			}
			if (frame != null) {
				frame.release();
			}
			if (storage != null) {
				cvReleaseMemStorage(storage);
			}
		}
		diff = null;
		image = null;
		prevImage = null;
		frame = null;
		storage = null;
		isAlreadyInitialized = false;
	}

	@Update
	public void update () {
		stop();
		start();
	}

	@Port(name = "image")
	public void onReceiveImage (Object message) {
		if (message instanceof BufferedImage) {
			if (isPortBinded("movement") || isPortBinded("image_diff")) {
				if (!isAlreadyInitialized) {
					isAlreadyInitialized = true;
					frame = IplImage.create(((BufferedImage) message).getWidth(), ((BufferedImage) message).getHeight(),
							IPL_DEPTH_8U, 3);
					diff = IplImage.create(((BufferedImage) message).getWidth(), ((BufferedImage) message).getHeight(),
							IPL_DEPTH_8U, 1);
					image = IplImage.create(((BufferedImage) message).getWidth(), ((BufferedImage) message).getHeight(),
							IPL_DEPTH_8U, 1);
					prevImage = IplImage
							.create(((BufferedImage) message).getWidth(), ((BufferedImage) message).getHeight(),
									IPL_DEPTH_8U, 1);

					storage = CvMemStorage.create();
				}
				frame.copyFrom((BufferedImage) message);
				process();
			}
		}
	}

	private void process () {

		cvSmooth(frame, frame, CV_GAUSSIAN, 9, 9, 2, 2);
		cvCvtColor(frame, image, CV_RGB2GRAY);

		if (prevImage != null) {
			// perform ABS difference
			cvAbsDiff(image, prevImage, diff);
			// do some threshold for wipe away useless details
			cvThreshold(diff, diff, 70, 255, CV_THRESH_BINARY);

			//canvasFrame.showImage(diff);
			CvSeq contour = new CvSeq(null);
			// recognize contours
			cvFindContours(diff, storage, contour, Loader.sizeof(CvContour.class), CV_RETR_LIST,
					CV_CHAIN_APPROX_SIMPLE);

			while (contour != null && !contour.isNull()) {
				if (contour.elem_size() > 0) {
					CvBox2D box = cvMinAreaRect2(contour, storage);
					// test intersection
					if (box != null) {
						if (isPortBinded("movement")) {
							getPortByName("movement", MessagePort.class).process("");
						}
						if (isPortBinded("image_diff")) {
							getPortByName("image_diff", MessagePort.class).process(diff.getBufferedImage());
						}

						break;
					}
				}
				contour = contour.h_next();
			}
		}
		if (prevImage != null) {
			prevImage.release();
			prevImage = null;
		}
		prevImage = image.clone();
	}
}
