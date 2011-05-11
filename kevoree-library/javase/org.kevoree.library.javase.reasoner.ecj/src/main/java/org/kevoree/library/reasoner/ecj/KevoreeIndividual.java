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
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.KevoreePackage;
import org.kevoree.NamedElement;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KevoreeIndividual extends Individual {

    public DPA[] dpas;

    public static final String P_KEVOREE_INDIVIDUAL = "kev-ind";
    public ContainerRoot myModel;

    private static int increment = 0;

    /**
     * Destructively crosses over the individual with another in some default
     * manner.
     */
    public void defaultCrossover(EvolutionState state, int thread,
                                 KevoreeIndividual ind) {
        List<Statment> statements = compare(ind);
        KevsInterpreterContext context = new KevsInterpreterContext(myModel);
        for (Statment statement : statements) {
            if (state.random[thread].nextBoolean()) {
                KevsInterpreterAspects.rich(AstHelper.createBlockFromStatement(statement)).interpret(context);
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
        myModel = load(this.getClass().getClassLoader().getResource(".") + File.separator + "kevoreeIndividualModel.kev");
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
        String path = "Models/" + increment++;
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
            AdaptationModel am = kkb.kompare(myModel, (ContainerRoot) ki.myModel,
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

}
