package org.kevoree.library.reasoner.ecj;

import org.kevoree.NamedElement;

import java.security.PrivateKey;

/**
 * Created by IntelliJ IDEA.
 * User: jbourcie
 * Date: 28/06/11
 * Time: 19:50
 * To change this template use File | Settings | File Templates.
 */
public class ComponentNode {
    private NamedElement node;
    private NamedElement component;

    public ComponentNode(NamedElement node, NamedElement component) {
        this.node = node;
        this.component = component;
    }

    public NamedElement getNode() {
        return node;
    }

    public void setNode(NamedElement node) {
        this.node = node;
    }

    public NamedElement getComponent() {
        return component;
    }

    public void setComponent(NamedElement component) {
        this.component = component;
    }
}
