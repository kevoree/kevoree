package org.kevoree.library.javase.webcam;

import org.kevoree.annotation.*;
import org.kevoree.extra.vlcj.VLCNativeLibraryLoader;
import org.kevoree.framework.AbstractComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.DefaultFullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.FullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.mac.MacVideoSurfaceAdapter;
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
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Start
    public void start() throws Exception {
        frame = new JFrame("Kevoree Frame");
        DefaultFullScreenStrategy full = new DefaultFullScreenStrategy(frame);
        mediaPlayer = MediaPlayerHelper.getInstance().getFactory(this.getName()).newEmbeddedMediaPlayer(new DefaultFullScreenStrategy(frame));
        Canvas c = new Canvas();
        c.setBackground(Color.black);
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(c, BorderLayout.CENTER);
        frame.setContentPane(p);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);
        mediaPlayer.setVideoSurface(MediaPlayerHelper.getInstance().getFactory(this.getName()).newVideoSurface(c));
        //mediaPlayer.setFullScreen(true);
    }

    @Stop
    public void stop() throws Exception {
        mediaPlayer.stop();
        frame.dispose();
        MediaPlayerHelper.getInstance().releaseKey(this.getName());
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
