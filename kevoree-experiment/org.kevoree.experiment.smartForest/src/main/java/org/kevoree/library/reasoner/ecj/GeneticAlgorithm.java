package org.kevoree.library.reasoner.ecj;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.KevoreePackage;
import org.kevoree.NamedElement;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.tools.marShell.ast.Script;
import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects;
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext;
import org.kevoree.tools.marShell.parser.KevsParser;
import org.kevoree.tools.marShell.parser.ParserUtil;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class GeneticAlgorithm {
    public static int forestWidth = 5;
    public static int generations = 50;
    public static int populations = 100;

    public static String folderToStoreTempFile = "generated";

    private static final ClassLoader classLoader = GeneticAlgorithm.class.getClassLoader();


    public static void main(String[] args){
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.OFF); //change to off

        deleteStatistiqueFile();
        folderToStoreTempFile = getComputerFullName() + "-" + folderToStoreTempFile;
        // initialization of the architecture : We are starting with an architecture with all components on all nodes
        URL url = null;
        String path = "";
        try {
            url = classLoader.getResource("baseMyNodes.kev");
            path = URLDecoder.decode(url.toString(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        ContainerRoot myModel = load(path);
        for (int i=0; i<forestWidth*forestWidth; i++){
            String scriptString = "tblock {\n addNode node"+i+":ArduinoNode \n }";
            KevsParser parser = new KevsParser();
            Option<Script> currentScript = parser
                    .parseScript(scriptString);
            if (!currentScript.isEmpty()) {
                Script script = (Script) currentScript.get();
                KevsInterpreterContext context = new KevsInterpreterContext(myModel);
                KevsInterpreterAspects.rich(script).interpret(context);
            }
        }
        int i = 0;
        for (ContainerNode containerNode : myModel.getNodes()) {
            String scriptString = "tblock {\n addComponent temp"+i+"@"+((NamedElement) containerNode).getName()+" : TempSensor \n   addComponent smoke"+i+"@"+((NamedElement) containerNode).getName()+" : SmokeSensor \n  addComponent humidity"+i+"@"+((NamedElement) containerNode).getName()+": HumiditySensor \n }";
            KevsParser parser = new KevsParser();
            Option<Script> currentScript = parser
                    .parseScript(scriptString);
            if (!currentScript.isEmpty()) {
                Script script = (Script) currentScript.get();
                KevsInterpreterContext context = new KevsInterpreterContext(myModel);
                KevsInterpreterAspects.rich(script).interpret(context);
            }
            i++;
        }

        try {
            InputStream ips=classLoader.getResourceAsStream("kevoreeMultiCrossOverTest.params");
            InputStreamReader ipsr=new InputStreamReader(ips);
            BufferedReader br=new BufferedReader(ipsr);
            String line;
            File generatedDirectory = new File(folderToStoreTempFile);
            if (generatedDirectory.exists() && generatedDirectory.isDirectory()){
                for (File myFile : generatedDirectory.listFiles()){
                    myFile.delete();
                }
            }
            generatedDirectory.mkdirs();
            FileWriter fw = new FileWriter (folderToStoreTempFile + File.separator + "kevoreeMultiTestGenerated.params");
            BufferedWriter bw = new BufferedWriter (fw);
            PrintWriter outputFile = new PrintWriter (bw);

            while ((line=br.readLine())!=null){
                line = line.replace("pop.subpop.0.size = 100", "pop.subpop.0.size = " + populations);
                line = line.replace("generations = 100", "generations = " + generations);
                line = line.replace("breed.elite.0 = 100", "breed.elite.0 = " + generations/4);

                outputFile.println (line);
            }
            br.close();
            outputFile.flush();
            outputFile.close();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        copyFileToGeneratedDirectory("ec.params");
        copyFileToGeneratedDirectory("multiobjective.params");
        copyFileToGeneratedDirectory("nsga2.params");
        copyFileToGeneratedDirectory("spea2.params");
        copyFileToGeneratedDirectory("simple.params");

        ParserUtil.save(folderToStoreTempFile + File.separator + "kevoreeIndividualModel.kev", myModel);
        System.out.println("Precision = " + MultiObjectiveKevoreeProblem.evaluatePrecision(myModel));
        KevoreeMultipleGeneticAlgorithm kmga = new KevoreeMultipleGeneticAlgorithm ();
    //    deleteAllOldModels();
        printFitnessOfBestParetoFront(0, populations, generations, forestWidth);
    }

    private static void deleteAllOldModels() {
        File myFile = new File("Models");
        if (myFile.isDirectory()){
            myFile.delete();
        }
    }

    private static void deleteStatistiqueFile() {
        File myFile = new File("run.statistique");
        if (myFile.exists() && myFile.isFile()){
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
        return (ContainerRoot) result;
    }

    private static void printBestParetoFront() {
        File myStatFile = new File("out.stat");
        String[] bestsIndividualsFile = retrieveBestIndividuals(myStatFile);
        System.out.println("Number of best individuals identified : "
                + bestsIndividualsFile.length);
        if (bestsIndividualsFile.length <= 3) {
            for (String path : bestsIndividualsFile) {
                App.startEditor(path);
            }
        }else {
            for (int i=0; i<3; i++){
                App.startEditor(bestsIndividualsFile[i]);
            }
        }
    }

    private static void printFitnessOfBestParetoFront(int run, int population, int generation, int actualForestWidth) {
        File myStatFile = new File("out.stat");
        KevoreeMultipleRepresentation kmr = retrieveFitnessOfBestIndividuals(myStatFile);
        System.out.println("Fitness of best individuals identified : " + kmr.getFitness()[1]);
        try {
            FileWriter fw = new FileWriter("run.statistique", true);
            fw.append("Forest size = "+ actualForestWidth*actualForestWidth+", population = "+population+", generation = "+generation+", run = "+run+", fitness = " +kmr.getFitness()[1]+"\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println("BestRepresentation : ");
        ContainerRoot myModel = KevoreeXmiHelper.load(kmr.getPathToFile());
        int i=1;
        for (ContainerNode myNode : myModel.getNodes()){
            int number = 3-myNode.getComponents().size();
            System.out.print(number+" ");
            if (i == actualForestWidth){
                System.out.println("");
                i = 1;
            } else {
                i++;
            }
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
//            List<float[]> representativeBestFitness = new ArrayList<float[]>();
            for (KevoreeMultipleRepresentation kevoreeMultipleRepresentation : paretoFront) {
                if (minFitness == kevoreeMultipleRepresentation.getFitness()[0]){
//                    for (float[] fs : representativeBestFitness) {
//                        kevoreeMultipleRepresentation.getFitness().equals(fs);
//                    }
                    kevoreeMultipleRepresentation.getFitness();
                    bestIndividualsFile.add(kevoreeMultipleRepresentation.getPathToFile());
                }
            }
            
        }
        return bestIndividualsFile.toArray(new String[bestIndividualsFile.size()]);
    }

    private static KevoreeMultipleRepresentation retrieveFitnessOfBestIndividuals(File myStatFile) {
        float bestIndividuals = 100;
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
        KevoreeMultipleRepresentation kmr = null;
        if (!paretoFront.isEmpty()){
            float minFitness = paretoFront.get(0).getFitness()[0];
            for (KevoreeMultipleRepresentation kevoreeMultipleRepresentation : paretoFront) {
                if (minFitness > kevoreeMultipleRepresentation.getFitness()[0]){
                    minFitness = kevoreeMultipleRepresentation.getFitness()[0];
                }
            }
//            List<float[]> representativeBestFitness = new ArrayList<float[]>();
            for (KevoreeMultipleRepresentation kevoreeMultipleRepresentation : paretoFront) {
                if (minFitness == kevoreeMultipleRepresentation.getFitness()[0]){
                    bestIndividuals = kevoreeMultipleRepresentation.getFitness()[1];
                    kmr = kevoreeMultipleRepresentation;
                }
            }

        }
        return kmr;
    }

    public static String getPathJar() {
        try {
            String path = GeneticAlgorithm.class.getSimpleName() + ".class";//$NON-NLS-1$
            URL url = GeneticAlgorithm.class.getResource(path);
            path = URLDecoder.decode(url.toString(), "UTF-8");//$NON-NLS-1$
            int index = path.lastIndexOf('/');
            path = path.substring(0, index);
            String jar = "jar:file:", file = "file:";//$NON-NLS-1$//$NON-NLS-2$

            if(path.startsWith(jar))  {
                index = path.lastIndexOf("!");//$NON-NLS-1$
                path = path.substring(jar.length(), path.substring(0, index).lastIndexOf(File.separator));
            }
            else {
                path = path.substring(file.length(), path.length());
                Package pack = GeneticAlgorithm.class.getPackage();

                if(null != pack) {
                    String packPath = pack.getName().replace('.', '/');

                    if(path.endsWith(packPath))
                        path = path.substring(0, path.length() - packPath.length());
              }
            }

            return path;

        }catch(Exception e) {
            return null;
        }
    }

    public static void copyFileToGeneratedDirectory(String myFileName){
        try {
            InputStream ips = classLoader.getResourceAsStream(myFileName);
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader br = new BufferedReader(ipsr);
            String line;
            FileWriter fw = new FileWriter(folderToStoreTempFile + File.separator + myFileName);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter outputFile = new PrintWriter(bw);

            while ((line = br.readLine()) != null) {
                outputFile.println(line);
            }
            br.close();
            outputFile.flush();
            outputFile.close();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static String getComputerFullName() {
    String hostName = null;
    try {
      final InetAddress addr = InetAddress.getLocalHost();
      hostName = new String(addr.getHostName());
    } catch(final Exception e) {
    }//end try
    return hostName;
  }
}
