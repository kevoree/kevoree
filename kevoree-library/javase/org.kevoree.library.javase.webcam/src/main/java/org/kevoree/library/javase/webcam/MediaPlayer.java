package org.kevoree.library.javase.webcam;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import org.kevoree.annotation.*;
import org.kevoree.extra.vlcj.VLCNativeLibraryLoader;
import org.kevoree.framework.AbstractComponentType;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;

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

    EmbeddedMediaPlayerComponent mediaPlayerComponent = null;
    JFrame frame = new JFrame();

    @Start
    public void start() throws Exception {
        /*
        if (instance == null) {
            String path = VLCNativeLibraryLoader.configure();
            //NativeLibrary.addSearchPath("vlccore", path);
            //NativeLibrary.getInstance("vlccore");
            NativeLibrary.addSearchPath("vlc", path);
            instance = NativeLibrary.getInstance("vlc");
            nbComponent++;
            Native.register(LibVlc.class, instance);
        } */

        System.setProperty("vlcj.check", "no");
        System.setProperty("vlcj.log", (String) this.getDictionary().get("LOG"));


        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        frame = new JFrame();
        frame.setContentPane(mediaPlayerComponent);
        frame.setLocation(100, 100);
        frame.setSize(1050, 600);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);
    }

    @Stop
    public void stop() throws Exception {
        mediaPlayerComponent.getMediaPlayer().stop();
        frame.setVisible(false);
        frame.dispose();
        frame = null;
    }

    @Update
    public void update() throws Exception {

    }

    @Port(name = "media")
    public void triggerMedia(Object o) {
        if (o != null) {
            mediaPlayerComponent.getMediaPlayer().playMedia(o.toString());
        }
    }

}
