package org.kevoree.platform.standalone;

import org.kevoree.kcl.api.FlexyClassLoader;
import org.kevoree.log.Log;
import org.kevoree.microkernel.KevoreeKernel;
import org.kevoree.microkernel.impl.KevoreeMicroKernelImpl;

import java.util.SortedSet;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/11/2013
 * Time: 11:27
 */
public class App {

    public static void main(String[] args) throws Exception {

        String profile = "mvn:org.kevoree:org.kevoree.bootstrap:{kevoree.version}";
        if (System.getProperty("kev.boot") != null) {
            String kprofile = System.getProperty("kev.boot");
            if (kprofile.equals("test")) {
                profile = "mvn:org.kevoree:org.kevoree.bootstrap.test:{kevoree.version}";
            } else {
                if (kprofile.equals("telemetry")) {
                    profile = "mvn:org.kevoree:org.kevoree.bootstrap.telemetry:{kevoree.version}";
                } else {
                    profile = kprofile;
                }
            }
        }
        KevoreeKernel kernel = new KevoreeMicroKernelImpl();
        String version = System.getProperty("version");
        if (version == null) {
            version = System.getProperty("kevoree.version");
        }
        if (version == null) {
            String[] profiles = profile.split(":");
            SortedSet<String> sets = kernel.getResolver().listVersion(profiles[1], profiles[2], "jar", kernel.getReleaseURLS());
            for (String s : sets) {
                if (!s.toLowerCase().contains("snapshot")) {
                    version = s;
                    break;
                }
            }
        }
        String bootJar = profile.replace("{kevoree.version}", version);
        Log.info("Kevoree bootstrap from " + bootJar);

        FlexyClassLoader bootstrapKCL = kernel.install(bootJar, bootJar);
        kernel.boot(bootstrapKCL.getResourceAsStream("KEV-INF/bootinfo"));
    }

}
