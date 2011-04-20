package org.kevoree.tools.marShell.ast


object AstHelper {

  def createBlockFromStatement(s : Statment) : Block = {
    TransactionalBloc(List(s))
  }


}