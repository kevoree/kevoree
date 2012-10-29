package org.kevoree.library.sky.provider.web

import org.kevoree.{ContainerNode, ContainerRoot, TypeDefinition}
import org.kevoree.framework.KevoreePropertyHelper
import java.io.File
import io.Source
import xml.Node
import scala.collection.JavaConversions._
import org.kevoree.library.sky.api.helper.KloudHelper
import org.slf4j.LoggerFactory

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 24/10/12
 * Time: 16:08
 *
 * @author Erwan Daubert
 * @version 1.0
 */
object HTMLPageBuilder {
  val logger = LoggerFactory.getLogger(this.getClass)

  def getIaasPage (pattern: String, nodeName: String, model: ContainerRoot): String = {
    (<html>
      <head>
        <link rel="stylesheet" href={pattern + "bootstrap/bootstrap.min.css"}/>
      </head>
      <body>
        <img height="200px" src={pattern + "scaled500.png"} alt="Kevoree"/>
        <ul class="breadcrumb">
          <li class="active">
            <a href={pattern}>Home</a> <span class="divider">/</span>
          </li>
        </ul>{val nodesList = model.getNodes.find(n => n.getName == nodeName) match {
        case None => List[ContainerNode]()
        case Some(node) => node.getHosts.toList
      }
      nodeList(pattern, model, nodesList)}

      </body>
    </html>).toString()
  }

  def getPaasPage (pattern: String, model: ContainerRoot): String = {
    (<html>
      <head>
        <link rel="stylesheet" href={pattern + "bootstrap/bootstrap.min.css"}/>
      </head>
      <body>
        <ul class="breadcrumb">
          <li class="active">
            <a href={pattern}>Home</a> <span class="divider">/</span>
          </li>
        </ul>
        <table class="table table-bordered">
          <thead>
            <tr>
              <td>User name
              </td> <td>number of nodes</td> <td>action(s)</td>
            </tr>
          </thead>
          <tbody>
            {var result: List[scala.xml.Elem] = List()
          KloudHelper.getKloudUserGroups(model).foreach {
            group => {
              logger.debug("{} is a user and he/she has {} nodes", group.getName, group.getSubNodes)
              result = result ++ List(
                                       <tr>
                                         <td>
                                           {group.getName}
                                         </td>
                                         <td>
                                           <a href={pattern + group.getName}>
                                             <span class="label notice">
                                               {group.getSubNodes.length}
                                             </span>
                                           </a>
                                         </td>
                                         <td>
                                           <button type="button" class="btn btn-warning">
                                             <a href={pattern + group.getName + "/release"}>release</a>
                                           </button>
                                         </td>
                                       </tr>
                                     )
            }
          }
          result}
          </tbody>
        </table>
      </body>
    </html>).toString()
  }

  def getPaasUserPage (login: String, pattern: String, model: ContainerRoot): String = {
    (<html>
      <head>
        <link rel="stylesheet" href={pattern + "bootstrap/bootstrap.min.css"}/>
        <link rel="stylesheet" href={pattern + "fileuploader/fileuploader.css"}/>
        <link rel="stylesheet" href={pattern + "bootstrap/css/bootstrap.min.css"}/>
        <link rel="stylesheet" href={pattern + "bootstrap/css/bootstrap-responsive.min.css"}/>
        <script type="text/javascript" src={pattern + "jquery/jquery.min.js"}></script>
        <script type="text/javascript" src={pattern + "jquery/jquery.form.js"}></script>
        <script type="text/javascript" src={pattern + "bootstrap/js/bootstrap.min.js"}></script>
        <script type="text/javascript" src={pattern + "fileuploader/fileuploader.js"}></script>
        <script type="text/javascript" src={pattern + "fileuploader-init.js"}></script>
      </head>
      <body>
        <ul class="breadcrumb">
          <li class="active">
            <a href={pattern}>Home</a> <span class="divider">/</span> <a href={pattern + login}>
            {login}
          </a> <span class="divider">/</span>
          </li>
        </ul>{val nodesList = model.getGroups.find(g => g.getName == login) match {
        case None => List[ContainerNode]()
        case Some(group) => group.getSubNodes
      }
      nodeList(pattern, model, nodesList)}

      </body>
    </html>).toString()
  }

  def getNodeLogPage (pattern: String, parentNodeName: String, nodeName: String, streamName: String, model: ContainerRoot): String = {
    (<html>
      <head>
        <link rel="stylesheet" href={pattern + "bootstrap/bootstrap.min.css"}/>
      </head>
      <body>
        <ul class="breadcrumb">
          <li>
            <a href={pattern}>Home</a> <span class="divider">/</span>
          </li>
          <li>
            <a href={pattern + "nodes/" + nodeName}>
              {nodeName}
            </a> <span class="divider">/</span>
          </li>
          <li class="active">
            <a href={pattern + "nodes/" + nodeName + "/" + streamName}>
              {streamName}
            </a>
          </li>
        </ul>{var result: List[scala.xml.Elem] = List()
      model.getNodes.find(n => n.getName == parentNodeName) match {
        case None =>
        case Some(node) => {
          node.getHosts.find(n => n.getName == nodeName) match {
            case None =>
              result = result ++ List(
                                       <div class="alert-message block-message error">
                                         <p>Node instance not hosted on this platform</p>
                                       </div>
                                     )
            case Some(child) =>
              result = result ++ List(
                                       <div class="alert-message block-message info">
                                         {streamName match {
                                         case "out" => {
                                           var subresult: List[scala.xml.Elem] = List()
                                           val logFolderOption = KevoreePropertyHelper.getStringPropertyForNode(model, parentNodeName, "log_folder")
                                           val file = new File(logFolderOption.getOrElse(System.getProperty("java.io.tmpdir")) + File.separator + nodeName + ".log.out")
                                           if (file.exists()) {
                                             Source.fromFile(file).getLines().toList /*.reverse*/ .foreach {
                                               line =>
                                                 subresult = subresult ++ List(<p>
                                                   {line}
                                                 </p>)
                                             }
                                           } else {
                                             subresult = subresult ++ List(<p>Unable to find the log file</p>)
                                           }

                                           subresult
                                         }
                                         case "err" => {
                                           var subresult: List[scala.xml.Elem] = List()
                                           val logFolderOption = KevoreePropertyHelper.getStringPropertyForNode(model, parentNodeName, "log_folder")
                                           val file = new File(logFolderOption.getOrElse(System.getProperty("java.io.tmpdir")) + File.separator + nodeName + ".log.err")
                                           if (file.exists()) {
                                             Source.fromFile(file).getLines().toList /*.reverse*/ .foreach {
                                               line =>
                                                 subresult = subresult ++ List(<p>
                                                   {line}
                                                 </p>)
                                             }
                                           } else {
                                             subresult = subresult ++ List(<p>Unable to find the log file</p>)
                                           }
                                           subresult
                                         }
                                         case _ => "unknow stream"
                                       }}
                                       </div>
                                     )
          }
        }
      }
      result}
      </body>
    </html>).toString()
  }

  def getNodePage (pattern: String, parentNodeName: String, nodeName: String, model: ContainerRoot): String = {
    (<html>
      <head>
        <link rel="stylesheet" href={pattern + "bootstrap/bootstrap.min.css"}/>
      </head>
      <body>
        <ul class="breadcrumb">
          <li>
            <a href={pattern}>Home</a> <span class="divider">/</span>
          </li>
          <li class="active">
            <a href={pattern + "nodes/" + nodeName}>
              {nodeName}<span class="divider">/</span>
            </a>
          </li>
        </ul>{var result: List[scala.xml.Elem] = List()
      model.getNodes.find(n => n.getName == parentNodeName) match {
        case None =>
        case Some(node) => {
          node.getHosts.find(n => n.getName == nodeName) match {
            case None =>
              result = result ++ List(
                                       <div class="alert-message block-message error">
                                         <p>Node instance not hosted on this platform</p>
                                       </div>
                                     )
            case Some(child) =>
              result = result ++ List(
                                       <div class="alert-message block-message info">
                                         <p>
                                           <a href={pattern + "nodes/" + nodeName + "/out"}>Output log</a>
                                         </p>
                                         <p>
                                           <a href={pattern + "nodes/" + nodeName + "/err"}>Error log</a>
                                         </p>
                                       </div>
                                     )
          }
        }
      }
      result}
      </body>
    </html>).toString()
  }

  private def nodeList (pattern: String, model: ContainerRoot, nodeList: List[ContainerNode]): Seq[Node] = {
    (<table class="table table-bordered">
      <thead>
        <tr>
          <td>#
            <button type="button" class="btn btn-success">
              <a href={pattern + "AddChild"}>add child</a>
            </button>
          </td> <td>virtual node</td> <td>ip</td> <td>action(s)</td>
        </tr>
      </thead>
      <tbody>
        {var result: List[scala.xml.Elem] = List()
      /*model.getNodes.find(n => n.getName == nodeName) match {
                case None =>
                case Some(node) => {*/
      nodeList.foreach {
        child => {
          val ips = KevoreePropertyHelper.getStringNetworkProperties(model, child.getName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
          val ipString = ips.mkString(", ")
          result = result ++ List(
                                   <tr>
                                     <td>
                                       {nodeList.indexOf(child)}
                                     </td>
                                     <td>
                                       <a href={pattern + "nodes/" + child.getName}>
                                         <span class="label notice">
                                           {child.getName}
                                         </span>
                                       </a>
                                     </td>
                                     <td>
                                       {ipString}
                                     </td>
                                     <td>
                                       <button type="button" class="btn btn-warning">
                                         <a href={pattern + "RemoveChild?name=" + child.getName}>delete</a>
                                       </button>
                                     </td>
                                   </tr>
                                 )
        }
        /* }
                  }*/
      }
      result}
      </tbody>
    </table>)
  }

  def addNodePage (pattern: String, paasNodeList: List[TypeDefinition]): String = {
    (<html>
      <head>
        <link rel="stylesheet" href={pattern + "bootstrap/bootstrap.min.css"}/>
        <link rel="stylesheet" href={pattern + "add_child.css"}/>
        <script type="text/javascript" src={pattern + "jquery/jquery.min.js"}/>
        <script type="text/javascript" src={pattern + "jquery/jquery.form.js"}/>
        <script type="text/javascript" src={pattern + "bootstrap/bootstrap.min.js"}/>
        <script type="text/javascript" src={pattern + "add_child.js"}/>

      </head>
      <body>
        <ul class="breadcrumb">
          <li>
            <a href={pattern}>Home</a> <span class="divider">/</span>
          </li>
          <li class="active">
            <a href={pattern + "AddChild"}>AddChild
              <span class="divider">/</span>
            </a>
          </li>
        </ul>
        <!--<img height="200px" src="/scaled500.png" alt="Kevoree"/>-->
        <ul class="breadcrumb">
          <li class="active">
            <a href={pattern}>Add PaaS Node</a> <span class="divider">/</span>
          </li>
        </ul>

        <form id="formNodeType" class="bs-docs-example form-horizontal" action=" " method=" ">
          <div class="control-group" id="nodeTypeList">
            <label class="control-label" for="nodeType">NodeType</label>
            <div class="controls">
              <select id="nodeType">
                <option value="JavaSENode">JavaSENode</option>
              </select>
            </div>
          </div>
        </form>
      </body>
    </html>).toString()
  }

}
