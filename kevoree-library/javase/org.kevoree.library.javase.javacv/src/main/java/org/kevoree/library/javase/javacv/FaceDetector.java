package org.kevoree.library.javase.javacv;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.*;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 26/08/11
 * Time: 11:00
 */


@Provides({
		@ProvidedPort(name = "image", type = PortType.MESSAGE, filter = "java.awt.image.BufferedImage")
})
@Requires({
		@RequiredPort(name = "faces", type = PortType.MESSAGE, filter = "java.awt.image.BufferedImage",
				optional = true, needCheckDependency = false)
})
@DictionaryType({
		@DictionaryAttribute(name = "MULTI_FACE", optional = true, defaultValue = "false", vals = {"false", "true"})
})
@Library(name = "JavaSE")
@ComponentType
public class FaceDetector extends AbstractComponentType {
	private static Logger logger = LoggerFactory.getLogger(FaceDetector.class);

	private boolean isAlreadyInitialized;
	private IplImage frame;
	private IplImage grayImage;
	private IplImage equImg;
	private CvMemStorage storage;
	private CvHaarClassifierCascade cascade;

	private static final String CASCADE_FILE_NAME = "haarcascade_frontalface_alt.xml";
	private static final String CASCADE_FILE_PATH = "haarcascades";

	private String cascadeFilePath;

	@Start
	public void start () {
		isAlreadyInitialized = false;
		copyResources();
	}

	@Stop
	public void stop () {
		if (isAlreadyInitialized) {
			equImg.release();
			grayImage.release();
			frame.release();
			cvClearMemStorage(storage);
		}
		equImg = null;
		grayImage = null;
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
			if (isPortBinded("faces")) {
				getPortByName("faces", MessagePort.class).process(process((BufferedImage) message));
			}
		}
	}

	private BufferedImage process (BufferedImage bufferedImage) {
		if (!isAlreadyInitialized) {
			isAlreadyInitialized = true;

			// We instantiate a classifier cascade to be used for detection, using the cascade definition.
			cascade = new CvHaarClassifierCascade(cvLoad(cascadeFilePath));

			frame = IplImage
					.create(bufferedImage.getWidth(), bufferedImage.getHeight(),
							IPL_DEPTH_8U, 3);
			grayImage = IplImage
					.create(bufferedImage.getWidth(), bufferedImage.getHeight(),
							IPL_DEPTH_8U, 1);
			equImg = IplImage
					.create(bufferedImage.getWidth(), bufferedImage.getHeight(),
							IPL_DEPTH_8U, 1);

			storage = CvMemStorage.create();
		}
		frame.copyFrom(bufferedImage);
		// We convert the original image to grayscale.
		cvCvtColor(frame, grayImage, CV_BGR2GRAY);
		// equalize the grayscale
		cvEqualizeHist(grayImage, equImg);

		CvSeq faces;
		if (doMultiFaceDetection()) {
			// We detect the faces.
			faces = cvHaarDetectObjects(equImg, cascade, storage, 1.1, 1, 0);

			//We iterate over the discovered faces and draw yellow rectangles around them.
			for (int i = 0; i < faces.total(); i++) {
				CvRect r = new CvRect(cvGetSeqElem(faces, i));
				bufferedImage.getGraphics().drawRect(r.x(), r.y(), r.width(), r.height());
			}
		} else {
			// speed things up by searching for only a single, largest face subimage
			faces = cvHaarDetectObjects(equImg, cascade, storage, 1.1, 1,
					CV_HAAR_DO_ROUGH_SEARCH | CV_HAAR_FIND_BIGGEST_OBJECT);

			CvRect r = new CvRect(cvGetSeqElem(faces, 0));
			if (!r.isNull()) {
				bufferedImage.getGraphics().drawRect(r.x(), r.y(), r.width(), r.height());
			}
		}
		return bufferedImage;
	}

	private boolean doMultiFaceDetection () {
		return ((String) this.getDictionary().get("MULTI_FACE")).equalsIgnoreCase("true");
	}

	private void copyResources () {
		try {
			File folder = new File(System.getProperty("java.io.tmpdir") + File.separator + "javacv" + File.separator
					+ CASCADE_FILE_PATH);
			if (!folder.exists()) {
				folder.mkdirs();
			}
			cascadeFilePath = copyFileFromStream(CASCADE_FILE_NAME, CASCADE_FILE_PATH, folder);
		} catch (IOException e) {
			logger.error("unable to extract some files", e);
		}
	}

	private String copyFileFromStream (String fileName, String filePath, File folder) throws IOException {
		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream(filePath + "/" + fileName);
		if (inputStream != null) {
			File copy = new File(folder + File.separator + fileName);
			copy.deleteOnExit();
			OutputStream outputStream = new FileOutputStream(copy);
			byte[] bytes = new byte[1024];
			int length = inputStream.read(bytes);
			while (length > -1) {
				outputStream.write(bytes, 0, length);
				length = inputStream.read(bytes);
			}
			return folder.getAbsolutePath() + File.separator + fileName;
		}
		return null;
	}
}
