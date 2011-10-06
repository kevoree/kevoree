package org.kevoree.sky.jclouds.ec2.command

import org.kevoree.sky.jclouds.ec2.JCloudsNode
import org.slf4j.LoggerFactory
import java.util.Properties
import org.jclouds.compute.{ComputeService, ComputeServiceContextFactory, ComputeServiceContext}
import org.jclouds.compute.options.TemplateOptions.Builder._
import org.jclouds.scriptbuilder.domain.Statement
import com.google.common.collect.ImmutableSet
import com.google.inject.Module
import org.jclouds.sshj.config.SshjSshClientModule
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule
import org.jclouds.enterprise.config.EnterpriseConfigurationModule
import com.google.common.io.Files
import java.io.File
import org.jclouds.io.{Payloads, Payload}
import com.google.common.base.{Predicate, Charsets}
import org.jclouds.compute.domain.{OsFamily, TemplateBuilder}
import java.nio.charset.Charset
import org.jclouds.scriptbuilder.statements.login.{UserAdd, AdminAccess}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 26/09/11
 * Time: 15:11
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class JCloudsHelper (node: JCloudsNode) {

  private final val logger = LoggerFactory.getLogger(classOf[JCloudsHelper])

  private def initProperties: Properties = {
    val properties: Properties = new Properties
    if (node.getDictionary.get("accesskeyid") == null || node.getDictionary.get("secretkey") == null) {
      logger.error("missing value on parameter \"accesskeyid\" or \"secretkey\"")
      return null
    }
    else {
      properties.put("accesskeyid", node.getDictionary.get("accesskeyid"))
      properties.put("secretkey", node.getDictionary.get("secretkey"))
    }
    val endPoint: String = node.getDictionary.get("endPoint").asInstanceOf[String]
    if (endPoint != null && !(endPoint == "")) {
      properties.put("endpoint", endPoint)
    }
    properties
  }

  private def getComputeServiceContextFactory (): ComputeServiceContext = {
    val properties = initProperties
    val computeServiceContext = new ComputeServiceContextFactory()
      .createContext(node.getDictionary.get("provider").asInstanceOf[String], properties)

    val computeService = computeServiceContext.getComputeService

    null
  }

  private def configureVM (compute: ComputeService) {
    var statement : Statement = null
    val templateBuilder: TemplateBuilder = compute.templateBuilder
    templateBuilder.osFamily(OsFamily.UBUNTU).osVersionMatches("10.04").os64Bit(true)
    templateBuilder.minCores(node.getIntegerProperty(JCloudsNode.minCores))
    templateBuilder.minRam(node.getIntegerProperty(JCloudsNode.minRam))

    // TODO add ssh implementation to allow direct access of the VM
    /*var modules: Iterable[Module] = ImmutableSet.of[Module](new SshjSshClientModule, new SLF4JLoggingModule,
                                                                   new EnterpriseConfigurationModule)
          return new ComputeServiceContextFactory().createContext(provider, identity, credential, modules, properties)
            .getComputeService*/
    /*return new Credentials(System.getProperty("user.name"), Files.toString(
                   new File(System.getProperty("user.home") + "/.ssh/id_rsa"), UTF_8)); // => to use the local private rsa key to connect by ssh*/

    // authorized a specific ssh public key
    val f = new File(node.getStringProperty(JCloudsNode.rsaKey))
    if (f.exists() && Files.toString(f, Charset.forName("UTF8")).startsWith("ssh-rsa ")) {
      templateBuilder.options(authorizePublicKey(Payloads.newPayload(f)))

      // create a non-user root
      val value = node.getStringProperty(JCloudsNode.nonRootUser)
      if (value != null && !value.contains(" ")) {
        val userBuilder = UserAdd.builder()
        userBuilder.login(value)
        userBuilder.authorizeRSAPublicKey(Files.toString(f, Charset.forName("UTF8")))
        userBuilder.installRSAPrivateKey()
        statement = userBuilder.build()
      } else {
        logger.debug(JCloudsNode.nonRootUser + " is not well defined")
      }
    } else {
      logger.debug(JCloudsNode.rsaKey + " has not a valid value: " + node.getStringProperty(JCloudsNode.rsaKey))
    }

    // start 10 nodes
    /*nodes = client.createNodesInGroup(group, 10, installPrivateKey(Files.toString(new File("/home/me/.ssh/id_rsa"));*/

    // TODO add a post boot script => this can be added as options on the VM configuration
    // start 10 nodes
    /*nodes = client.createNodesInGroup(group, 10, runScript(Files.toString(new File("runscript.sh")));*/


    // TODO open required port
    /*Template template = client.templateBuilder().options(inboundPorts(22, 8080)).build();
            // start 2 nodes
            nodes = client.createNodesInGroup(group, 2, template);*/

    // TODO get all commands to run

    // note this will create a user with the same name as you on the
    // node. ex. you can connect via ssh publicip
    //    val bootInstructions: Statement = AdminAccess.standard
    // to run commands as root, we use the runScript option in the template.
    templateBuilder.options(runScript(statement))
  }

  private def createVMOnGroup () {
    // TODO parameters must be an image and a group identification
  }

  private def findInstanceByName (name: String, computeService: ComputeService) {
    computeService.listNodesDetailsMatching()

  }

  /*private def findInstanceByKeyName (client: EC2Client, keyName: String): RunningInstance = {
    val reservations = client.getInstanceServices.describeInstancesInRegion(null)
    val allInstances = new HashSet[RunningInstance]()
    import scala.collection.JavaConversions._
    reservations.foreach{
      reservation =>
      allInstances.add(reservation)
    }
    allInstances.find(instance => instance.getKeyName == keyName && instance.getInstanceState != InstanceState.TERMINATED) match {
      case Some(i) => i
      case None => null
    }
  }*/

}