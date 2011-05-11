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
import org.kevoree.tools.marShell.ast.AstHelper;
import org.kevoree.tools.marShell.ast.Block;
import org.kevoree.tools.marShell.ast.Script;
import org.kevoree.tools.marShell.ast.Statment;
import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects;
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext;
import org.kevoree.tools.marShell.parser.KevsParser;
import org.kevoree.tools.marShell.parser.ParserUtil;
import org.kevoree.tools.marShellTransform.AdaptationModelWrapper;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AddBinding;
import scala.Option;
import scala.collection.Iterator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KevoreeMultiIndividual extends KevoreeIndividual {

    public final static DPA[] MultipleDpas = {
            new RemoveBindingDPA(),
            new RemoveChannelDPA(),
            new RemoveComponentDPA(),
            new AddBindingDPA(),
            new MoveComponentDPA()
    };

    public Object clone() {
        KevoreeMultiIndividual ki = (KevoreeMultiIndividual) super.clone();
        ki.dpas = MultipleDpas;
        return ki;
    }

}
