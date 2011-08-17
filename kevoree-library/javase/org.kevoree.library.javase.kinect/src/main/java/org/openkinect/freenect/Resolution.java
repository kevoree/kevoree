package org.openkinect.freenect;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/08/11
 * Time: 13:48
 */
public enum Resolution {
	FREENECT_RESOLUTION_LOW    (0),
	FREENECT_RESOLUTION_MEDIUM (1),
	FREENECT_RESOLUTION_HIGH   (2),
	FREENECT_RESOLUTION_DUMMY  (2147483647);

	private int value;

	Resolution (int value) {
		this.value = value;
	}

	public int getValue () {
		return value;
	}
}

/*typedef enum {
	FREENECT_RESOLUTION_LOW    = 0, *//**< QVGA - 320x240 *//*
	FREENECT_RESOLUTION_MEDIUM = 1, *//**< VGA  - 640x480 *//*
	FREENECT_RESOLUTION_HIGH   = 2, *//**< SXGA - 1280x1024 *//*
	FREENECT_RESOLUTION_DUMMY  = 2147483647, *//**< Dummy value to force enum to be 32 bits wide *//*
} freenect_resolution;*/
