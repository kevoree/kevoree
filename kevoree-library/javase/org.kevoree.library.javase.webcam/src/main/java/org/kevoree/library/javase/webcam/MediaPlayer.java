package org.kevoree.library.javase.webcam;

import org.kevoree.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.embedded.DefaultFullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/11/11
 * Time: 21:06
 *
 */
@Library(name = "JavaSE")
@ComponentType
public class MediaPlayer extends Player {

    private JFrame frame;
    private EmbeddedMediaPlayer mediaPlayer;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Start
    public void start() throws Exception {
        frame = new JFrame("Kevoree Frame");
//        DefaultFullScreenStrategy full = new DefaultFullScreenStrategy(frame);
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

}
