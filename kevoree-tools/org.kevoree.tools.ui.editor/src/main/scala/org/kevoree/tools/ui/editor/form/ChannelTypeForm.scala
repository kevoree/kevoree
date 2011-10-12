package org.kevoree.tools.ui.editor.form

import javax.swing.JPanel

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/10/11
 * Time: 17:37
 * To change this template use File | Settings | File Templates.
 */

trait ChannelTypeForm {
  def createNewChannelTypePanel(): JPanel = {
    val layout = new JPanel()
    layout
  }
}