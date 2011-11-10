package org.kevoree.library.javase.vlc;

import org.kevoree.extra.vlcj.VLCNativeLibraryLoader;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/11/11
 * Time: 11:25
 * To change this template use File | Settings | File Templates.
 */
public class MediaPlayerHelper {

	private static MediaPlayerHelper ourInstance = new MediaPlayerHelper();

	public static MediaPlayerHelper getInstance () {
		return ourInstance;
	}

	private MediaPlayerHelper () {

	}

	private static String[] DEFAULT_FACTORY_ARGUMENTS = {
//            "--vout=macosx",
			"--no-video-title-show",
			"--no-plugins-cache",
			"--quiet",
			"--quiet-synchro",
//            "--intf",
//            "dummy"
	};

	private List<String> keys = new ArrayList<String>();
	private MediaPlayerFactory mediaPlayerFactory = null;

	public MediaPlayerFactory getFactory (String key) {
		if (keys.isEmpty()) {
			System.setProperty("vlcj.check", "no");
			VLCNativeLibraryLoader.initialize();
			mediaPlayerFactory = new MediaPlayerFactory(DEFAULT_FACTORY_ARGUMENTS);
		}
		keys.add(key);
		return mediaPlayerFactory;
	}

	public void releaseKey (String key) {
		keys.remove(key);
		if (keys.isEmpty()) {
			mediaPlayerFactory.release();
			VLCNativeLibraryLoader.release();
			mediaPlayerFactory = null;
		}
	}


}
