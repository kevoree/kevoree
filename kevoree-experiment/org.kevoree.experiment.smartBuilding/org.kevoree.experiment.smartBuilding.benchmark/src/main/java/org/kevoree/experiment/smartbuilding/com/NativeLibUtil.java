/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.experiment.smartbuilding.com;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ffouquet
 */
public class NativeLibUtil {
    
    public static void standaloneRxTx() {
        System.out.println("Load RxTx");
        try {
            String osName = System.getProperty("os.name");
            String osProc = System.getProperty("os.arch");
            if (osName.equals("Mac OS X")) {
                NativeLibUtil.copyFile(NativeLibUtil.class.getClassLoader().getResourceAsStream("nativelib/Mac_OS_X/librxtxSerial.jnilib"), "librxtxSerial.jnilib");
            }
            
            if (osName.equals("Win32")) {
                NativeLibUtil.copyFile(NativeLibUtil.class.getClassLoader().getResourceAsStream("nativelib/Windows/win32/rxtxSerial.dll"), "rxtxSerial.dll");
            }
            if (osName.equals("Win64") || osName.equals("Windows 7")) {
                NativeLibUtil.copyFile(NativeLibUtil.class.getClassLoader().getResourceAsStream("nativelib/Windows/win64/rxtxSerial.dll"), "rxtxSerial.dll");
            }
            if (osName.equals("Linux") && osProc.equals("x86-64")) {
                NativeLibUtil.copyFile(NativeLibUtil.class.getClassLoader().getResourceAsStream("nativelib/Linux/x86_64-unknown-linux-gnu/librxtxSerial.so"), "librxtxSerial.so");
            }
            if (osName.equals("Linux") && osProc.equals("ia64")) {
                NativeLibUtil.copyFile(NativeLibUtil.class.getClassLoader().getResourceAsStream("nativelib/Linux/ia64-unknown-linux-gnu/librxtxSerial.so"), "librxtxSerial.so");
            }
            if (osName.equals("Linux") && osProc.equals("x86")) {
                NativeLibUtil.copyFile(NativeLibUtil.class.getClassLoader().getResourceAsStream("nativelib/Linux/i686-unknown-linux-gnu/librxtxParallel.so"), "librxtxParallel.so");
                NativeLibUtil.copyFile(NativeLibUtil.class.getClassLoader().getResourceAsStream("nativelib/Linux/i686-unknown-linux-gnu/librxtxSerial.so"), "librxtxSerial.so");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static void copyFile(InputStream in, String to) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(to);
            while (true) {
                int data = in.read();
                if (data == -1) {
                    break;
                }
                out.write(data);
            }
            in.close();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(NativeLibUtil.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Logger.getLogger(NativeLibUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(NativeLibUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
