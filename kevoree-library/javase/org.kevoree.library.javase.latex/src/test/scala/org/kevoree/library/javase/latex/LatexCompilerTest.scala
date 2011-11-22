package org.kevoree.library.javase.latex

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/11/11
 * Time: 14:18
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object LatexCompilerTest extends App {

  val compiler = new LinuxLatexCompiler

  if (compiler.isAvailable) {
    println(compiler.compile("CCGrid2012.tex", "/home/edaubert/Documents/these/articles/CCGrid2012"))

    Thread.sleep(15000)

    compiler.clean("/home/edaubert/Documents/these/articles/CCGrid2012")
  }
}