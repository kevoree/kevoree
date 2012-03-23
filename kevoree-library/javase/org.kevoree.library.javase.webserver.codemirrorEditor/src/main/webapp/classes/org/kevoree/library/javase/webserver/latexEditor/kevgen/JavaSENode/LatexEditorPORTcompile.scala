package org.kevoree.library.javase.webserver.latexEditor.kevgen.JavaSENode
import org.kevoree.framework.port._
import scala.{Unit=>void}
import org.kevoree.library.javase.webserver.latexEditor._
class LatexEditorPORTcompile(component : LatexEditor) extends org.kevoree.framework.MessagePort with KevoreeRequiredPort {
def getName : String = "compile"
def getComponentName : String = component.getName 
def process(o : Object) = {
{this ! o}
}
def getInOut = false
}
