package org.kevoree.library.frascatiNodeTypes;


import org.objectweb.fractal.api.control.ContentController;
import org.ow2.frascati.FraSCAti;
import org.ow2.frascati.util.FrascatiClassLoader;


public class Test {


	
	
	public static void main( String[] args ) throws Exception {
		 
		FraSCAti frascati = FraSCAti.newFraSCAti();
		 org.ow2.frascati.util.FrascatiClassLoader f = new FrascatiClassLoader(Thread.currentThread().getContextClassLoader());
		 frascati.setClassLoader(f );
		 
		 //Launcher launcher = new Launcher("helloworld1-pojo.composite", frascati);
		 org.objectweb.fractal.api.Component first = frascati.getComposite("helloworld1-pojo.composite");
		 org.objectweb.fractal.api.Component c = frascati.getComposite("helloworld2-pojo.composite");
		 
		 ContentController content = (ContentController) c.getFcInterface("content-controller");
		
		 
		 for (Object o :  content.getFcSubComponents()[0].getFcInterfaces()){
			 System.err.println(o);
		 }
		 System.err.println(content.getFcSubComponents()[0].getFcInterface("printService"));
		 
		 //PrintService s = (PrintService)content.getFcSubComponents()[0].getFcInterface("printService");
		 	
		 //s.print("Toto");
		 
//		 launcher.
		 
		 //org.ow2.frascati.remote.introspection.resources.ObjectFactory fact =  new org.ow2.frascati.remote.introspection.resources.ObjectFactory();
		 
		 
		 
	    
	      
	      
	
	}
	
}
