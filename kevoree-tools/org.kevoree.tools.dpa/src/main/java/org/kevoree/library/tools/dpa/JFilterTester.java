package org.kevoree.library.tools.dpa;

import fr.inria.jfilter.Filter;
import fr.inria.jfilter.FilterException;
import fr.inria.jfilter.FilterParser;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 07/02/13
 * Time: 14:32
 */
public class JFilterTester {

    public static void main(String[]args) throws FilterException {
        System.out.println("Hello ");

        System.out.println("name = editor_node");
        ContainerRoot model = KevoreeXmiHelper.$instance.load("/Users/duke/Desktop/hello.kev");
        Filter filter1 = FilterParser.instance.parse("name = editor_node");
        Collection<ContainerNode> filtered = filter1.filter(model.getNodes());
        for(ContainerNode n : filtered){
            System.out.println("Name="+n.getName());
        }

        System.out.println("name = editor_*");
        Filter filter2 = FilterParser.instance.parse("name = editor_*");
        Collection<ContainerNode> filtered2 = filter2.filter(model.getNodes());
        for(ContainerNode n : filtered2){
            System.out.println("Name="+n.getName()+":"+n.getTypeDefinition().getName());

        }

        System.out.println("typeDefinition.name = MiniCloudNode*");
        Filter filter3 = FilterParser.instance.parse("typeDefinition.name = MiniCloudNode*");
        Collection<ContainerNode> filtered3 = filter3.filter(model.getNodes());
        for(ContainerNode n : filtered3){
            System.out.println("Name="+n.getName()+":"+n.getTypeDefinition().getName());
        }




    }

}
