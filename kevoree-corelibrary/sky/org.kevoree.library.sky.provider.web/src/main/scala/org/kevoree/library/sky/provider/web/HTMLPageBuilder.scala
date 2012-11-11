package org.kevoree.library.sky.provider.web

import org.kevoree.{ContainerNode, ContainerRoot, TypeDefinition}
import org.kevoree.framework.KevoreePropertyHelper
import java.io.File
import io.Source
import xml.Node
import scala.collection.JavaConversions._
import org.kevoree.library.sky.api.helper.KloudModelHelper
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

  def getIaasPage(pattern: String, nodeName: String, model: ContainerRoot): String = {
    (<html>
      <head>
        <link rel="stylesheet" href={pattern + "bootstrap/css/bootstrap.min.css"}/>
      </head>
      <body>
        <div class="container-fluid">
          <div class="row-fluid">
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
         </div></div>
      </body>
    </html>).toString()
  }

  def getPaasPage(pattern: String, model: ContainerRoot): String = {
    (<html>
      <head>
        <link rel="stylesheet" href={pattern + "form.css"}/>
        <link rel="stylesheet" href={pattern + "bootstrap/css/bootstrap.min.css"}/>
        <link rel="stylesheet" href={pattern + "fileuploader/fileuploader.css"}/>
        <link rel="stylesheet" href={pattern + "bootstrap/css/bootstrap.min.css"}/>
        <link rel="stylesheet" href={pattern + "bootstrap/css/bootstrap-responsive.min.css"}/>
        <script type="text/javascript" src={pattern + "jquery/jquery.min.js"}></script>
        <script type="text/javascript" src={pattern + "jquery/jquery.form.js"}></script>
        <script type="text/javascript" src={pattern + "bootstrap/js/bootstrap.min.js"}></script>
        <script type="text/javascript" src={pattern + "initializeuserconfiguration/initialize_user_config.js"}></script>
      </head>
      <body>
        <div class="container-fluid">
          <div class="row-fluid">
            <ul class="breadcrumb">
              <li class="active">
                <a href=" ">Home</a>
              </li>
            </ul>
            <ul class="breadcrumb">
              <li class="active">
                <span>Initialize user configuration</span>
              </li>
            </ul>

            <form method=" " class="bs-docs-example form-horizontal" action=" " id="formNodeType">
              <div class="control-group" id="loginControlGroup">
                <label class="control-label mandatory" id="loginLabel" for="loginInput">login</label>

                <div class="controls" id="loginControls">
                  <input id="loginInput"/>
                </div>
              </div>
              <div class="control-group" id="passwordControlGroup">
                <label class="control-label" id="passwordLabel" for="passwordInput">password</label>

                <div class="controls" id="passwordControls">
                  <input type="password" id="passwordInput"/>
                </div>
              </div>
              <div class="control-group" id="sshControlGroup">
                <label class="control-label" id="sshLabel" for="sshInput">SSH public key</label>

                <div class="controls" id="sshControls">
                  <textarea id="sshInput" rows="3" cols="60"></textarea>
                </div>
              </div>

              <div class="control-group">
                <label class="control-label"></label>

                <div class="controls">
                  <input id="submit" class="btn" type="submit" value="Initialize"/>
                </div>
              </div>
            </form>

            <ul class="breadcrumb">
              <li class="active">
                <span>Already existing user configuration</span>
              </li>
            </ul>
            <table class="table table-bordered">
              <thead>
                <tr>
                  <td>User name
                  </td>
                  <td>number of nodes</td>
                  <td>action(s)</td>
                </tr>
              </thead>
              <tbody>

                {var result: List[scala.xml.Elem] = List()
              KloudModelHelper.getPaaSKloudGroups(model).foreach {
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
                        <a class="btn btn-warning" href={pattern + group.getName + "/release"}>release</a>
                      </td>
                    </tr>
                  )
                }
              }
              result}
              </tbody>
            </table>
          </div>
        </div>
      </body>
    </html>).toString()
  }

  def getPaasUserPage(login: String, pattern: String, model: ContainerRoot): String = {
    (<html>
      <head>
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
        <div class="container-fluid">
          <div class="row-fluid">
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
          </div>
        </div>
      </body>
    </html>).toString()
  }

  def getNodeLogPage(pattern: String, parentNodeName: String, nodeName: String, streamName: String, model: ContainerRoot): String = {
    (<html>
      <head>
        <link rel="stylesheet" href={pattern + "bootstrap/css/bootstrap.min.css"}/>
      </head>
      <body>
        <div class="container-fluid">
          <div class="row-fluid">
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
          </div>
        </div>
      </body>
    </html>).toString()
  }

  def getNodePage(pattern: String, parentNodeName: String, nodeName: String, model: ContainerRoot): String = {
    (<html>
      <head>
        <link rel="stylesheet" href={pattern + "bootstrap/css/bootstrap.min.css"}/>
      </head>
      <body>
        <div class="container-fluid">
          <div class="row-fluid">
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
          </div>
        </div>
      </body>

    </html>).toString()
  }

  private def nodeList(pattern: String, model: ContainerRoot, nodeList: List[ContainerNode]): Seq[Node] = {
    (<table class="table table-bordered">
      <thead>
        <tr>
          <td>#
            <a class="btn btn-success" href={pattern + "AddChild"}>add child</a>
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
                <a class="btn btn-warning" href={pattern + "RemoveChild?name=" + child.getName}>delete</a>
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

  def addNodePage(pattern: String, paasNodeList: List[TypeDefinition]): String = {
    (<html>
      <head>
        <link rel="stylesheet" href={pattern + "bootstrap/css/bootstrap.min.css"}/>
        <link rel="stylesheet" href={pattern + "form.css"}/>
        <script type="text/javascript" src={pattern + "jquery/jquery.min.js"}/>
        <script type="text/javascript" src={pattern + "jquery/jquery.form.js"}/>
        <script type="text/javascript" src={pattern + "bootstrap/js/bootstrap.min.js"}/>
        <script type="text/javascript" src={pattern + "addchild/add_child.js"}/>

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
