package org.kevoree.library.ui.fileExplorer;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/11/11
 * Time: 17:52
 * To change this template use File | Settings | File Templates.
 */

import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

/**
 * A very simple file monitor, that will monitor a directory tree and trigger
 * actions, when it detects changes. Only directory trees, that actually
 * contain files matching a filter criteria are included.
 */
public class FileMonitor implements Runnable {

  private File root;
  private SimpleFileManager simpleFileManager;
  private Vector<Runnable>clients = new Vector<Runnable>();

  /**
   * Construct a new monitor, monitoring a specific directory tree
   * @param root root of the directory tree to monitor
   * @param smf the <code>SimpleFileManager</code> to notify
   */
  public FileMonitor(File root, SimpleFileManager smf) {
    this.root=root;
    simpleFileManager = smf;
  }

  /**
   * Descend recursively into a directory tree and return the most recent
   * modifaction date.
   * @param dir the directory to descend into
   * @returnThe most recent timestamp found.
   */
  private long descend(File dir, int depth) {
    File[] lst =  dir.listFiles();
    long lastMod = dir.lastModified();
    long test= lastMod;

    for (int i=0;i<lst.length;i++) {
      if (depth>0 &&lst[i].canRead()) {
        if (lst[i].isDirectory()) {
          test = descend(lst[i],depth-1);
        }
        else {
          test = lst[i].lastModified();
        }
      }
      if (test>lastMod) lastMod=test;
    }
    return lastMod;
  }

  /**
   * Add a client to the notification list
   * @param r a Runnable, that will be submitted to
   * <code>EventQueue.invokeAndWait()</code> once a change in the
   * directory tree is detected.
   */
  public synchronized void addClient(Runnable r) {
    clients.add(r);
  }

  // Interface implementation: Runnable

  /** {inheritdoc} */
  public void run() {
    long lastMod=0;
    while(true) {
      try {
        long tmp = descend(root,SimpleFileManager.MAXDEPTH);
        if (tmp>lastMod) {
          simpleFileManager.refresh();
          Runnable[] r = clients.toArray(new Runnable[0]);
          for (int i=0;i<r.length;i++) {
            EventQueue.invokeAndWait(r[i]);
          }
          lastMod=tmp;
        }
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {
        // Goodbye
        return;
      }
      catch (InvocationTargetException e) {
        //?!
        return;
      }
    }
  }

}
