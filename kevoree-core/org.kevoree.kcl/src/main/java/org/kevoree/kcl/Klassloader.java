package org.kevoree.kcl;

import java.io.InputStream;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 19/08/13
 * Time: 12:04
 */
public abstract class Klassloader extends ClassLoader {

    public abstract void addJarFromStream(InputStream child);

    public abstract void addChild(Klassloader child);

    public abstract void addJarFromURL(URL child);

    public abstract void removeChild(Klassloader child);

    public abstract void isolateFromSystem();

}
