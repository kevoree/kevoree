/*
package org.kevoree.library.sky.provider.web

import io.Source
import org.kevoree.{ContainerRoot, TypeDefinition}
import org.kevoree.framework.KevoreePropertyHelper
import scala.collection.JavaConversions._
import xml.Node
import java.io.File

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 27/09/11
 * Time: 00:01
 */

object VirtualNodeHTMLHelper {

  def getNodeStreamAsHTML (pattern: String, parentNodeName: String, nodeName: String, streamName: String, model: ContainerRoot): String = {
    (<html>
      <head>
        <link rel="stylesheet" href={pattern + "bootstrap.min.css"}/>
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

  def getNodeHomeAsHTML (pattern: String, parentNodeName: String, nodeName: String, model: ContainerRoot): String = {
    (<html>
      <head>
        <link rel="stylesheet" href={pattern + "bootstrap.min.css"}/>
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

  def exportNodeListAsHTML (pattern: String, nodeName: String, model: ContainerRoot): String = {
    (<html>
      <head>
        <link rel="stylesheet" href={pattern + "bootstrap.min.css"}/>
      </head>
      <body>
        <img height="200px" src={pattern + "scaled500.png"} alt="Kevoree"/>
        <ul class="breadcrumb">
          <li class="active">
            <a href={pattern}>Home</a> <span class="divider">/</span>
          </li>
        </ul>{nodeList(pattern, nodeName, model)}

      </body>
    </html>).toString()
  }

  def nodeList (pattern: String, nodeName: String, model: ContainerRoot): Seq[Node] = {
    (<table class="zebra-striped">
      <thead>
        <tr>
          <td>#
            <button type="button" class="btn btn-primary">
              <a href={pattern + "AddChild"}>add child</a>
            </button>
          </td> <td>virtual node</td> <td>ip</td>
        </tr>
      </thead>
      <tbody>
        {var result: List[scala.xml.Elem] = List()
      model.getNodes.find(n => n.getName == nodeName) match {
        case None =>
        case Some(node) => {
          node.getHosts.foreach {
            child => {
              val ips = KevoreePropertyHelper.getStringNetworkProperties(model, child.getName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
              val ipString = ips.mkString(", ")
              result = result ++ List(
                                       <tr>
                                         <td>
                                           {node.getHosts.indexOf(child)}
                                         </td> <td>
                                         <a href={pattern + "nodes/" + child.getName}>
                                           <span class="label notice">
                                             {child.getName}
                                           </span>
                                           <span class="divider">/</span>
                                           <button type="button" class="btn btn-primary">
                                             <a href={pattern + "RemoveChild?name=" + child.getName}>delete</a>
                                           </button>
                                         </a>
                                       </td>
                                         <td>
                                           {ipString}
                                         </td>
                                       </tr>
                                     )
            }
          }
        }
      }
      result}
      </tbody>
    </table>)
  }

  def exportPaaSNodeList (pattern: String, paasNodeList: List[TypeDefinition]): String = {
    (<html>
      <head>
        <link rel="stylesheet" href={pattern + "bootstrap.min.css"}/>
        <link rel="stylesheet" href={pattern + "add_child.css"}/>
        <script type="text/javascript" src={pattern + "jquery.min.js"}/>
        <script type="text/javascript" src={pattern + "jquery.form.js"}/>
        <script type="text/javascript" src={pattern + "bootstrap.min.js"}/>
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
}*/
