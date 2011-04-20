package org.kevoree.library.reasoner.ecj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.kevoree.ContainerRoot;
import org.kevoree.KevoreePackage;
import org.kevoree.tools.marShell.parser.ParserUtil;

public class GeneticAlgorithm {
    
    public static void main(String[] args){

        String basePath = GeneticAlgorithm.class.getClassLoader().getResource(".").getPath();


        ContainerRoot myModel = load(basePath+"baseMyNode.kev");
        ParserUtil.save(basePath +"kevoreeIndividualModel.kev", myModel);
        KevoreeGeneticAlgorithm kga = new KevoreeGeneticAlgorithm ();
        KevoreeIndividual ki = kga.start();
        if (ki!=null){
            ParserUtil.save(basePath +"kevoreeIndividualModel.kev", ki.myModel);
            App.startEditor(basePath+"kevoreeIndividualModel.kev");
            KevoreeMultipleGeneticAlgorithm kmga = new KevoreeMultipleGeneticAlgorithm ();
            deleteAllOldModels();
            printBestParetoFront();
        }
    }

    private static void deleteAllOldModels() {
        File myFile = new File("Models");
        if (myFile.isDirectory()){
            myFile.delete();
        }
    }
    
    public static ContainerRoot load(String uri) {
        ResourceSetImpl rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("kev", new XMIResourceFactoryImpl());
        rs.getPackageRegistry().put(KevoreePackage.eNS_URI, KevoreePackage.eINSTANCE);
        Resource res = rs.getResource(URI.createURI(uri), true);
        ((XMIResource) res).getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
        ((XMIResource) res).getDefaultSaveOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
        Object result = res.getContents().get(0);
        //println(res)
        return (ContainerRoot) result;
    }

    private static void printBestParetoFront() {
        File myStatFile = new File("out.stat");
        String[] bestsIndividualsFile = retrieveBestIndividuals(myStatFile);
        System.out.println("Number of best individuals identified : "
                + bestsIndividualsFile.length);
        if (bestsIndividualsFile.length <= 20) {
            for (String path : bestsIndividualsFile) {
                App.startEditor(path);
            }
        }else {
            App.startEditor(bestsIndividualsFile[0]);
        }
    }

    private static String[] retrieveBestIndividuals(File myStatFile) {
        List<String> bestIndividualsFile = new ArrayList<String>();
        List<KevoreeMultipleRepresentation> paretoFront = new ArrayList<KevoreeMultipleRepresentation>();
        try{
            InputStream ips=new FileInputStream(myStatFile); 
            InputStreamReader ipsr=new InputStreamReader(ips);
            BufferedReader br=new BufferedReader(ipsr);
            String line;
            boolean pareto = false, evaluated = false, fitness = false, rank = false, file = false;
            KevoreeMultipleRepresentation current = new KevoreeMultipleRepresentation();
            while ((line=br.readLine())!=null){
                if (!pareto && line.contains("Pareto Front of Subpopulation 0")){
                    pareto = true;
                    evaluated = true;
                } else
                if (pareto && evaluated){
                    current = new KevoreeMultipleRepresentation();
                    evaluated = false;
                    fitness = true;
                } else
                if (pareto && fitness){
                    line = line.substring(line.indexOf("[")+1, line.indexOf("min]"));
                    String[] toto = line.split(" ");
                    float[] fit = new float[toto.length];
                    for (int i = 0; i < toto.length; i++) {
                        fit[i] = new Float(toto[i]);
                    }
                    current.setFitness(fit);
                    fitness = false;
                    rank = true;
                } else
                if (pareto && rank){
                  //nothing to do
                    rank = false;
                    file = true;
                } else
                if (pareto && file) {
                    current.setPathToFile(line);
                    paretoFront.add(current);
                    file = false;
                    evaluated = true;
                }
            }
            br.close(); 
        }       
        catch (Exception e){
            e.printStackTrace();
        }
        if (!paretoFront.isEmpty()){
            float minFitness = paretoFront.get(0).getFitness()[0];
            for (KevoreeMultipleRepresentation kevoreeMultipleRepresentation : paretoFront) {
                if (minFitness > kevoreeMultipleRepresentation.getFitness()[0]){
                    minFitness = kevoreeMultipleRepresentation.getFitness()[0];
                }
            }
            List<float[]> representativeBestFitness = new ArrayList<float[]>();
            for (KevoreeMultipleRepresentation kevoreeMultipleRepresentation : paretoFront) {
                if (minFitness == kevoreeMultipleRepresentation.getFitness()[0]){
                    for (float[] fs : representativeBestFitness) {
                        kevoreeMultipleRepresentation.getFitness().equals(fs);
                    }
                    kevoreeMultipleRepresentation.getFitness();
                    bestIndividualsFile.add(kevoreeMultipleRepresentation.getPathToFile());
                }
            }
            
        }
        return bestIndividualsFile.toArray(new String[bestIndividualsFile.size()]);
    }
}
