/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.tools.marShell.parser.sub

import org.kevoree.tools.marShell.ast.CreateComponentTypeStatment
import org.kevoree.tools.marShell.ast.Statment

trait KevsTypeParser extends KevsAbstractParser {

  def parseType : Parser[List[Statment]]= parseCreateComponentType | parseChannelComponentType
  
  
  //example : removeNode node1,node2
  def parseCreateComponentType : Parser[List[Statment]] = "createComponentType" ~ repsep(ident,",") ^^{ case _ ~ nodeIDs =>
      var res : List[Statment] = List()
      nodeIDs.foreach{typeName=>
        res = res ++ List(CreateComponentTypeStatment(typeName))
      }
      res
  }
  def parseChannelComponentType : Parser[List[Statment]] = "createChannelType" ~ repsep(ident,",") ^^{ case _ ~ nodeIDs =>
      var res : List[Statment] = List()
      nodeIDs.foreach{typeName=>
        res = res ++ List(CreateComponentTypeStatment(typeName))
      }
      res
  }
  def parseAddPortType : Parser[List[Statment]] 
  
  
  
}
