package org.kevoree.library.javase.motion;

import com.googlecode.javacpp.BytePointer;
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.openkinect.freenect.Freenect;
import org.openkinect.freenect.VideoFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Map;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/08/11
 * Time: 14:55
 */
@Provides({
		@ProvidedPort(name = "image", type = PortType.MESSAGE, filter = "java.awt.image.BufferedImage"),
		@ProvidedPort(name = "kinect_raw", type = PortType.MESSAGE, filter = "java.nio.ByteBuffer")
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

	private CanvasFrame canvasFrame;

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

				canvasFrame = new CanvasFrame("Some Title");
				// TODO to remove and add a port to send the image result
				canvasFrame.setCanvasSize(((Image) message).getWidth(null), ((Image) message).getHeight(null));
			}

			opencv_core.IplImage frame = opencv_core.IplImage.createFrom((BufferedImage) message);
			process(frame);

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

	@Port(name = "kinect_raw")
	public void onReceiveImageFromKinect (Object message) {
		if (message instanceof Map) {
			if (((Map) message).get("frame") != null && ((Map) message).get("frame") instanceof ByteBuffer
					&& ((Map) message).get("format") != null && ((Map) message).get("format") instanceof VideoFormat) {
				if (!isAlreadyInitialized) {
					/*frame = new JFrame(this.getName());
				 frame.setSize(((Image) message).getWidth(null), ((Image) message).getHeight(null));
				 frame.setVisible(true);*/
					//final Canvas canvas = new KCanvasDepth();
					//f.add(canvas, BorderLayout.CENTER);
					//init();
					isAlreadyInitialized = true;

					canvasFrame = new CanvasFrame(this.getName());
					// TODO to remove and add a port to send the image result
					canvasFrame.setCanvasSize(((VideoFormat)((Map) message).get("format")).getWidth(), ((VideoFormat)((Map) message).get("format")).getHeight());
				}
				opencv_core.IplImage frame = loadImageFromKinect((ByteBuffer) ((Map) message).get("frame"),
						(VideoFormat) ((Map) message).get("format"));

				process(frame);

			}
		}
	}

	private IplImage loadImageFromKinect (ByteBuffer frame, VideoFormat format) {
		int iplDepth;
		int channels;
		IplImage rawImage = null, grayImage = null;
		switch (format.getFrameSize()) {
			case Freenect.FREENECT_VIDEO_RGB_SIZE:
				iplDepth = IPL_DEPTH_8U;
				channels = 3;
				break;
			case Freenect.FREENECT_VIDEO_BAYER_SIZE:
			case Freenect.FREENECT_VIDEO_IR_8BIT_SIZE:
				iplDepth = IPL_DEPTH_8U;
				channels = 1;
				break;
			case Freenect.FREENECT_VIDEO_IR_10BIT_SIZE:
				iplDepth = IPL_DEPTH_16U;
				channels = 1;
				break;
			//case Freenect.FREENECT_VIDEO_YUV_RGB:  iplDepth = IPL_DEPTH_8U; channels = 3; break;
			case Freenect.FREENECT_VIDEO_YUV_SIZE:
				iplDepth = IPL_DEPTH_8U;
				channels = 2;
				break;
			case Freenect.FREENECT_VIDEO_IR_10BIT_PACKED_SIZE:
			default:
				iplDepth = IPL_DEPTH_8U;
				channels = 3;
		}
		//if (rawImage == null || rawImage.width() != format.getWidth() || rawImage.height() != format.getHeight()) {
		rawImage = IplImage.createHeader(format.getWidth(), format.getHeight(), iplDepth, channels);
		//}
		BytePointer pointer = new BytePointer();
		pointer.capacity(frame.limit());
		frame.clear();
		pointer.asByteBuffer().put(frame);
		cvSetData(rawImage, pointer, format.getWidth() * channels * iplDepth / 8);


		if (iplDepth > 8 && ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
			// ack, the camera's endianness doesn't correspond to our machine ...
			// swap bytes of 16-bit images
			ByteBuffer bb = rawImage.getByteBuffer();
			ShortBuffer in = bb.order(ByteOrder.BIG_ENDIAN).asShortBuffer();
			ShortBuffer out = bb.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
			out.put(in);
		}

		/*if (colorMode == FrameGrabber.ColorMode.GRAY && channels == 3) {
					grayImage = IplImage.create(format.getWidth(), format.getHeight(), iplDepth, 1);
					cvCvtColor(rawImage, grayImage, CV_RGB2GRAY);
					//grayImage.timestamp = timestamp[0];
					return grayImage;
				} else if (colorMode == FrameGrabber.ColorMode.BGR && channels == 3) {*/
		cvCvtColor(rawImage, rawImage, CV_RGB2BGR);
		//rawImage.timestamp = timestamp[0];
		return rawImage;
		/*} else {
					//rawImage.timestamp = timestamp[0];
					return rawImage;
				}*/
	}

	private void process (IplImage frame) {
		IplImage image;
		//opencv_core.IplImage prevImage = null;
		IplImage diff;


		CvMemStorage storage = CvMemStorage.create();

//			while (canvasFrame.isVisible() && (frame = grabber.grab()) != null) {
		cvSmooth(frame, frame, CV_GAUSSIAN, 9, 9, 2, 2);
//				if (image == null) {
		image = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
		cvCvtColor(frame, image, CV_RGB2GRAY);
//				} else {
//					prevImage = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
//					prevImage = image;
//					image = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
//					cvCvtColor(frame, image, CV_RGB2GRAY);
//				}

//				if (diff == null) {
		diff = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
//				}

		if (prevImage != null) {
			// perform ABS difference
			cvAbsDiff(image, prevImage, diff);
			// do some threshold for wipe away useless details
			cvThreshold(diff, diff, 64, 255, CV_THRESH_BINARY);

			canvasFrame.showImage(diff);

			// recognize contours
			CvSeq contour = new CvSeq(null);
			cvFindContours(diff, storage, contour, Loader.sizeof(CvContour.class), CV_RETR_LIST,
					CV_CHAIN_APPROX_SIMPLE);

			while (contour != null && !contour.isNull()) {
				if (contour.elem_size() > 0) {
					CvBox2D box = cvMinAreaRect2(contour, storage);
					// test intersection
					if (box != null) {
						CvPoint2D32f center = box.center();
						CvSize2D32f size = box.size();
						//System.out.println(System.currentTimeMillis());
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
		}

		prevImage = image;
	}
}
