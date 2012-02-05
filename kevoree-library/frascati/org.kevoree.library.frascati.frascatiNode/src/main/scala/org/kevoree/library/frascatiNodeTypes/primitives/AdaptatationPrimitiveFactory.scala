package org.kevoree.library.frascatiNodeTypes.primitives;
import org.kevoreeAdaptation.AdaptationPrimitive
import org.kevoree.library.frascatiNodeTypes.FrascatiNode
import org.eclipse.stp.sca.ComponentType
import org.kevoree.ComponentInstance
import org.ow2.frascati.FraSCAti
import org.objectweb.fractal.api.Component
import org.objectweb.fractal.api.control.ContentController
import org.objectweb.fractal.api.control.AttributeController
import org.kevoree.Instance
import scala.collection.JavaConversions._
import org.objectweb.fractal.api.control.LifeCycleController
import java.io.File
import org.kevoree.api.PrimitiveCommand


object AdaptatationPrimitiveFactory {

  	var _frascati :FraSCAti = _ ;
	def frascati :FraSCAti = _frascati;
  	
  
  def getPrimitive(adaptationPrimitive: AdaptationPrimitive, node: FrascatiNode): org.kevoree.api.PrimitiveCommand = {

    //        values = {"UpdateType", "UpdateDeployUnit", "AddType", "AddDeployUnit", "AddThirdParty", 
    //"RemoveType", "RemoveDeployUnit", "UpdateInstance", "UpdateBinding", "UpdateDictionaryInstance", 

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
        node.getSuperPrimitive(adaptationPrimitive);
      }
      case "AddThirdParty" => {
        node.getSuperPrimitive(adaptationPrimitive);
      }
      case "RemoveType" => {
        node.getSuperPrimitive(adaptationPrimitive);
      }
      case "RemoveDeployUnit" => {
        node.getSuperPrimitive(adaptationPrimitive);
      }
      case "UpdateDictionaryInstance" => {
    	  	if (adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new UpdateDictionaryInstance(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
        
        
        new UpdateDictionaryInstance(adaptationPrimitive);
      }
      case "UpdateInstance" => {
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "RemoveInstance" => {
    	  	if (adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new RemoveInstance(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "AddInstance" => {
    	  	if (adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new AddInstance(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "AddBinding" => {
        if (adaptationPrimitive.getRef.asInstanceOf[org.kevoree.MBinding].getPort.
            eContainer.asInstanceOf[ComponentInstance].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new AddBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);

      }
      case "UpdateBinding" => {
        if (adaptationPrimitive.getRef.asInstanceOf[org.kevoree.MBinding].getPort.
            eContainer.asInstanceOf[ComponentInstance].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new UpdateBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }

      case "RemoveBinding" => {
        if (adaptationPrimitive.getRef.asInstanceOf[org.kevoree.MBinding].getPort.
            eContainer.asInstanceOf[ComponentInstance].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new RemoveBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "AddFragmentBinding" => {
        if (adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Channel].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new AddFragmentBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);

      }
      case "RemoveFragmentBinding" => {
    	  if (adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Channel].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new RemoveFragmentBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "UpdateFragmentBinding" => {
    	 if (adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Channel].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new UpdateFragmentBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "StartInstance" => {
    	  	if (adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new StartInstance(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "StopInstance" => {
    	  	if (adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getTypeDefinition.getDeployUnits.forall(e =>
              e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new StopInstance(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "StartThirdParty" => {
                  node.getSuperPrimitive(adaptationPrimitive);

      }
      case "RemoveThirdParty" => {
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case _@ e => { null }

    }

  }

  
  //////////// CRUD Binding //////////////////
  class RemoveBinding(adapptationPrimitive: AdaptationPrimitive) extends PrimitiveCommand {
    override def execute(): Boolean = { true }

    override def undo() = {}

  }

  class AddBinding(adapptationPrimitive: AdaptationPrimitive) extends PrimitiveCommand {
    override def execute(): Boolean = { true }

    override def undo() = {}

  }

    class UpdateBinding(adapptationPrimitive: AdaptationPrimitive) extends PrimitiveCommand {
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
    class AddInstance(adapptationPrimitive: AdaptationPrimitive) extends PrimitiveCommand {
    override def execute(): Boolean = { true }

    override def undo() = {}

  }

    class RemoveInstance(adapptationPrimitive: AdaptationPrimitive) extends PrimitiveCommand {
    var c  : Component = _
      override def execute(): Boolean = { 
      var c  : Component =AdaptatationPrimitiveFactory.frascati.getCompositeManager().getComposite(  adapptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getName)
      AdaptatationPrimitiveFactory.frascati.close(c)
      true 
     }

    def printService(serviceName : String, interfaceName: String) : String ={   
    		  	<service name={serviceName}>
    			  <interface.java interface={interfaceName}/>
    			  </service>
      .toString()
    }

        def printServiceComposite(serviceName : String, componentName: String) : String ={   
    		<service name={serviceName} promote={componentName + "/" +serviceName}/>
    		.toString()
    }
        
        def printReferenceComposite(serviceName : String, componentName: String) : String ={   
          <reference name={serviceName} promote={componentName + "/" + serviceName} />
    		.toString()
    }


    def printReference(serviceName : String, interfaceName: String) : String = {  
    		<reference name={serviceName}>
    			  <interface.java interface={interfaceName}/>
    			  </reference>.toString()
    }

    def properties(propertyName : String, value: String) : String = {  
    		<property name={propertyName}>{value}</property>
    			  .toString()
    }

    
    def generateComponent(componentName : String, componentJavaClass:String,instance:org.kevoree.ComponentInstance):String ={
          	  "<?xml version=\"1.0\" encoding=\"ISO-8859-15\"?>"+
    		(<composite xmlns={"http://www.osoa.org/xmlns/sca/1.0"} name={componentName}>
    			  {/*generate promote*/}
    			  <component name={componentName+"_internal_"}>    			  
    			  <implementation.java class={componentJavaClass}/>
        			  {/*generate service*/}
        			  {/*generate reference*/}
        			  {
        			    instance.getDictionary.get.getValues.foreach(value=> properties(value.getAttribute.getName,value.getValue))
        			  }
        			  </component>
    			  </composite>.toString())
    }
    
    override def undo() = {
    	  var v = File.createTempFile(adapptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getName,"composite")
    	  
    	  
    	  
    	  //adapptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getTypeDefinition.get
    	  
    	  
    	  
    	  
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
		 var attr  =content.getFcSubComponents().apply(0).getFcInterfaces().map(o=> o.isInstanceOf[AttributeController]).apply(0)
		 if (attr!=null){
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