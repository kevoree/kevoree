/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.tools.dpa;

import java.util.Map;
import org.kevoree.ContainerRoot;
import org.kevoree.NamedElement;

/**
 *
 * @author ffouquet
 */
public interface DPA {

    public Map<String,NamedElement> applyPointcut(ContainerRoot model);
    
    public String getScript(Map<String,NamedElement> vars);
    
}
