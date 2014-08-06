package org.kevoree.tools.annotator.test;

import org.junit.Test;
import org.kevoree.*;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.tools.annotator.Annotations2Model;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.Assert.*;


/**
 * Created by duke on 28/02/2014.
 */
public class AnnotatorTest {

    private Annotations2Model a2m = new Annotations2Model();

    @Test
    public void test() throws Exception {
        KevoreeFactory factory = new DefaultKevoreeFactory();
        ContainerRoot model = factory.createContainerRoot();
        DeployUnit fakeDU = factory.createDeployUnit();
        fakeDU.setName("nameDU");
        fakeDU.setGroupName("groupDU");
        fakeDU.setVersion("1.0");
        model.addDeployUnits(fakeDU);
        Path unzipDir = Files.createTempDirectory("kevTest");
        ZipUtility.unzip(getClass().getClassLoader().getResourceAsStream("helloworld.zip"), unzipDir.toFile().getAbsolutePath());
        ArrayList<String> urls = new ArrayList<String>();
        urls.add(unzipDir.toFile().getAbsolutePath());

        a2m.fillModel(unzipDir.toFile(), model, fakeDU, urls);

        assertEquals(1, model.getTypeDefinitions().size());
        assertEquals("org.kevoree.ComponentType", model.getTypeDefinitions().get(0).metaClassName());

        assertEquals("time", model.getTypeDefinitions().get(0).getDictionaryType().findAttributesByID("time").getName());
        assertEquals("2000", model.getTypeDefinitions().get(0).getDictionaryType().findAttributesByID("time").getDefaultValue());
        assertEquals(true, model.getTypeDefinitions().get(0).getDictionaryType().findAttributesByID("time").getOptional());
        assertEquals(false, model.getTypeDefinitions().get(0).getDictionaryType().findAttributesByID("time").getFragmentDependant());
        assertEquals("java.lang.Long", model.getTypeDefinitions().get(0).getDictionaryType().findAttributesByID("time").getDatatype());
        assertEquals(fakeDU.path(), model.getTypeDefinitions().get(0).getDeployUnit().path());

        assertEquals(1, ((ComponentType) model.getTypeDefinitions().get(0)).getProvided().size());
        assertEquals("conso", ((ComponentType) model.getTypeDefinitions().get(0)).getProvided().get(0).getName());
        assertEquals(true, ((ComponentType) model.getTypeDefinitions().get(0)).getProvided().get(0).getOptional());

        //JSONModelSerializer saver = new JSONModelSerializer();
        //saver.serializeToStream(model, System.out);

        deleteFolder(unzipDir.toFile());
    }

    @Test
    public void testProdCons() throws Exception {
        KevoreeFactory factory = new DefaultKevoreeFactory();
        ContainerRoot model = factory.createContainerRoot();
        DeployUnit fakeDU = factory.createDeployUnit();
        fakeDU.setName("nameDU");
        fakeDU.setGroupName("groupDU");
        fakeDU.setVersion("1.0");
        model.addDeployUnits(fakeDU);
        Path unzipDir = Files.createTempDirectory("kevTest");
        ZipUtility.unzip(getClass().getClassLoader().getResourceAsStream("prodcons.zip"), unzipDir.toFile().getAbsolutePath());
        ArrayList<String> urls = new ArrayList<String>();
        urls.add(unzipDir.toFile().getAbsolutePath());

        a2m.fillModel(unzipDir.toFile(), model, fakeDU, urls);

        assertEquals(4, model.getTypeDefinitions().size());

        for (TypeDefinition td : model.getTypeDefinitions()) {
            if (td.getName().equals("FrameComponent")) {
                assertEquals(1, ((ComponentType) td).getRequired().size());
                assertEquals("textEntered", ((ComponentType) td).getRequired().get(0).getName());
                assertEquals(true, ((ComponentType) td).getRequired().get(0).getOptional());
            }
        }

        //JSONModelSerializer saver = new JSONModelSerializer();
        //saver.serializeToStream(model, System.out);

        deleteFolder(unzipDir.toFile());
    }


    @Test
    public void testLighlxc() throws Exception {
        KevoreeFactory factory = new DefaultKevoreeFactory();
        ContainerRoot model = factory.createContainerRoot();
        DeployUnit fakeDU = factory.createDeployUnit();
        fakeDU.setName("nameDU");
        fakeDU.setGroupName("groupDU");
        fakeDU.setVersion("1.0");
        model.addDeployUnits(fakeDU);
        Path unzipDir = Files.createTempDirectory("kevTest");
        ZipUtility.unzip(getClass().getClassLoader().getResourceAsStream("lighlxc.zip"), unzipDir.toFile().getAbsolutePath());
        Path unzipDir2 = Files.createTempDirectory("kevTest2");
        ZipUtility.unzip(getClass().getClassLoader().getResourceAsStream("org.kevoree.library.cloud.api-3.4.1.zip"), unzipDir2.toFile().getAbsolutePath());
        Path unzipDir3 = Files.createTempDirectory("kevTest3");
        ZipUtility.unzip(getClass().getClassLoader().getResourceAsStream("org.kevoree.library.java.javaNode-3.4.2-SNAPSHOT.zip"), unzipDir3.toFile().getAbsolutePath());
        ArrayList<String> urls = new ArrayList<String>();
        urls.add(unzipDir.toFile().getAbsolutePath());
        urls.add(unzipDir2.toFile().getAbsolutePath());
        urls.add(unzipDir3.toFile().getAbsolutePath());
        a2m.fillModel(unzipDir.toFile(), model, fakeDU, urls);
        /*
        for(TypeDefinition td : model.getTypeDefinitions()){
           if(td.getName().equals("PlatformJavaNode")){
               assertEquals("3.4.1",td.getVersion());
           }
        }
        */
        deleteFolder(unzipDir.toFile());
        deleteFolder(unzipDir2.toFile());
        deleteFolder(unzipDir3.toFile());
    }

    @Test
    public void testLighlxc2() throws Exception {
        KevoreeFactory factory = new DefaultKevoreeFactory();
        ContainerRoot model = factory.createContainerRoot();
        DeployUnit fakeDU = factory.createDeployUnit();
        fakeDU.setName("nameDU");
        fakeDU.setGroupName("groupDU");
        fakeDU.setVersion("1.0");
        model.addDeployUnits(fakeDU);
        Path unzipDir = Files.createTempDirectory("kevTest");
        ZipUtility.unzip(getClass().getClassLoader().getResourceAsStream("lighlxc.zip"), unzipDir.toFile().getAbsolutePath());
        Path unzipDir2 = Files.createTempDirectory("kevTest2");
        ZipUtility.unzip(getClass().getClassLoader().getResourceAsStream("org.kevoree.library.cloud.api-3.4.2-SNAPSHOT.zip"), unzipDir2.toFile().getAbsolutePath());
        Path unzipDir3 = Files.createTempDirectory("kevTest3");
        ZipUtility.unzip(getClass().getClassLoader().getResourceAsStream("org.kevoree.library.java.javaNode-3.4.2-SNAPSHOT.zip"), unzipDir3.toFile().getAbsolutePath());
        ArrayList<String> urls = new ArrayList<String>();
        urls.add(unzipDir.toFile().getAbsolutePath());
        urls.add(unzipDir2.toFile().getAbsolutePath());
        urls.add(unzipDir3.toFile().getAbsolutePath());
        a2m.fillModel(unzipDir.toFile(), model, fakeDU, urls);

        /*
        for(TypeDefinition td : model.getTypeDefinitions()){
            if(td.getName().equals("PlatformJavaNode")){
                assertEquals("3.4.2-SNAPSHOT",td.getVersion());
            }
        }
        */
        deleteFolder(unzipDir.toFile());
        deleteFolder(unzipDir2.toFile());
        deleteFolder(unzipDir3.toFile());
    }



    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

}
