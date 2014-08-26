package org.kevoree.bootstrap.dev;

import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.api.helper.KModelHelper;
import org.kevoree.bootstrap.Bootstrap;
import org.kevoree.bootstrap.dev.annotator.Annotations2Model;
import org.kevoree.bootstrap.dev.annotator.MinimalPomParser;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.kcl.api.FlexyClassLoader;
import org.kevoree.log.Log;
import org.kevoree.microkernel.KevoreeKernel;
import org.kevoree.modeling.api.json.JSONModelSerializer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by duke on 8/14/14.
 */
public class BootstrapDev {

    public static void main(String[] ignore) throws Exception {

        //Log.set(Log.LEVEL_TRACE);

        String nodeName = System.getProperty("node.name");
        if (nodeName == null) {
            nodeName = Bootstrap.defaultNodeName;
        }

        KevoreeKernel kernel = KevoreeKernel.self.get();

        long before = System.currentTimeMillis();
        Annotations2Model annotations2Model = new Annotations2Model();
        KevoreeFactory factory = new DefaultKevoreeFactory();
        String directoryTargets = System.getProperty("dev.target.dirs");
        String[] directoryTargetList = directoryTargets.split(File.pathSeparator);
        for (String directoryTarget : directoryTargetList) {
            File directoryTargetFile = new File(directoryTarget);
            if (!(directoryTargetFile.exists() && directoryTargetFile.isDirectory())) {
                //throw new Exception("Bad target dir argument !" + directoryTarget);
            } else {
                ContainerRoot model = factory.createContainerRoot();
                factory.root(model);

                /*
                DeployUnit fakeDeployUnit = factory.createDeployUnit();
                fakeDeployUnit.setGroupName("org.kevoree");
                fakeDeployUnit.setName("org.kevoree.annotation.api");
                fakeDeployUnit.setVersion(factory.getVersion());
                model.addDeployUnits(fakeDeployUnit);
                */
                DeployUnit mainDu = MinimalPomParser.lookupLocalDeployUnit(directoryTargetFile, model, factory);
                String key = "mvn:" + KModelHelper.fqnGroup(mainDu) + ":" + mainDu.getName() + ":" + mainDu.getVersion();
                FlexyClassLoader kcl = kernel.install(key, "file:" + directoryTargetFile.getAbsolutePath());

                /*
                if (mainDu != null) {
                    model.addDeployUnits(mainDu);
                    mainDu.addRequiredLibs(fakeDeployUnit);
                } else {
                    mainDu = fakeDeployUnit;
                } */

                ArrayList<String> classPaths = new ArrayList<String>();
                classPaths.add(directoryTargetFile.getAbsolutePath());
                ClassLoader previous = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(kcl);
                annotations2Model.fillModel(directoryTargetFile, model, mainDu, classPaths);
                Thread.currentThread().setContextClassLoader(previous);

                //generate the file
                if (model.getPackages().size() > 0) {
                    File targetFile = new File(directoryTarget + File.separator + "KEV-INF" + File.separator + "lib.json");
                    JSONModelSerializer saver = new JSONModelSerializer();
                    targetFile.getParentFile().mkdirs();
                    targetFile.createNewFile();
                    FileOutputStream fop = new FileOutputStream(targetFile);
                    saver.serializeToStream(model, fop);
                    fop.flush();
                    fop.close();
                }
            }
        }
        long time = System.currentTimeMillis() - before;
        Log.info("Generation of KEV-INF/lib.json done in {} ms ", time);


        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final Bootstrap boot = new Bootstrap(kernel, nodeName);
        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {
            public void run() {
                try {
                    Thread.currentThread().setContextClassLoader(loader);
                    boot.stop();
                } catch (Throwable ex) {
                    System.out.println("Error stopping kevoree platform: " + ex.getMessage());
                }
            }
        });
        String bootstrapModel = System.getProperty("node.bootstrap");
        try {
            if (bootstrapModel != null) {
                boot.bootstrapFromFile(new File(bootstrapModel));
            } else {
                boot.bootstrapFromKevScript(new ByteArrayInputStream(System.getProperty("node.script").getBytes()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
