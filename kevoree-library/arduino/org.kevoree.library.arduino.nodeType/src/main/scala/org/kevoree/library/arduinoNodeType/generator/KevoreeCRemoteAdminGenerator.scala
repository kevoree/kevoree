package org.kevoree.library.arduinoNodeType.generator

import templates.SimpleCopyTemplate

trait KevoreeCRemoteAdminGenerator extends KevoreeCAbstractGenerator {

  def generateCheckForAdminMsg() {
    context b SimpleCopyTemplate.copyFromClassPath("templates/KevScriptMsgChecker.c")
  }

  def generateConcatKevscriptParser(): Unit = {
    context b SimpleCopyTemplate.copyFromClassPath("templates/KevScriptParser.c")
  }
}