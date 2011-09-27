package org.kevoree.library.sky.virtualCloud

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 27/09/11
 * Time: 00:01
 * To change this template use File | Settings | File Templates.
 */

object VirtualNodeHTMLHelper {

  def exportNodeListAsHTML(manager:KevoreeNodeManager): String = {
    (<html>
      <head>
          <link rel="stylesheet" href="http://twitter.github.com/bootstrap/1.3.0/bootstrap.min.css"/>
      </head>
      <body>
        <ul class="breadcrumb">
          <li class="active">
            <a href="#">Home</a> <span class="divider">/</span>
          </li>
        </ul>

        <table class="zebra-striped">
          <thead><tr>
            <td>#</td> <td>virtual node</td>
          </tr></thead>
          <tbody>
          {
            var result : List[scala.xml.Elem] = List()
            manager.getRunners.foreach {elem =>
             result = result ++ List(
            <tr>
              <td>{manager.getRunners.indexOf(elem)}</td><td><a href={"nodes/"+elem.nodeName}>{elem.nodeName}</a></td>
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