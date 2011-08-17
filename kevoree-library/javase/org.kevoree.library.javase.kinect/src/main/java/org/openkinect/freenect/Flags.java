package org.openkinect.freenect;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/08/11
 * Time: 13:40
 */
public enum Flags {
	FREENECT_DEVICE_MOTOR  (0x01),
	FREENECT_DEVICE_CAMERA (0x02),
	FREENECT_DEVICE_AUDIO  (0x04),;

	private int value;

	Flags (int value) {
		this.value = value;
	}

	public int getValue () {
		return value;
	}
}
/*typedef enum {
	FREENECT_DEVICE_MOTOR  = 0x01,
	FREENECT_DEVICE_CAMERA = 0x02,
	FREENECT_DEVICE_AUDIO  = 0x04,
} freenect_device_flags;*/
