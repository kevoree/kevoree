package controlbasicGossiper

import java.io.{InputStream}


/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 28/01/13
 * Time: 11:30
 * To change this template use File | Settings | File Templates.
 */
object HelperModelSigned {

  def loadSignedModelStream(input: InputStream): Array[Byte] = {
    val inputData: Array[Byte] = Stream.continually(input.read).takeWhile(-1 !=).map(_.toByte).toArray
    inputData
  }

}
