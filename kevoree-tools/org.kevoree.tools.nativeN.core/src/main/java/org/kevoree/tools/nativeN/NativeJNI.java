package org.kevoree.tools.nativeN;



import org.kevoree.tools.nativeN.api.NativeEventPort;
import org.kevoree.tools.nativeN.utils.FileManager;

import java.io.*;
import java.util.EventObject;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 01/08/12
 * Time: 16:27
 *
 */
public class NativeJNI extends AbstractNativeJNI implements NativeEventPort {

    protected native int init(int key,int port_event);
    protected native boolean register();
    protected native int start(int key,String path);
    public native int stop(int key);
    protected native int update(int key);
    protected native int create_input(int key,String name);
    protected native int create_output(int key,String name);
    protected native int enqueue(int key,int port,String msg);


    private NativeManager handler=null;

    public NativeJNI(NativeManager obj)
    {
        super(obj);
        this.handler =obj;
    }

    public void dispatchEvent(String queue,String evt)
    {
        handler.fireEvent(this,queue,evt);
    }

    public String configureCL()
    {
        try
        {
            File folder = new File(System.getProperty("java.io.tmpdir") + File.separator + "native");
            if (folder.exists())
            {
                FileManager.deleteOldFile(folder);
            }
            folder.mkdirs();
            // todo
            String r = ""+new Random().nextInt(800);
            String absolutePath = FileManager.copyFileFromStream(FileManager.getPath("native.so"), folder.getAbsolutePath(),"libnative"+r+""+ FileManager.getExtension());

            System.load(absolutePath);

            return absolutePath;
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }


    @Override
    public String getMessage() {
        return "todo";
    }
}
