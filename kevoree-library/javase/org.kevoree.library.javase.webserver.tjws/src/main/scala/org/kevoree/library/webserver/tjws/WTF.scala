package org.kevoree.library.webserver.tjws

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 12/04/12
 * Time: 23:34
 * To change this template use File | Settings | File Templates.
 */

object WTF  extends App{

  println("HIFDI");
  var handlers = new Array[ResponseHandler](1000)

  new Thread(){

    override def run(){
      for (i <- 0 until 1000) {
        handlers(i) = new ResponseHandler(3000, null)
        handlers(i).start()
      }
    }

  }.start()






  Thread.sleep(2000)

}
