package org.kevoree.library.javase.latex


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/11/11
 * Time: 10:02
 *
 * @author Erwan Daubert
 * @version 1.0
 */

trait LatexCompilerInterface {

  case class STOP ()

  case class AVAILABILITY ()
  
  case class CLEAN(folder : String)

  case class COMPILE (file: String, folder: String)

  def isAvailable: Boolean

  def clean(folder : String)
  
  def compile (file: String, folder: String): String

}