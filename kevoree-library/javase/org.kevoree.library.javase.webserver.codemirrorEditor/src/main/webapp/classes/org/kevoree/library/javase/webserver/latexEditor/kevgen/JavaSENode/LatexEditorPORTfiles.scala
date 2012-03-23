package org.kevoree.library.javase.webserver.latexEditor.kevgen.JavaSENode
import org.kevoree.framework.port._
import scala.{Unit=>void}
import org.kevoree.library.javase.webserver.latexEditor._
class LatexEditorPORTfiles(component : LatexEditor) extends org.kevoree.library.javase.fileSystem.LockFilesService with KevoreeRequiredPort {
def getName : String = "files"
def getComponentName : String = component.getName 
def getInOut = true
def getFilesPath() : java.util.Set[java.lang.String] ={
var msgcall = new org.kevoree.framework.MethodCallMessage
msgcall.setMethodName("getFilesPath");
(this !? msgcall).asInstanceOf[java.util.Set[java.lang.String]]}
def getFilteredFilesPath(arg0:java.util.Set[java.lang.String]) : java.util.Set[java.lang.String] ={
var msgcall = new org.kevoree.framework.MethodCallMessage
msgcall.setMethodName("getFilteredFilesPath");
msgcall.getParams.put("arg0",arg0.asInstanceOf[AnyRef]);
(this !? msgcall).asInstanceOf[java.util.Set[java.lang.String]]}
def getFileContent(arg0:java.lang.String,arg1:java.lang.Boolean) : Array[Byte] ={
var msgcall = new org.kevoree.framework.MethodCallMessage
msgcall.setMethodName("getFileContent");
msgcall.getParams.put("arg0",arg0.asInstanceOf[AnyRef]);
msgcall.getParams.put("arg1",arg1.asInstanceOf[AnyRef]);
(this !? msgcall).asInstanceOf[Array[Byte]]}
def getAbsolutePath(arg0:java.lang.String) : java.lang.String ={
var msgcall = new org.kevoree.framework.MethodCallMessage
msgcall.setMethodName("getAbsolutePath");
msgcall.getParams.put("arg0",arg0.asInstanceOf[AnyRef]);
(this !? msgcall).asInstanceOf[java.lang.String]}
def saveFile(arg0:java.lang.String,arg1:Array[Byte],arg2:java.lang.Boolean) : scala.Boolean ={
var msgcall = new org.kevoree.framework.MethodCallMessage
msgcall.setMethodName("saveFile");
msgcall.getParams.put("arg0",arg0.asInstanceOf[AnyRef]);
msgcall.getParams.put("arg1",arg1.asInstanceOf[AnyRef]);
msgcall.getParams.put("arg2",arg2.asInstanceOf[AnyRef]);
(this !? msgcall).asInstanceOf[scala.Boolean]}
def unlock(arg0:java.lang.String) : void ={
var msgcall = new org.kevoree.framework.MethodCallMessage
msgcall.setMethodName("unlock");
msgcall.getParams.put("arg0",arg0.asInstanceOf[AnyRef]);
(this !? msgcall).asInstanceOf[void]}
}
