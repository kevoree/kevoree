/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kermeta.emf.genmodel;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

//import org.eclipse.core.runtime.IPath;
//import org.eclipse.core.runtime.Path;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.emf.codegen.ecore.generator.Generator;
import org.eclipse.emf.codegen.ecore.generator.GeneratorAdapterFactory;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenModelFactory;
import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.codegen.ecore.genmodel.generator.GenBaseGeneratorAdapter;
import org.eclipse.emf.codegen.ecore.genmodel.generator.GenModelGeneratorAdapterFactory;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

/**
 *
 * @author ffouquet
 */
public class Util {

    private final static String outputfileName = "emfoutput";

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public static void createGenModel(File ecore, File genmodel, File sourcePath, Log log,Boolean bool) {
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().
                put("ecore", new org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl());
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().
                put("genmodel", new org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl());

        //IPath ecorePath = new Path(ecorepath);
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put("http://www.eclipse.org/emf/2002/GenModel", GenModelPackage.eINSTANCE);

        resourceSet.getURIConverter().getURIMap().putAll(EcorePlugin.computePlatformURIMap());
        URI ecoreURI = URI.createFileURI(ecore.getAbsolutePath());
        Resource resource = resourceSet.getResource(ecoreURI, true);
        EPackage ePackage = (EPackage) resource.getContents().get(0);

        GenModel genModelModel = null;

        if (genmodel != null && genmodel.exists()) {
            Resource resourceGenModel = resourceSet.getResource(URI.createFileURI(genmodel.getAbsolutePath()), true);
            genModelModel = (GenModel) resourceGenModel.getContents().get(0);
            genModelModel.setModelDirectory("/" + outputfileName);
            genModelModel.getForeignModel().add(ecore.getAbsolutePath());
            genModelModel.initialize(Collections.singleton(ePackage));

        } else {
            if(genmodel == null){
                genmodel = new File("outputfileName");
            }

            URI genModelURI = URI.createFileURI(genmodel.getAbsolutePath());

            System.out.println(genModelURI);

            Resource genModelResource = Resource.Factory.Registry.INSTANCE.getFactory(genModelURI).createResource(genModelURI);

            genModelModel = GenModelFactory.eINSTANCE.createGenModel();
            genModelResource.getContents().add(genModelModel);
            resourceSet.getResources().add(genModelResource);
            genModelModel.setModelDirectory("/" + outputfileName);
            genModelModel.getForeignModel().add(ecore.getAbsolutePath());
            genModelModel.initialize(Collections.singleton(ePackage));
            genModelModel.setModelName(genModelURI.trimFileExtension().lastSegment());

            try {
                genModelResource.save(Collections.EMPTY_MAP);
            } catch (IOException e) {
                log.debug("GenModelSave Error", e);
            }
        }

        if(bool) {
            log.info("Clear output directory , "+sourcePath.getAbsolutePath());
            deleteDirectory(sourcePath);
        }
        sourcePath.mkdir();
        EcorePlugin.getPlatformResourceMap().put(outputfileName, URI.createFileURI(sourcePath.getAbsolutePath() + "/"));
        Util.generate(genModelModel, log);

    }

    public static void generate(GenModel genModel, Log log) {
        //Generate Code
        genModel.setCanGenerate(true);
        GeneratorAdapterFactory.Descriptor.Registry.INSTANCE.addDescriptor(GenModelPackage.eNS_URI, GenModelGeneratorAdapterFactory.DESCRIPTOR);
        // Create the generator and set the model-level input object.
        Generator generator = new Generator();
        generator.setInput(genModel);
        generator.requestInitialize();
        // Generator model code.
        Diagnostic d = generator.generate(genModel, GenBaseGeneratorAdapter.MODEL_PROJECT_TYPE, new BasicMonitor.Printing(System.out));



        log.info(d.getMessage());
    }
}
