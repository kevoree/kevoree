package org.kevoree.library.javase.webserver.latexEditor.kevgen.JavaSENode;
import java.util.Hashtable
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.framework.KevoreeActor
import org.kevoree.framework.KevoreeComponent
import org.kevoree.framework._
import org.kevoree.library.javase.webserver.latexEditor._
class LatexEditorActivator extends org.kevoree.framework.osgi.KevoreeComponentActivator {
def callFactory() : KevoreeComponent = { LatexEditorFactory.createComponentActor() } }
