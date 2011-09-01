package org.kevoree.tools.ui.editor.standalone

import java.awt.{Dimension, Color, BorderLayout}
import java.io.{OutputStream, PrintStream}
import javax.swing.{SwingUtilities, JScrollPane, JPanel}
import actors.DaemonActor
import java.lang.StringBuffer

/**
 * User: ffouquet
 * Date: 01/09/11
 * Time: 11:03
 */

class LogPanel extends JPanel {
  var STDwriter: PrintStream = null
  var ERRwriter: PrintStream = null
  //public static PrintStream SSTDwriter = null;
  // public static PrintStream SSTDwriter = null;
  private var eol: String = System.getProperty("line.separator")
  private var scrollShell: JScrollPane = null

  setLayout(new BorderLayout)
  val textArea: RichTextArea = new RichTextArea
  textArea.setBackground(new Color(57, 57, 57))
  textArea.setEditable(false)
  textArea.setPreferredSize(new Dimension(500, 250))
  STDwriter = new PrintStream(new TextOutputStream(textArea, Color.WHITE))
  ERRwriter = new PrintStream(new TextOutputStream(textArea, Color.ORANGE))
  scrollShell = new JScrollPane(textArea)
  add(scrollShell, BorderLayout.CENTER)
  System.setOut(STDwriter)
  System.setErr(ERRwriter)





  private class TextOutputStream extends OutputStream {
    var currentLine = new StringBuilder()
    def this(textArea: RichTextArea, color: Color) {
      this ()
      _textArea = textArea
      _color = color
    }

    def write(i: Int): Unit = {
      SwingUtilities.invokeLater(new Runnable {
        def run: Unit = {
          try {
            currentLine.append(i.asInstanceOf[Char])
            if(i.asInstanceOf[Char] == '\n'){
              _textArea.append(currentLine.toString(), _color, false)
              currentLine.clear()
            }
          }
          catch {
            case e: Exception => {
              e.printStackTrace
            }
          }
        }
      })
    }

    private var _textArea: RichTextArea = null
    private var _color: Color = null
  }

}