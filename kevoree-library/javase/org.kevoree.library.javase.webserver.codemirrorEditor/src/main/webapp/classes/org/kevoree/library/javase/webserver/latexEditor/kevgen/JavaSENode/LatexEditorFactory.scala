package org.kevoree.library.javase.webserver.latexEditor.kevgen.JavaSENode
import org.kevoree.framework._
import org.kevoree.library.javase.webserver.latexEditor._
class LatexEditorFactory extends org.kevoree.framework.osgi.KevoreeInstanceFactory {
override def registerInstance(instanceName : String, nodeName : String)=LatexEditorFactory.registerInstance(instanceName,nodeName)
override def remove(instanceName : String)=LatexEditorFactory.remove(instanceName)
def createInstanceActivator = LatexEditorFactory.createInstanceActivator}
object LatexEditorFactory extends org.kevoree.framework.osgi.KevoreeInstanceFactory {
def createInstanceActivator: org.kevoree.framework.osgi.KevoreeInstanceActivator = new LatexEditorActivator
def createComponentActor() : KevoreeComponent = {
new KevoreeComponent(createLatexEditor()){def startComponent(){getKevoreeComponentType.asInstanceOf[org.kevoree.library.javase.webserver.latexEditor.LatexEditor].startPage()}
def stopComponent(){getKevoreeComponentType.asInstanceOf[org.kevoree.library.javase.webserver.latexEditor.LatexEditor].stopPage()}
override def updateComponent(){getKevoreeComponentType.asInstanceOf[org.kevoree.library.javase.webserver.latexEditor.LatexEditor].updatePage()}
}}
def createLatexEditor() : org.kevoree.library.javase.webserver.latexEditor.LatexEditor ={
var newcomponent = new org.kevoree.library.javase.webserver.latexEditor.LatexEditor();
newcomponent.getHostedPorts().put("request",createLatexEditorPORTrequest(newcomponent))
newcomponent.getHostedPorts().put("compileCallback",createLatexEditorPORTcompileCallback(newcomponent))
newcomponent.getNeededPorts().put("content",createLatexEditorPORTcontent(newcomponent))
newcomponent.getNeededPorts().put("files",createLatexEditorPORTfiles(newcomponent))
newcomponent.getNeededPorts().put("compile",createLatexEditorPORTcompile(newcomponent))
newcomponent}
def createLatexEditorPORTrequest(component : LatexEditor) : LatexEditorPORTrequest ={ new LatexEditorPORTrequest(component)}
def createLatexEditorPORTcompileCallback(component : LatexEditor) : LatexEditorPORTcompileCallback ={ new LatexEditorPORTcompileCallback(component)}
def createLatexEditorPORTcontent(component : LatexEditor) : LatexEditorPORTcontent ={ return new LatexEditorPORTcontent(component);}
def createLatexEditorPORTfiles(component : LatexEditor) : LatexEditorPORTfiles ={ return new LatexEditorPORTfiles(component);}
def createLatexEditorPORTcompile(component : LatexEditor) : LatexEditorPORTcompile ={ return new LatexEditorPORTcompile(component);}
}
