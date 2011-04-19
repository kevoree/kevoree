package org.kevoree.library.reasoner.ecj;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.kevoree.Channel;
import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.KevoreePackage;
import org.kevoree.MBinding;
import org.kevoree.NamedElement;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoree.library.tools.dpa.DPA;
import org.kevoree.library.reasoner.ecj.dpa.AddChannelDPA;
import org.kevoree.library.reasoner.ecj.dpa.AddComponentDPA;
import org.kevoree.library.reasoner.ecj.dpa.AddProvidedBindingDPA;
import org.kevoree.library.reasoner.ecj.dpa.AddRequiredBindingDPA;
import org.kevoree.library.reasoner.ecj.dpa.RemoveBindingDPA;
import org.kevoree.library.reasoner.ecj.dpa.RemoveChannelDPA;
import org.kevoree.library.reasoner.ecj.dpa.RemoveComponentDPA;
import org.kevoree.tools.marShell.ast.Script;
import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects;
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext;
import org.kevoree.tools.marShell.parser.KevsParser;
import org.kevoree.tools.marShell.parser.ParserUtil;
import org.kevoreeAdaptation.AdaptationModel;

import scala.Option;
import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;

public class KevoreeIndividual extends Individual {

    public static final DPA[] dpas = {
            new AddChannelDPA(),
            new AddComponentDPA(),
            new AddProvidedBindingDPA(),
            new AddRequiredBindingDPA(),
            new RemoveBindingDPA(),
            new RemoveChannelDPA(),
            new RemoveComponentDPA()};

    public static final String P_KEVOREE_INDIVIDUAL = "kev-ind";
    public ContainerRoot myModel;

    private static int increment = 0;

    /**
     * Destructively crosses over the individual with another in some default
     * manner.
     */
    public void defaultCrossover(EvolutionState state, int thread,
            KevoreeIndividual ind) {
        
    }

    /** Destructively mutates the individual in some default manner. */
    public void defaultMutate(EvolutionState state, int thread) {
        DPA myDPA = dpas[state.random[thread].nextInt(dpas.length)];
        List<Map<String, NamedElement>> myLists = myDPA.applyPointcut(myModel);
        if (!myLists.isEmpty()){
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

    /** Initializes the individual. */
    public void reset(EvolutionState state, int thread) {

    }

    public Object getGenome() {
        return myModel;
    }

    public void setGenome(Object gen) {
        if (gen instanceof ContainerRoot){
            this.myModel = (ContainerRoot)gen;
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
        if (!(ind instanceof ContainerRoot)) {
            return false;
        }
        if (myModel.getNodes().size() != ((ContainerRoot) ind).getNodes()
                .size()) {
            return false;
        }
        KevoreeKompareBean kkb = new KevoreeKompareBean();
        for (ContainerNode cn : myModel.getNodes()) {
            AdaptationModel am = kkb.kompare(myModel, (ContainerRoot) ind,
                    cn.getName());
            if (am.getAdaptations().isEmpty()) {
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
    
    public void setup(final EvolutionState state, final Parameter base){
        myModel = load(this.getClass().getClassLoader().getResource(".")+ File.separator+"kevoreeIndividualModel.kev");
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
        String path = "Models/"+increment ++;
        ParserUtil.save(path, myModel);
        return path;
    }

    // TODO could be added later....
    // public double distanceTo(Individual otherInd) {
    // return (equals(otherInd) ? 0 : Double.POSITIVE_INFINITY);
    // }
    
    
    // a List of primtive script that should be applied to this to obtain the individual given in parameters
//    private List<String> compare(KevoreeIndividual ki){
//        
//        List<ContainerNode> myNodes = myModel.getNodes();
//        List<ContainerNode> kiNodes = ki.myModel.getNodes();
//        List<ComponentInstance> myComponentInstances = new ArrayList<ComponentInstance>();
//        List<ComponentInstance> kiComponentInstances = new ArrayList<ComponentInstance>();
//        List<MBinding> myBindings = myModel.getMBindings();
//        List<MBinding> kiBindings = ki.myModel.getMBindings();
//        List<Channel> myChannels = myModel.getHubs();
//        List<Channel> kiChannels = myModel.getHubs();
//        
//        // the diffMy* list contains elements present in My that are not present in ki
//        // the diffKi* list contains elements present in ki that are not present in my
//        List<ContainerNode> diffMyNodes = new ArrayList<ContainerNode>();
//        List<ContainerNode> diffKiNodes = new ArrayList<ContainerNode>();
//        List<ComponentInstance> diffMyComponentInstances = new ArrayList<ComponentInstance>();
//        List<ComponentInstance> diffKiComponentInstances = new ArrayList<ComponentInstance>();
//        List<MBinding> diffMyBindings = new ArrayList<MBinding>();
//        List<MBinding> diffKiBindings = new ArrayList<MBinding>();
//        List<Channel> diffMyChannels = new ArrayList<Channel>();
//        List<Channel> diffKiChannels = new ArrayList<Channel>();
//        
//        // ComponentInstanceList initialisation
//        for (ContainerNode myNode : myNodes) {
//            myComponentInstances.addAll((myNode.getComponents()));
//        }
//        for (ContainerNode kiNode : kiNodes) {
//            kiComponentInstances.addAll((kiNode.getComponents()));
//        }
//        
//        //diff
//        if (kiNodes.removeAll(myNodes)){
//            diffKiNodes = kiNodes;
//        }
//        if (myNodes.removeAll(kiNodes)){
//            diffMyNodes = myNodes;
//        }
//        if (myComponentInstances.removeAll(kiComponentInstances)){
//            diffMyComponentInstances = myComponentInstances;
//        }
//        if (kiComponentInstances.removeAll(myComponentInstances)){
//            diffKiComponentInstances = kiComponentInstances;
//        }
//        if (myBindings.removeAll(kiBindings)){
//            diffMyBindings = myBindings;
//        }
//        if (kiBindings.removeAll(myBindings)){
//            diffKiBindings = kiBindings;
//        }
//        if (myChannels.removeAll(kiChannels)){
//            diffMyChannels = myChannels;
//        }
//        if (kiChannels.removeAll(myChannels)){
//            diffKiChannels = kiChannels;
//        }
//        
//        
//        return null;
//    }

}
