package org.kevoree.library.javase.webserver.gallery

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 17/10/11
 * Time: 09:29
 * To change this template use File | Settings | File Templates.
 */

object SampleFormHelper {

  def generateFormHtml(targetURL : String) : String = {
    <html>
    <body>
    <form action={"/sample"} method="POST">
        <input type="text" name="name" />
        <input type="text" name="forname" />
        <input type="submit" value="Send" />
    </form>
    </body>
    </html>.toString()
  }

}