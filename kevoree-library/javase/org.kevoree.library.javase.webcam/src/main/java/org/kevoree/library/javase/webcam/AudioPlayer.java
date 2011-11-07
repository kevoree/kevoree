package org.kevoree.library.javase.webcam;

import org.kevoree.annotation.*;
import org.kevoree.extra.vlcj.VLCNativeLibraryLoader;
import org.kevoree.framework.AbstractComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/11/11
 * Time: 00:11
 * To change this template use File | Settings | File Templates.
 */
@Provides({
        @ProvidedPort(name = "media", type = PortType.MESSAGE)
})
@Library(name = "JavaSE")
@ComponentType
public class AudioPlayer extends AbstractComponentType {

    HeadlessMediaPlayer mediaPlayer = null;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Start
    public void start() throws Exception {
        mediaPlayer = MediaPlayerHelper.getInstance().getFactory(this.getName()).newHeadlessMediaPlayer();
    }

    @Stop
    public void stop() throws Exception {
        mediaPlayer.stop();
        MediaPlayerHelper.getInstance().releaseKey(this.getName());
    }

    @Port(name = "media")
    public void triggerMedia(final Object o) {
        if (o != null) {
            logger.debug("Run media mrl=" + o.toString());
            mediaPlayer.playMedia(o.toString());
        }
    }


}
