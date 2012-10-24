package org.kevoree.library.sky.api.http

import io.Source
import org.kevoree.library.sky.api.KevoreeNodeManager
import org.kevoree.TypeDefinition
import org.kevoree.library.sky.api.nodeType.AbstractHostNode
import org.kevoree.framework.KevoreePropertyHelper
import scala.collection.JavaConversions._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 27/09/11
 * Time: 00:01
 */

object VirtualNodeHTMLHelper {

  def getNodeStreamAsHTML (nodeName: String, streamName: String, manager: KevoreeNodeManager): String = {
    (<html>
      <head>
        <link rel="stylesheet" href="/bootstrap.min.css"/>
      </head>
      <body>
        <ul class="breadcrumb">
          <li>
            <a href="/">Home</a> <span class="divider">/</span>
          </li>
          <li>
            <a href={"/nodes/" + nodeName}>
              {nodeName}
            </a> <span class="divider">/</span>
          </li>
          <li class="active">
            <a href={"/nodes/" + nodeName + "/" + streamName}>
              {streamName}
            </a>
          </li>
        </ul>{var result: List[scala.xml.Elem] = List()
      manager.runners.find(r => r.nodeName == nodeName) match {
        case Some(runner) => {
          result = result ++ List(
                                   <div class="alert-message block-message info">
                                     {streamName match {
                                     case "out" => {
                                       var subresult: List[scala.xml.Elem] = List()
                                       Source.fromFile(runner.getOutFile).getLines().toList /*.reverse*/ .foreach {
                                         line =>
                                           subresult = subresult ++ List(<p>
                                             {line}
                                           </p>)
                                       }
                                       subresult
                                     }
                                     case "err" => {
                                       var subresult: List[scala.xml.Elem] = List()
                                       Source.fromFile(runner.getErrFile).getLines().toList /*.reverse*/ .foreach {
                                         line =>
                                           subresult = subresult ++ List(<p>
                                             {line}
                                           </p>)
                                       }
                                       subresult
                                     }
                                     case _ => "unknow stream"
                                   }}
                                   </div>
                                 )
        }
        case None => {
          result = result ++ List(
                                   <div class="alert-message block-message error">
                                     <p>Node instance not hosted on this platform</p>
                                   </div>
                                 )
        }
      }
      result}
      </body>
    </html>).toString()
  }

  def getNodeHomeAsHTML (nodeName: String, manager: KevoreeNodeManager): String = {
    (<html>
      <head>
        <link rel="stylesheet" href="/bootstrap.min.css"/>
      </head>
      <body>
        <ul class="breadcrumb">
          <li>
            <a href="/">Home</a> <span class="divider">/</span>
          </li>
          <li class="active">
            <a href={"/nodes/" + nodeName}>
              {nodeName}<span class="divider">/</span>
            </a>
          </li>
        </ul>{var result: List[scala.xml.Elem] = List()
      manager.runners.find(r => r.nodeName == nodeName) match {
        case Some(runner) => {
          result = result ++ List(
                                   <div class="alert-message block-message info">
                                     <p>
                                       <a href={"/nodes/" + nodeName + "/out"}>Output log</a>
                                     </p>
                                     <p>
                                       <a href={"/nodes/" + nodeName + "/err"}>Error log</a>
                                     </p>
                                   </div>
                                 )
        }
        case None => {
          result = result ++ List(
                                   <div class="alert-message block-message error">
                                     <p>Node instance not hosted on this platform</p>
                                   </div>
                                 )
        }
      }
      result}
      </body>
    </html>).toString()
  }

  def exportNodeListAsHTML (node: AbstractHostNode): String = {
    (<html>
      <head>
        <link rel="stylesheet" href="/bootstrap.min.css"/>
      </head>
      <body>
        <img height="200px" src="/scaled500.png" alt="Kevoree"/>
        <ul class="breadcrumb">
          <li class="active">
            <a href="/">Home</a> <span class="divider">/</span>
          </li>
        </ul>

        <table class="zebra-striped">
          <thead>
            <tr>
              <td>#
                <button type="button" class="btn btn-primary">
                  <a href="/AddChild">add child</a>
                </button>
              </td> <td>virtual node</td> <td>ip</td>
            </tr>
          </thead>
          <tbody>
            {var result: List[scala.xml.Elem] = List()
          node.getNodeManager.runners.foreach {
            elem => {
              val ips = KevoreePropertyHelper.getStringNetworkProperties(node.getModelService.getLastModel, elem.nodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
              val ipString = ips.mkString(", ")

              result = result ++ List(
                                       <tr>
                                         <td>
                                           {node.getNodeManager.runners.indexOf(elem)}
                                         </td> <td>
                                         <a href={"nodes/" + elem.nodeName}>
                                           <span class="label notice">
                                             {elem.nodeName}
                                           </span>
                                           <span class="divider">/</span>
                                           <button type="button" class="btn btn-primary">
                                             <a href={"/RemoveChild?name=" + elem.nodeName}>delete</a>
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
          result}
          </tbody>
        </table>

      </body>
    </html>).toString()
  }

  def exportPaaSNodeList (paasNodeList: List[TypeDefinition]): String = {
    (<html>
      <head>
        <link rel="stylesheet" href="/bootstrap.min.css"/>
        <link rel="stylesheet" href="/add_child.css"/>
        <script type="text/javascript" src="/jquery.min.js"/>
        <script type="text/javascript" src="/jquery.form.js"/>
        <script type="text/javascript" src="/bootstrap.min.js"/>
        <script type="text/javascript" src="/add_child.js"/>

      </head>
      <body>
        <ul class="breadcrumb">
          <li>
            <a href="/">Home</a> <span class="divider">/</span>
          </li>
          <li class="active">
            <a href={"/AddChild"}>AddChild
              <span class="divider">/</span>
            </a>
          </li>
        </ul>
        <!--<img height="200px" src="/scaled500.png" alt="Kevoree"/>-->
        <ul class="breadcrumb">
          <li class="active">
            <a href="/">Add PaaS Node</a> <span class="divider">/</span>
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