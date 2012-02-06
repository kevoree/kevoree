package org.kevoree.library.frascatiNodeTypes.primitives;
import org.kevoreeAdaptation.AdaptationPrimitive
import org.kevoree.library.frascatiNodeTypes.FrascatiNode
import org.eclipse.stp.sca.ComponentType
import org.ow2.frascati.FraSCAti
import org.kevoree.api.PrimitiveCommand
import org.objectweb.fractal.api.Component
import org.objectweb.fractal.api.control.ContentController
import org.objectweb.fractal.api.control.AttributeController
import scala.collection.JavaConversions._
import org.objectweb.fractal.api.control.LifeCycleController
import java.io.File
import scala.xml.Node
import scala.collection.mutable.Queue
import java.io.PrintWriter
import org.kevoree.{DeployUnit, ComponentInstance, Instance}
import org.kevoree.extra.jcl.KevoreeJarClassLoader
import org.kevoree.framework.AbstractNodeType


object AdaptatationPrimitiveFactory {

  	var _frascati :FraSCAti = _ ;
	def frascati : FraSCAti = _frascati;
  	def setFrascati(fras :FraSCAti) = {
  	  _frascati =fras
  	  }
  
  def getPrimitive(adaptationPrimitive: AdaptationPrimitive, node: FrascatiNode, nodeType : AbstractNodeType, topKCL : KevoreeJarClassLoader): org.kevoree.api.PrimitiveCommand = {

    //        values = {"UpdateType", "UpdateDeployUnit", "AddType", "AddDeployUnit", "AddThirdParty", 
    //"RemoveType", "RemoveDeployUnit", "UpdateInstance", "UpdateBinding", "UpdateDictionaryInstance", 

    println("pass par la " + adaptationPrimitive.getPrimitiveType.getName)
    
    adaptationPrimitive.getPrimitiveType.getName match {
      case "UpdateType" => {
        node.getSuperPrimitive(adaptationPrimitive);
      }
      case "UpdateDeployUnit" => {
        node.getSuperPrimitive(adaptationPrimitive);
      }
      case "AddType" => {
        node.getSuperPrimitive(adaptationPrimitive);
      }
      case "AddDeployUnit" => {
        FrascatiAddDedployUnit(adaptationPrimitive.getRef.asInstanceOf[DeployUnit],nodeType.getBootStrapperService,topKCL)
      }
      case "AddThirdParty" => {
        FrascatiAddDedployUnit(adaptationPrimitive.getRef.asInstanceOf[DeployUnit],nodeType.getBootStrapperService,topKCL)
      }
      case "RemoveType" => {
        node.getSuperPrimitive(adaptationPrimitive);
      }
      case "RemoveDeployUnit" => {
        node.getSuperPrimitive(adaptationPrimitive);
      }
      case "UpdateDictionaryInstance" => {
    	 /* 	if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.Instance] && adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new UpdateDictionaryInstance(adaptationPrimitive);
        } else*/
          node.getSuperPrimitive(adaptationPrimitive)
      }
      case "UpdateInstance" => {
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "RemoveInstance" => {
    	  	if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.Instance] && adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new RemoveInstance(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "AddInstance" => {
    	  	if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.Instance] && adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          FrascatiAddInstance(adaptationPrimitive,nodeType.getNodeName,nodeType.getBootStrapperService);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "AddBinding" => {
        if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.MBinding] &&  adaptationPrimitive.getRef.asInstanceOf[org.kevoree.MBinding].getPort.
            eContainer.asInstanceOf[ComponentInstance].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new AddBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);

      }
      case "UpdateBinding" => {
        if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.MBinding] && adaptationPrimitive.getRef.asInstanceOf[org.kevoree.MBinding].getPort.
            eContainer.asInstanceOf[ComponentInstance].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new UpdateBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }

      case "RemoveBinding" => {
        if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.MBinding] && adaptationPrimitive.getRef.asInstanceOf[org.kevoree.MBinding].getPort.
            eContainer.asInstanceOf[ComponentInstance].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new RemoveBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "AddFragmentBinding" => {
        if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.Channel] &&  adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Channel].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new AddFragmentBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);

      }
      case "RemoveFragmentBinding" => {
    	  if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.Channel] && adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Channel].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new RemoveFragmentBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "UpdateFragmentBinding" => {
    	 if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.Channel] &&  adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Channel].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new UpdateFragmentBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "StartInstance" => {
    	  	if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.Instance] && adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new StartInstance(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "StopInstance" => {
    	  	if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.Instance] && adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new StopInstance(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "StartThirdParty" => {
                  node.getSuperPrimitive(adaptationPrimitive);

      }
      case "RemoveThirdParty" => {
        FrascatiRemoveDeployUnit(adaptationPrimitive.getRef.asInstanceOf[DeployUnit],nodeType.getBootStrapperService,topKCL)
      }
      case _@ e => { null }

    }

  }

  
  //////////// CRUD Binding //////////////////
  case class RemoveBinding(adapptationPrimitive: AdaptationPrimitive) extends PrimitiveCommand {
    override def execute(): Boolean = { true }

    override def undo() = {}

  }

  case class AddBinding(adapptationPrimitive: AdaptationPrimitive) extends PrimitiveCommand {
    override def execute(): Boolean = { true }

    override def undo() = {}

  }

  case class UpdateBinding(adapptationPrimitive: AdaptationPrimitive) extends PrimitiveCommand {
    override def execute(): Boolean = { true }

    override def undo() = {}
  }

    
  //////////// CRUD FragmentBinding //////////////////
      class RemoveFragmentBinding(adapptationPrimitive: AdaptationPrimitive) extends PrimitiveCommand {
    override def execute(): Boolean = { true }

    override def undo() = {}

  }

  class AddFragmentBinding(adapptationPrimitive: AdaptationPrimitive) extends PrimitiveCommand {
    override def execute(): Boolean = { true }

    override def undo() = {}

  }

    class UpdateFragmentBinding(adapptationPrimitive: AdaptationPrimitive) extends PrimitiveCommand {
    override def execute(): Boolean = { true }

    override def undo() = {}
  }

  

      //////////// CRUD Instance //////////////////
    class AddInstance(adaptationPrimitive: AdaptationPrimitive) extends AInstance {
    override def execute(): Boolean = { 
      println("pass par la AddInstance")
      
      if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.ComponentInstance]  && adaptationPrimitive.getRef.asInstanceOf[org.kevoree.ComponentInstance].getTypeDefinition.asInstanceOf[org.kevoree.ComponentType].getBean.endsWith(".composite")){
    	  
    	  AdaptatationPrimitiveFactory.frascati.getComposite(adaptationPrimitive.getRef.asInstanceOf[org.kevoree.ComponentInstance].getTypeDefinition.asInstanceOf[org.kevoree.ComponentType].getBean)
      }
      else if(adaptationPrimitive.getRef.isInstanceOf[org.kevoree.ComponentInstance]){      
        val s = generateComponent(adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getName,adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getTypeDefinition.asInstanceOf[org.kevoree.ComponentType].getBean, adaptationPrimitive.getRef.asInstanceOf[org.kevoree.ComponentInstance] )
        val f = java.io.File.createTempFile(adaptationPrimitive.getRef.asInstanceOf[org.kevoree.ComponentInstance].getName,".composite")
        val output = new java.io.FileOutputStream(f)
        val writer = new PrintWriter(output)
        writer.print(s);
        writer.flush()
        writer.close()
        output.close()
        println(f.getAbsolutePath)
        AdaptatationPrimitiveFactory.frascati.getComposite(f.getAbsolutePath())
        
      }
      
      
      true }

    override def undo() = {}

  }

    class RemoveInstance(adapptationPrimitive: AdaptationPrimitive) extends AInstance {
    var c  : Component = _
      override def execute(): Boolean = { 
      var c  : Component =AdaptatationPrimitiveFactory.frascati.getCompositeManager().getComposite(  adapptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getName)
      AdaptatationPrimitiveFactory.frascati.close(c)
      true 
     }

        override def undo() = {
    	  var v = File.createTempFile(adapptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getName,"composite")
    	  //adapptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getTypeDefinition.get
    	  
        }
     }

    abstract class AInstance extends PrimitiveCommand {
    def printService(serviceName : String, interfaceName: String) : Node ={   
    		  	<service name={serviceName}>
    			  <interface.java interface={interfaceName}/>
    			  </service>
    }

        def printServiceComposite(componentName: String,serviceName : String) : Node ={   
    		<service name={serviceName} promote={componentName + "/" +serviceName}/>
    }
        
        def printReferenceComposite(componentName: String,serviceName : String) : Node ={   
          <reference name={serviceName} promote={componentName + "/" + serviceName} />
    }


    def printReference(serviceName : String, interfaceName: String) : Node = {  
    		<reference name={serviceName}>
    			  <interface.java interface={interfaceName}/>
    			  </reference>
    }

    def properties(propertyName : String, value: String) : Node = {  
    		<property name={propertyName}>{value}</property>
    			  
    }

    
    def generateComponent(componentName : String, componentJavaClass:String,instance:org.kevoree.ComponentInstance):String ={
          	  "<?xml version=\"1.0\" encoding=\"ISO-8859-15\"?>"+
    		<composite xmlns={"http://www.osoa.org/xmlns/sca/1.0"} name={componentName}>
    			 { var res =  Queue[Node]() ; instance.getTypeDefinition.asInstanceOf[org.kevoree.ComponentType].getProvided.foreach(e => res ++= printServiceComposite(componentName+"_internal_",e.getName))
    			  instance.getTypeDefinition.asInstanceOf[org.kevoree.ComponentType].getRequired.foreach(e => res++=printReferenceComposite(componentName+"_internal_",e.getName))
    			  res
    			 }
    			  <component name={componentName+"_internal_"}>    			  
    			  <implementation.java class={componentJavaClass}/>
        		{	var  res =  Queue[Node](); instance.getTypeDefinition.asInstanceOf[org.kevoree.ComponentType].getProvided.foreach(e => res ++=printService(e.getName, e.getRef.getName));
        			  instance.getTypeDefinition.asInstanceOf[org.kevoree.ComponentType].getRequired.foreach(e => res ++=printReference(e.getName, e.getRef.getName))
        			  if (!instance.getDictionary.isEmpty)
        				  instance.getDictionary.get.getValues.foreach(value=> res ++=properties(value.getAttribute.getName,value.getValue))
    			res}
        			  
        			  </component>
    			  </composite>.toString()
    }
    

  }

  
    class StartInstance(adapptationPrimitive: AdaptationPrimitive) extends PrimitiveCommand {
    var lf: LifeCycleController =_
      override def execute(): Boolean = { 
       if (lf != null){
         var c  : Component =AdaptatationPrimitiveFactory.frascati.getCompositeManager().getComposite(  adapptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getName)
         lf= c.getFcInterface("lifecycle-controller").asInstanceOf[LifeCycleController];
       }       
       lf.startFc()
      true
    }

    override def undo() = {
    	lf.stopFc()

      
    }

  }

      class StopInstance(adapptationPrimitive: AdaptationPrimitive) extends PrimitiveCommand {
    var lf: LifeCycleController =_
      override def execute(): Boolean = { 
       if (lf != null){
         var c  : Component =AdaptatationPrimitiveFactory.frascati.getCompositeManager().getComposite(  adapptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getName)
         lf= c.getFcInterface("lifecycle-controller").asInstanceOf[LifeCycleController];
       }       
       lf.stopFc()
      true
    }

    override def undo() = {
    	lf.startFc()
    }


  }

      //////////// CRUD Dico //////////////////
  
  class UpdateDictionaryInstance(adapptationPrimitive: AdaptationPrimitive) extends PrimitiveCommand {
    var oldDico :org.kevoree.Dictionary = _
    
    override def execute(): Boolean = { 
      var c  : Component =AdaptatationPrimitiveFactory.frascati.getCompositeManager().getComposite(  adapptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getName)
      
      var content: ContentController = c.getFcInterface("content-controller").asInstanceOf[ContentController];
		 content.getFcSubComponents().apply(0).getFcInterfaces().foreach(o=> System.err.println(o))
		 var attr  =content.getFcSubComponents().apply(0).getFcInterfaces().filter(o=> o.isInstanceOf[AttributeController]).apply(0)
		 if (attr != null){
			 var att  =attr.asInstanceOf[AttributeController]
			 //TODO set properties
			 adapptationPrimitive.getRef.asInstanceOf[Instance].getDictionary.get
		 }
      true
      
      
    }

    override def undo() = {
      //TODO
    }

  }

}