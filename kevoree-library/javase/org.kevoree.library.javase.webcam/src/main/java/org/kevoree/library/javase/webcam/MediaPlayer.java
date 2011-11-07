package org.kevoree.library.javase.webcam;

import org.kevoree.annotation.*;
import org.kevoree.extra.vlcj.VLCNativeLibraryLoader;
import org.kevoree.framework.AbstractComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.DefaultFullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.FullScreenStrategy;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/11/11
 * Time: 21:06
 * To change this template use File | Settings | File Templates.
 */
@Provides({
        @ProvidedPort(name = "media", type = PortType.MESSAGE)
})
@Library(name = "JavaSE")
@ComponentType
public class MediaPlayer extends AbstractComponentType {

    private JFrame frame;
    private EmbeddedMediaPlayer mediaPlayer;
    private MediaPlayerFactory mediaFactory;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Start
    public void start() throws Exception {
        VLCNativeLibraryLoader.initialize();
        System.setProperty("vlcj.check", "no");
        String[] options = {"--no-video-title-show", "--vout=macosx"};
        mediaFactory = new MediaPlayerFactory(options);
        frame = new JFrame("Kevoree Media Player");
        //Canvas vs = new Canvas();
        //frame.add(vs, BorderLayout.CENTER);
        frame.setVisible(true);

        FullScreenStrategy fullScreenStrategy = new DefaultFullScreenStrategy(frame);
        mediaPlayer = mediaFactory.newMediaPlayer(fullScreenStrategy);

        
        //mediaPlayer.setVideoSurface(vs);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);
    }

    @Stop
    public void stop() throws Exception {
        mediaPlayer.stop();
        frame.setVisible(false);
        frame.dispose();
        mediaPlayer.release();
        mediaFactory.release();
        VLCNativeLibraryLoader.release();
    }

    @Update
    public void update() throws Exception {

    }

    @Port(name = "media")
    public void triggerMedia(final Object o) {
        if (o != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    logger.debug("Run media mrl=" + o.toString());
                    mediaPlayer.playMedia(o.toString());
                }
            });
        }
    }

}
