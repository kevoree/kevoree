package org.kevoree.library.frascatiNodeTypes;


import java.net.URL;
import java.net.URLClassLoader;

import org.objectweb.fractal.api.control.ContentController;
import org.ow2.frascati.FraSCAti;
import org.ow2.frascati.assembly.factory.api.ClassLoaderManagerFcInItf;
import org.ow2.frascati.assembly.factory.api.ClassLoaderManagerInterceptorSCAIntent;
import org.ow2.frascati.assembly.factory.api.ClassLoaderManagerInterceptorSCALCb56bb98SCACCIntent;
import org.ow2.frascati.component.factory.api.MembraneGenerationFcInItf;
import org.ow2.frascati.util.FrascatiClassLoader;


public class Test {


	
	
	public static void main( String[] args ) throws Exception {
	
		System.err.println(((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs());
		
		/*for (URL u : ((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs()){
			System.err.println(u);
		}*/
		
		org.ow2.frascati.tinfi.api.control.SCABasicIntentController o1;
		FraSCAti frascati = FraSCAti.newFraSCAti();
		System.err.println(((ClassLoaderManagerFcInItf)frascati.getClassLoaderManager()).getFcItfImpl().getClass());
		ClassLoaderManagerInterceptorSCAIntent a  =(ClassLoaderManagerInterceptorSCAIntent) ((ClassLoaderManagerFcInItf)frascati.getClassLoaderManager()).getFcItfImpl();
		System.err.println(frascati.getClassLoaderManager().getClassLoader());
		System.err.println(a.getClassLoader());
		System.err.println(a.getFcItfDelegate().getClass());
		ClassLoaderManagerInterceptorSCAIntent b = (ClassLoaderManagerInterceptorSCAIntent) a.getFcItfDelegate();
		System.err.println(a + " "+ b);

		System.err.println(((ClassLoaderManagerFcInItf)frascati.getClassLoaderManager()).getClassLoader());
		
		System.err.println(b.getFcItfDelegate().getClass());
		
		ClassLoaderManagerInterceptorSCALCb56bb98SCACCIntent d = (ClassLoaderManagerInterceptorSCALCb56bb98SCACCIntent) b.getFcItfDelegate();
		System.err.println(d.getClassLoader());
		System.err.println(d.getFcItfDelegate());
		
		System.err.println(frascati.getMembraneGeneration());
		
		MembraneGenerationFcInItf f  =(MembraneGenerationFcInItf) frascati.getMembraneGeneration();
		
		
		
//		org.ow2.frascati.util.FrascatiClassLoader f = new FrascatiClassLoader(Thread.currentThread().getContextClassLoader());
//		 frascati.setClassLoader(f );
	
		 //Launcher launcher = new Launcher("helloworld1-pojo.composite", frascati);
		 URL url[] = new URL[1];
		 url[0] = new URL("file:/opt/frascati-runtime-1.4/examples/helloworld-pojo/target/helloworld-pojo-1.4.jar");
		 frascati.getClassLoaderManager().loadLibraries(url);
	/*	 
		 
		 
		 
		 
		 org.objectweb.fractal.api.Component c = frascati.getComposite("/tmp/server1463694843683778624.composite");//1helloworld1-pojo.composite");
//		 org.objectweb.fractal.api.Component c = frascati.getComposite("2hel

		 
		 ContentController content = (ContentController) c.getFcInterface("content-controller");
		
		 
		 for (Object o :  content.getFcSubComponents()[0].getFcInterfaces()){
			 System.err.println(o);
		 }
		 System.err.println(content.getFcSubComponents()[0].getFcInterface("printService"));
		 
		 //PrintService s = (PrintService)content.getFcSubComponents()[0].getFcInterface("printService");
		 	
		 //s.print("Toto");
		 
//		 launcher.
		*/ 
		 //org.ow2.frascati.remote.introspection.resources.ObjectFactory fact =  new org.ow2.frascati.remote.introspection.resources.ObjectFactory();
		 
		 
		 
	    
	      
	      
	
	}
	
}
