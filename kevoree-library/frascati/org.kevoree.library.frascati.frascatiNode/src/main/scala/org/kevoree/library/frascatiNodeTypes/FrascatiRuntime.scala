package org.kevoree.library.frascatiNodeTypes

import actors.Actor
import org.ow2.frascati.FraSCAti
import org.ow2.frascati.util.FrascatiClassLoader
import org.kevoree.api.PrimitiveCommand
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.Enumeration
import java.lang.Class
import org.kevoree.kcl.KevoreeJarClassLoader

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/02/12
 * Time: 01:13
 */
case class ExecuteContextCommand(cmd: PrimitiveCommand)

case class UndoContextCommand(cmd: PrimitiveCommand)

class FrascatiRuntime extends Actor {

  private val logger = LoggerFactory.getLogger(this.getClass)
  private var internal_frascati: FraSCAti = null

  def startRuntime: FraSCAti = {
    (this !? StartRuntime()).asInstanceOf[FraSCAti]
  }

  def stopRuntime: Boolean = {
    (this !? StopRuntime()).asInstanceOf[Boolean]
  }

  case class StartRuntime()

  case class StopRuntime()

  def act() {
    while (true) {
      receive {
        case StartRuntime() => {

          val f_cl = new FrascatiClassLoader(classOf[FraSCAti].getClassLoader) {


            var urls = new java.util.ArrayList[java.net.URL]()

            //BAD WORKAROUND

            //TODO REMOVE THIS !!!!!

            //MANAGED NESTED JAR

            val userdir = System.getProperty("user.home");
            
            urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/javax/xml/stream/stax-api/1.0-2/stax-api-1.0-2.jar"))

            urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/aopalliance/aopalliance/1.0/aopalliance-1.0.jar"))
            urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/asm/asm/3.0/asm-3.0.jar"))
            urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/cglib/cglib-nodep/2.2.2/cglib-nodep-2.2.2.jar"))
            urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/eclipse/emf/common/2.4.0/common-2.4.0.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/eclipse/equinox/common/3.4.0/common-3.4.0.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/commons-cli/commons-cli/1.1/commons-cli-1.1.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/commons-collections/commons-collections/3.2.1/commons-collections-3.2.1.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/commons-lang/commons-lang/2.4/commons-lang-2.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/eclipse/jdt/core/3.3.0.771/core-3.3.0.771.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/apache/cxf/cxf-api/2.4.0/cxf-api-2.4.0.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/apache/cxf/cxf-common-utilities/2.4.0/cxf-common-utilities-2.4.0.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/apache/cxf/cxf-rt-core/2.4.0/cxf-rt-core-2.4.0.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/apache/cxf/cxf-tools-common/2.4.0/cxf-tools-common-2.4.0.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/apache/cxf/cxf-tools-validator/2.4.0/cxf-tools-validator-2.4.0.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/apache/cxf/cxf-tools-wsdlto-core/2.4.0/cxf-tools-wsdlto-core-2.4.0.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/apache/cxf/cxf-tools-wsdlto-databinding-jaxb/2.4.0/cxf-tools-wsdlto-databinding-jaxb-2.4.0.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/apache/cxf/cxf-tools-wsdlto-frontend-jaxws/2.4.0/cxf-tools-wsdlto-frontend-jaxws-2.4.0.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/dtdparser/dtdparser/1.21/dtdparser-1.21.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/eclipse/emf/ecore/2.4.0/ecore-2.4.0.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/objectweb/fractal/fraclet/java/fraclet-annotations/3.3/fraclet-annotations-3.3.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/objectweb/fractal/fractaladl/fractal-adl/2.3.1/fractal-adl-2.3.1.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/objectweb/fractal/fractal-api/2.0.2/fractal-api-2.0.2.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/objectweb/fractal/fractal-util/1.1.2/fractal-util-1.1.2.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/ow2/frascati/frascati-assembly-factory/1.4/frascati-assembly-factory-1.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/ow2/frascati/frascati-component-factory/1.4/frascati-component-factory-1.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/ow2/frascati/frascati-component-factory-juliac/1.4/frascati-component-factory-juliac-1.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/ow2/frascati/frascati-component-factory-juliac-jdt/1.4/frascati-component-factory-juliac-jdt-1.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/ow2/frascati/frascati-component-factory-juliac-tinfi-oo/1.4/frascati-component-factory-juliac-tinfi-oo-1.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/ow2/frascati/frascati-component-factory-tinfi-oo/1.4/frascati-component-factory-tinfi-oo-1.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/ow2/frascati/factory/frascati-factory-tools/1.4/frascati-factory-tools-1.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/ow2/frascati/frascati-implementation-fractal/1.4/frascati-implementation-fractal-1.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/ow2/frascati/frascati-metamodel-frascati/1.4/frascati-metamodel-frascati-1.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/eclipse/stp/sca/domainmodel/frascati-model/2.0.1.2/frascati-model-2.0.1.2.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/ow2/frascati/frascati-runtime-factory/1.4/frascati-runtime-factory-1.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/ow2/frascati/frascati-sca-parser/1.4/frascati-sca-parser-1.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/ow2/frascati/tinfi/frascati-tinfi-membranes-oo/1.4.4/frascati-tinfi-membranes-oo-1.4.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/ow2/frascati/tinfi/frascati-tinfi-mixins/1.4.4/frascati-tinfi-mixins-1.4.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/ow2/frascati/tinfi/frascati-tinfi-oo/1.4.4/frascati-tinfi-oo-1.4.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/ow2/frascati/tinfi/frascati-tinfi-runtime/1.4.4/frascati-tinfi-runtime-1.4.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/ow2/frascati/tinfi/frascati-tinfi-runtime-api/1.4.4/frascati-tinfi-runtime-api-1.4.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/ow2/frascati/frascati-util/1.4/frascati-util-1.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/apache/geronimo/specs/geronimo-javamail_1.4_spec/1.7.1/geronimo-javamail_1.4_spec-1.7.1.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/com/google/guava/guava/r07/guava-r07.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/com/sun/xml/bind/jaxb-impl/2.1.13/jaxb-impl-2.1.13.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/com/sun/xml/bind/jaxb-xjc/2.1.13/jaxb-xjc-2.1.13.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/jgrapht/jgrapht-jdk1.5/0.7.3/jgrapht-jdk1.5-0.7.3.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/objectweb/fractal/julia/julia-asm/2.5.2/julia-asm-2.5.2.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/objectweb/fractal/julia/julia-mixins/2.5.2/julia-mixins-2.5.2.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/objectweb/fractal/julia/julia-runtime/2.5.2/julia-runtime-2.5.2.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/objectweb/fractal/juliac/juliac-core/2.4/juliac-core-2.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/objectweb/fractal/juliac/juliac-jdt/2.4/juliac-jdt-2.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/objectweb/fractal/juliac/juliac-oo/2.4/juliac-oo-2.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/objectweb/fractal/juliac/juliac-runtime/2.4/juliac-runtime-2.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/objectweb/monolog/monolog/1.8/monolog-1.8.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/objectweb/monolog/monolog-api/1.8/monolog-api-1.8.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/apache/neethi/neethi/3.0.0/neethi-3.0.0.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/objenesis/objenesis/1.2/objenesis-1.2.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/kevoree/org.kevoree.adaptation.model/1.6.0-SNAPSHOT/org.kevoree.adaptation.model-1.6.0-SNAPSHOT.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/kevoree/org.kevoree.api/1.6.0-SNAPSHOT/org.kevoree.api-1.6.0-SNAPSHOT.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/kevoree/org.kevoree.framework/1.6.0-SNAPSHOT/org.kevoree.framework-1.6.0-SNAPSHOT.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/kevoree/org.kevoree.kcl/1.6.0-SNAPSHOT/org.kevoree.kcl-1.6.0-SNAPSHOT.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/kevoree/org.kevoree.kompare/1.6.0-SNAPSHOT/org.kevoree.kompare-1.6.0-SNAPSHOT.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/kevoree/library/javase/org.kevoree.library.javase.javaseNode/1.6.0-SNAPSHOT/org.kevoree.library.javase.javaseNode-1.6.0-SNAPSHOT.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/kevoree/org.kevoree.model/1.6.0-SNAPSHOT/org.kevoree.model-1.6.0-SNAPSHOT.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/kevoree/tools/org.kevoree.tools.annotation.api/1.6.0-SNAPSHOT/org.kevoree.tools.annotation.api-1.6.0-SNAPSHOT.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/kevoree/tools/org.kevoree.tools.javase.framework/1.6.0-SNAPSHOT/org.kevoree.tools.javase.framework-1.6.0-SNAPSHOT.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/oro/oro/2.0.8/oro-2.0.8.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/eclipse/stp/sca/osoa/java/osoa-java-api/2.0.1.2/osoa-java-api-2.0.1.2.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/eclipse/core/runtime/3.4.0/runtime-3.4.0.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/oasis-open/sca/j/sca-caa-apis/1.1-CD04/sca-caa-apis-1.1-CD04.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/eclipse/stp/sca/sca-model/2.0.1.2/sca-model-2.0.1.2.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/eclipse/stp/sca/introspection/sca-model-introspection/2.0.1.2/sca-model-introspection-2.0.1.2.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/scala-lang/scala-library/2.9.1/scala-library-2.9.1.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/slf4j/slf4j-api/1.6.4/slf4j-api-1.6.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/codehaus/woodstox/stax2-api/3.0.2/stax2-api-3.0.2.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/objectweb/fractal/fractaladl/task-deployment/2.3.1/task-deployment-2.3.1.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/objectweb/fractal/fractaladl/task-framework/2.3.1/task-framework-2.3.1.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/apache/velocity/velocity/1.6.4/velocity-1.6.4.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/codehaus/woodstox/woodstox-core-asl/4.1.1/woodstox-core-asl-4.1.1.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/wsdl4j/wsdl4j/1.6.2/wsdl4j-1.6.2.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/eclipse/emf/ecore/xmi/2.4.0/xmi-2.4.0.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/xml-resolver/xml-resolver/1.2/xml-resolver-1.2.jar"))
          urls.add(new java.net.URL("file:"+userdir+"/.m2/repository/org/apache/ws/xmlschema/xmlschema-core/2.0/xmlschema-core-2.0.jar"))
          urls.add(new URL("file:"+userdir+"/Documents/dev/dukeboard/kevoree/kevoree-library/frascati/org.kevoree.library.frascati.helloworld.pojo/target/org.kevoree.library.frascati.helloworld.pojo-1.6.0-SNAPSHOT.jar"))


            override def loadClass(p1: String): Class[_] = {
              val cl = classOf[FraSCAti].getClassLoader.loadClass(p1)
              logger.info("fload " + p1 + " ======> res " + cl)
              cl
            }

            override def getResources(p1: String): Enumeration[URL] = {
              logger.info("GetResss==" + p1)
              val res = classOf[FraSCAti].getClassLoader.getResources(p1)
              println("getresResult=" + res)
              res
            }

            override def getResource(p1: String): URL = {
              logger.info("GetRes=" + p1)
              classOf[FraSCAti].getClassLoader.getResource(p1)
            }

            override def getURLs(): Array[java.net.URL] = {
              logger.info("GETURL CLASSLOADER")
              println(urls);
             // val urls = classOf[FrascatiRuntime].getClassLoader.asInstanceOf[KevoreeJarClassLoader].getLoadedURLs
              urls.toArray(new Array[java.net.URL](urls.size))
            }

            override def addURL(p1: URL) {
              println("add URL " + p1)
            }
          }

          Thread.currentThread().setContextClassLoader(f_cl);
          internal_frascati = FraSCAti.newFraSCAti();
          internal_frascati.setClassLoader(f_cl)
          internal_frascati.getClassLoaderManager.setClassLoader(f_cl)

          reply(internal_frascati)
        }
        case StopRuntime() => {
          internal_frascati.close(internal_frascati.getComposite("org.ow2.frascati.FraSCAti"));
          internal_frascati = null;
          reply(true)
          exit()
        }
        case ExecuteContextCommand(cmd: PrimitiveCommand) => {
          try {
            reply(cmd.execute())
          } catch {
            case _@e => {
              logger.error("", e);
              reply(false)
            }
          }
        }
        case UndoContextCommand(cmd: PrimitiveCommand) => {
          try {
            cmd.undo()
            reply(true)
          } catch {
            case _@e => {
              logger.error("", e);
              reply(false)
            }
          }
        }
      }
    }
  }
}
