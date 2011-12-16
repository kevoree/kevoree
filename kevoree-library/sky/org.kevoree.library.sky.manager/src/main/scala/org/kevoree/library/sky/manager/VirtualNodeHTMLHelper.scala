package org.kevoree.library.sky.manager

import io.Source

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 27/09/11
 * Time: 00:01
 * To change this template use File | Settings | File Templates.
 */

object VirtualNodeHTMLHelper {

  def getNodeStreamAsHTML(nodeName : String,streamName : String):String={
      (  <html>
      <head>
          <link rel="stylesheet" href="http://twitter.github.com/bootstrap/1.3.0/bootstrap.min.css"/>
      </head>
      <body>
        <ul class="breadcrumb">
          <li><a href="/">Home</a> <span class="divider">/</span></li>
          <li><a href={"/nodes/"+nodeName}>{nodeName}</a> <span class="divider">/</span></li>
          <li class="active"><a href={"/nodes/"+nodeName+"/"+streamName}>{streamName}</a></li>
        </ul>
        {
          var result : List[scala.xml.Elem] = List()
          KevoreeNodeManager.runners.find(r => r.nodeName == nodeName)match {
            case Some(runner)=> {
               result = result ++ List(
                 <div class="alert-message block-message info">
                   {
                     streamName match {
                       case "out" => {
                         var subresult : List[scala.xml.Elem] = List()
                         Source.fromFile(runner.asInstanceOf[KevoreeNodeRunner].getOutFile).getLines().toList.reverse.foreach{ line =>
                            subresult = subresult ++ List(<p>{line}</p>)
                         }
                         subresult
                       }
                       case "err" => {
                         var subresult : List[scala.xml.Elem] = List()
                         Source.fromFile(runner.asInstanceOf[KevoreeNodeRunner].getErrFile).getLines().toList.reverse.foreach{ line =>
                            subresult = subresult ++ List(<p>{line}</p>)
                         }
                         subresult
                       }
                       case _ => "unknow stream"
                     }
                   }
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
          result
        }
      </body>
    </html>).toString
  }



  def getNodeHomeAsHTML(nodeName : String):String={
      (  <html>
      <head>
          <link rel="stylesheet" href="http://twitter.github.com/bootstrap/1.3.0/bootstrap.min.css"/>
      </head>
      <body>
        <ul class="breadcrumb">
          <li><a href="/">Home</a> <span class="divider">/</span></li>
          <li class="active"><a href={"/nodes/"+nodeName}>{nodeName}</a></li>
        </ul>
        {
          var result : List[scala.xml.Elem] = List()
          KevoreeNodeManager.runners.find(r => r.nodeName == nodeName)match {
            case Some(runner)=> {
               result = result ++ List(
                 <div class="alert-message block-message info">
                   <p><a href={"/nodes/"+nodeName+"/out"}>Output log</a></p>
                   <p><a href={"/nodes/"+nodeName+"/err"}>Error log</a></p>
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
          result
        }
      </body>
    </html>).toString
  }


  def exportNodeListAsHTML(): String = {
    (<html>
      <head>
          <link rel="stylesheet" href="http://twitter.github.com/bootstrap/1.3.0/bootstrap.min.css"/>
      </head>
      <body>
        <img height="200px" src="http://s3.amazonaws.com/files.posterous.com/headers/3005281/scaled500.png" />
        <ul class="breadcrumb">
          <li class="active">
            <a href="/">Home</a> <span class="divider">/</span>
          </li>
        </ul>

        <table class="zebra-striped">
          <thead><tr>
            <td>#</td> <td>virtual node</td>
          </tr></thead>
          <tbody>
          {
            var result : List[scala.xml.Elem] = List()
            KevoreeNodeManager.runners.foreach {elem =>
             result = result ++ List(
            <tr>
              <td>{KevoreeNodeManager.runners.indexOf(elem)}</td><td><a href={"nodes/"+elem.nodeName}><span class="label notice">{elem.nodeName}</span></a></td>
            </tr>
             )
          }
            result
          }
          </tbody>
        </table>

      </body>
    </html>).toString()
  }


}