package org.kevoree.library.reasoner.ecj;

import org.kevoree.NamedElement;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: jbourcie
 * Date: 28/06/11
 * Time: 19:48
 * To change this template use File | Settings | File Templates.
 */
public class DiffModel {
    private List<Map<String, NamedElement> > addInstance;
    private List<Map<String, NamedElement>> removeInstance;

    public DiffModel(List<Map<String, NamedElement>> addInstance, List<Map<String, NamedElement>> removeInstance) {
        this.addInstance = addInstance;
        this.removeInstance = removeInstance;
    }

    public List<Map<String, NamedElement>> getAddInstance() {
        return addInstance;
    }

    public void setAddInstance(List<Map<String, NamedElement>> addInstance) {
        this.addInstance = addInstance;
    }

    public List<Map<String, NamedElement>> getRemoveInstance() {
        return removeInstance;
    }

    public void setRemoveInstance(List<Map<String, NamedElement>> removeInstance) {
        this.removeInstance = removeInstance;
    }
}
