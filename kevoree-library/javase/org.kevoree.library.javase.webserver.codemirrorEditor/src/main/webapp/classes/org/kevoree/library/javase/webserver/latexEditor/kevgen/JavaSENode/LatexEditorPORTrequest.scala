package org.kevoree.library.javase.webserver.latexEditor.kevgen.JavaSENode
import org.kevoree.framework.port._
import org.kevoree.library.javase.webserver.latexEditor._
import scala.{Unit=>void}
class LatexEditorPORTrequest(component : LatexEditor) extends org.kevoree.framework.MessagePort with KevoreeProvidedPort {
def getName : String = "request"
def getComponentName : String = component.getName 
def process(o : Object) = {this ! o}
override def internal_process(msg : Any)= msg match {
case _ @ msg =>try{component.requestHandler(msg)}catch{case _ @ e => {e.printStackTrace();println("Uncatched exception while processing Kevoree message")}}
}
}
