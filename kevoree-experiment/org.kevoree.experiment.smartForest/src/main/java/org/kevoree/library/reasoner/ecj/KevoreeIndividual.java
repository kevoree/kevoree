package org.kevoree.library.reasoner.ecj;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.kevoree.*;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoree.library.reasoner.ecj.dpa.*;
import org.kevoree.library.tools.dpa.DPA;
import org.kevoree.tools.marShell.ast.*;
import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects;
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext;
import org.kevoree.tools.marShell.parser.KevsParser;
import org.kevoree.tools.marShell.parser.ParserUtil;
import org.kevoree.tools.marShellTransform.AdaptationModelWrapper;
import org.kevoreeAdaptation.AdaptationModel;
import scala.Option;
import scala.collection.Iterator;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KevoreeIndividual extends Individual {

    public DPA[] dpas;

    public static final String P_KEVOREE_INDIVIDUAL = "kev-ind";
    public static final String FOLDER_TO_STORE_MODELS = "models-folder";
    public ContainerRoot myModel;
    private String model_path;

    private static int increment = 0;

    /**
     * Destructively crosses over the individual with another in some default
     * manner.
     */
    public void defaultCrossover(EvolutionState state, int thread,
                                 KevoreeIndividual ind) {
        DiffModel dm = compareForest(ind);
        KevsInterpreterContext context = new KevsInterpreterContext(myModel);
        DPA addDPA = new AddComponentDPA();
        DPA removeDPA = new RemoveComponentDPA();
        for (Map<String, NamedElement> map : dm.getAddInstance()) {
            if (state.random[thread].nextBoolean()) {
                String scriptString = addDPA.getScript(map);
                KevsParser parser = new KevsParser();
                Option<Script> currentScript = parser
                        .parseScript(scriptString);
                if (!currentScript.isEmpty()) {
                    Script script = (Script) currentScript.get();
                    KevsInterpreterAspects.rich(script).interpret(context);
                }
            }
        }
        for (Map<String, NamedElement> map : dm.getRemoveInstance()) {
            if (state.random[thread].nextBoolean()) {
                String scriptString = removeDPA.getScript(map);
                KevsParser parser = new KevsParser();
                Option<Script> currentScript = parser
                        .parseScript(scriptString);
                if (!currentScript.isEmpty()) {
                    Script script = (Script) currentScript.get();
                    KevsInterpreterAspects.rich(script).interpret(context);
                }
            }
        }
    }

    /**
     * Destructively mutates the individual in some default manner.
     */
    public void defaultMutate(EvolutionState state, int thread) {
        DPA myDPA = dpas[state.random[thread].nextInt(dpas.length)];
        List<Map<String, NamedElement>> myLists = myDPA.applyPointcut(myModel);
        if (!myLists.isEmpty()) {
            Map<String, NamedElement> myMap = myLists.get(state.random[thread].nextInt(myLists.size()));
            String scriptString = myDPA.getScript(myMap);
            KevsParser parser = new KevsParser();
            Option<Script> currentScript = parser
                    .parseScript(scriptString);
            if (!currentScript.isEmpty()) {
                Script script = (Script) currentScript.get();
                KevsInterpreterContext context = new KevsInterpreterContext(myModel);
                KevsInterpreterAspects.rich(script).interpret(context);
            }
        }
    }

    /**
     * Initializes the individual.
     */
    public void reset(EvolutionState state, int thread) {
        int numberOfComponentsToBeRemoved = state.random[thread].nextInt(myModel.getNodes().size()*2);
        RemoveComponentDPA rcDPA = new RemoveComponentDPA();
        for (int i=0; i<numberOfComponentsToBeRemoved; i++){
            List<Map<String, NamedElement>> myList = rcDPA.applyPointcut(myModel);
            if (myList.size()<=0)
                return;
            String scriptString = rcDPA.getScript(myList.get(state.random[thread].nextInt(myList.size())));
            KevsParser parser = new KevsParser();
            Option<Script> currentScript = parser
                    .parseScript(scriptString);
            if (!currentScript.isEmpty()) {
                Script script = (Script) currentScript.get();
                KevsInterpreterContext context = new KevsInterpreterContext(myModel);
                KevsInterpreterAspects.rich(script).interpret(context);
            }
        }
    }

    public Object getGenome() {
        return myModel;
    }

    public void setGenome(Object gen) {
        if (gen instanceof ContainerRoot) {
            this.myModel = (ContainerRoot) gen;
        }
    }

    public int genomeLength() {
        return (int) size();
    }

    public Parameter defaultBase() {
        return KevoreeDefaults.base().push(P_KEVOREE_INDIVIDUAL);
    }

    public Object clone() {
        KevoreeIndividual ki = (KevoreeIndividual) super.clone();
        ki.myModel = EcoreUtil.copy(myModel);
        return ki;
    }

    public boolean equals(Object ind) {
        if (!(ind instanceof KevoreeIndividual)) {
            return false;
        }
        if (myModel.getNodes().size() != ((KevoreeIndividual) ind).myModel.getNodes()
                .size()) {
            return false;
        }
        KevoreeKompareBean kkb = new KevoreeKompareBean();
        for (ContainerNode cn : myModel.getNodes()) {
            AdaptationModel am = kkb.kompare(myModel, ((KevoreeIndividual) ind).myModel,
                    cn.getName());
            if (!am.getAdaptations().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return myModel.hashCode();
    }

    public long size() {
        int count = 0;
        for (ContainerNode cn : myModel.getNodes()) {
            count += cn.getComponents().size();
        }
        return count;
    }

    public void setup(final EvolutionState state, final Parameter base) {
        myModel = load(GeneticAlgorithm.folderToStoreTempFile + File.separator + "kevoreeIndividualModel.kev");
        model_path = state.parameters.getString(base.push(FOLDER_TO_STORE_MODELS),null);
        File stat = new File(model_path);
        if (stat.isDirectory()){
            for(File file : stat.listFiles()){
                file.delete();
            }
            stat.delete();
        }

        model_path = model_path + File.separator;
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

//    public void printIndividualForHumans(EvolutionState state, int log){
//        ParserUtil.save("target/classes/modified.kev", myModel);
//
//        App.startEditor("target/classes/modified.kev");
//    }

    // 
    public String toString() {

        String path = model_path + "Models" + increment++;
        ParserUtil.save(path, myModel);
        return path;
    }


    public double distanceTo(Individual otherInd) {
        System.out.println("distanceTo");
        double result = 0.0;
        KevoreeKompareBean kkb = new KevoreeKompareBean();
        for (ContainerNode cn : myModel.getNodes()) {
            AdaptationModel am = kkb.kompare(myModel, ((KevoreeIndividual) otherInd).myModel,
                    cn.getName());
            result += am.getAdaptations().size();
        }
        return result;
    }


    // a List of primtive script that should be applied to this to obtain the individual given in parameters
    private List<Statment> compare(KevoreeIndividual ki) {
        AdaptationModel amAggregated = null;
        KevoreeKompareBean kkb = new KevoreeKompareBean();
        for (ContainerNode cn : myModel.getNodes()) {
            AdaptationModel am = kkb.kompare(myModel, ki.myModel,
                    cn.getName());
            if (amAggregated == null) {
                amAggregated = am;
            } else {
                amAggregated.getAdaptations().addAll(am.getAdaptations());
            }
        }
        if (amAggregated != null) {
            List<Statment> result = new ArrayList<Statment>();
            Script script = AdaptationModelWrapper.generateScriptFromAdaptModel(amAggregated);
            Iterator<Block> it = script.blocks().iterator();
            while (it.hasNext()) {
                Iterator<Statment> itStatment = it.next().l().iterator();
                while (itStatment.hasNext()) {
                    result.add(itStatment.next());
                }
            }

            return result;
        }
        return null;
    }

    // return a diff model specific to the smartForest use case.
    private DiffModel compareForest(KevoreeIndividual ki) {
        List<Map<String, NamedElement>> addList = new ArrayList<Map<String, NamedElement>>();
        List<Map<String, NamedElement>> removeList = new ArrayList<Map<String, NamedElement>>();
        for (int i=0; i<myModel.getNodes().size(); i++){
            ContainerNode myNode = myModel.getNodes().get(i);
            ContainerNode otherNode = ki.myModel.getNodes().get(i);
            for (ComponentInstance ci : myNode.getComponents()){
                if (!MultiObjectiveKevoreeProblem.containsInstance(otherNode,ci.getTypeDefinition().getName())) {
                    Map<String, NamedElement> myMap = new HashMap<String, NamedElement>();
                    myMap.put(RemoveComponentDPA.componentName,
                        (NamedElement) ci);
                    myMap.put(RemoveComponentDPA.nodeName,
                        (NamedElement) myNode);
                    removeList.add(myMap);
                }

            }
            for (ComponentInstance ci : otherNode.getComponents()){
                if (!MultiObjectiveKevoreeProblem.containsInstance(myNode,ci.getTypeDefinition().getName())){
                    Map<String, NamedElement> myMap = new HashMap<String, NamedElement>();
                    myMap.put(AddComponentDPA.typeDefinition, (NamedElement) ci.getTypeDefinition());
                    myMap.put(AddComponentDPA.nodeName,
                        (NamedElement) myNode);
                    addList.add(myMap);
                }
            }

        }
        return new DiffModel(addList, removeList);
    }

}
