package org.kevoree.tools.annotator;

import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.KevoreeFactory;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.log.Log;
import org.kevoree.serializer.JSONModelSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by duke on 23/01/2014.
 */
public class App {

    public static void main(String[] args) throws Exception {

        if (args.length != 1 && args.length != 2) {
            throw new Exception("Bad number of argument ! " + args.length);
        } else {
            long before = System.currentTimeMillis();
            Annotations2Model annotations2Model = new Annotations2Model();
            KevoreeFactory factory = new DefaultKevoreeFactory();
            String directoryTargets = args[0];
            String[] directoryTargetList = directoryTargets.split(File.pathSeparator);
            for (String directoryTarget : directoryTargetList) {
                File directoryTargetFile = new File(directoryTarget);
                if (!(directoryTargetFile.exists() && directoryTargetFile.isDirectory())) {
                    //throw new Exception("Bad target dir argument !" + directoryTarget);
                } else {
                    ContainerRoot model = factory.createContainerRoot();
                    DeployUnit fakeDeployUnit = factory.createDeployUnit();
                    fakeDeployUnit.setGroupName("org.kevoree");
                    fakeDeployUnit.setName("org.kevoree.model");
                    fakeDeployUnit.setVersion(factory.getVersion());
                    model.addDeployUnits(fakeDeployUnit);
                    String[] classPath = System.getProperty("java.class.path").split(File.separator);
                    ArrayList<String> classPaths = new ArrayList<String>();
                    annotations2Model.fillModel(directoryTargetFile, model, fakeDeployUnit, classPaths);
                    //generate the file
                    if (model.getTypeDefinitions().size() > 0) {
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

            if (args.length == 2) {
                String subMain = args[1];
                Class subMainClazz = App.class.getClassLoader().loadClass(subMain);
                Method meth = subMainClazz.getMethod("main", String[].class);
                String[] params = new String[0];
                meth.invoke(null, (Object) params);
            }


        }
    }

}
