package org.kevoree.library.javase.webcam;

import org.kevoree.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/11/11
 * Time: 00:11
 */
@Library(name = "JavaSE")
@ComponentType
public class AudioPlayer extends Player {

	@Start
	public void start () throws Exception {
		mediaPlayer = MediaPlayerHelper.getInstance().getFactory(this.getName()).newHeadlessMediaPlayer();
	}

	@Stop
	public void stop () throws Exception {
		mediaPlayer.stop();
		MediaPlayerHelper.getInstance().releaseKey(this.getName());
	}
}
