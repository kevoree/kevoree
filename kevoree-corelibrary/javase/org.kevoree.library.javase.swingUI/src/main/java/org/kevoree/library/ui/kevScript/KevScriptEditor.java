package org.kevoree.library.ui.kevScript;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.kevscript.LocalKevsShell;
import org.kevoree.library.ui.layout.KevoreeLayout;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 30/05/12
 * Time: 21:01
 */
@Library(name = "JavaSE")
@ComponentType
public class KevScriptEditor extends AbstractComponentType {

    private LocalKevsShell lkevs = null;

    @Start
    public void start(){
        lkevs = new LocalKevsShell(getModelService(),getKevScriptEngineFactory());
        KevoreeLayout.getInstance().displayTab(lkevs,getName());
    }
    @Stop
    public void stop(){
        KevoreeLayout.getInstance().releaseTab(getName());
        lkevs = null;
    }

}
