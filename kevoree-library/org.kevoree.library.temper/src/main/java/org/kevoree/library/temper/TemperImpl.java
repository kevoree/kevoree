package org.kevoree.library.temper;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface TemperImpl extends Library, TemperService{	
	
	TemperImpl INSTANCE = (TemperImpl) Native.loadLibrary(
            "temper", TemperImpl.class);

}
