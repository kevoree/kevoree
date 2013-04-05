/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.tools.ui.editor.kloud

import com.explodingpixels.macwidgets.HudWindow
import javax.swing._
import java.awt.{FlowLayout, Color, BorderLayout}
import java.awt.event._
import org.kevoree.tools.ui.editor.property.SpringUtilities
import org.kevoree.ContainerRoot
import org.kevoree.framework.KevoreeXmiHelper
import org.slf4j.LoggerFactory
import com.explodingpixels.macwidgets.plaf.{HudPasswordFieldUI, HudButtonUI, HudTextFieldUI, HudLabelUI}
import java.io.{ByteArrayOutputStream, OutputStreamWriter}
import org.kevoree.tools.ui.editor.{PositionedEMFHelper, KevoreeEditor}
import org.kevoree.tools.ui.editor.command.LoadModelCommand
import java.net.{URLDecoder, URLEncoder, HttpURLConnection, URL}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 19/02/12
 * Time: 19:48
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class KloudForm(editor: KevoreeEditor, button: AbstractButton) {
  var logger = LoggerFactory.getLogger(this.getClass)
  private val defaultAddress = "http://10.0.0.2:8080/kloud"

  val newPopup = new HudWindow("Submit a model on Kloud")
  newPopup.getJDialog.setSize(400, 200)
  newPopup.getJDialog.setLocationRelativeTo(editor.getPanel)
  val layoutPopup = new JPanel()
  layoutPopup.setOpaque(false)
  layoutPopup.setLayout(new BorderLayout())

  val layoutPopupTop = new JPanel()
  layoutPopupTop.setOpaque(false)

  val configLayout = new JPanel(new SpringLayout)
  configLayout.setSize(400, 200)
  configLayout.setOpaque(false)

  // build the content of the Dialog

  // set the login
  val loginTxtField = new JTextField()
  loginTxtField.setUI(new HudTextFieldUI())
  val loginLbl = new JLabel("Login: ", SwingConstants.TRAILING)
  loginLbl.setUI(new HudLabelUI())
  loginLbl.setOpaque(false)
  loginLbl.setLabelFor(loginTxtField)
  configLayout.add(loginLbl)
  configLayout.add(loginTxtField)

  loginTxtField.addFocusListener(new FocusListener() {
    def focusGained(p1: FocusEvent) {
      loginLbl.setForeground(Color.WHITE)
    }

    def focusLost(p1: FocusEvent) {
      if (loginTxtField.getText == "") {
        loginLbl.setForeground(Color.RED)
      }
    }
  })

  // set the password
  val passwordTxtField = new JPasswordField()
  passwordTxtField.setUI(new HudPasswordFieldUI())
  val passwordLbl = new JLabel("Password: ", SwingConstants.TRAILING)
  passwordLbl.setForeground(Color.WHITE)
  passwordLbl.setUI(new HudLabelUI())
  passwordLbl.setOpaque(false)
  passwordLbl.setLabelFor(passwordTxtField)
  configLayout.add(passwordLbl)
  configLayout.add(passwordTxtField)

  /*passwordTxtField.addFocusListener(new FocusListener() {
    def focusGained (p1: FocusEvent) {
      passwordLbl.setForeground(Color.WHITE)
    }

    def focusLost (p1: FocusEvent) {
      if (passwordTxtField.getPassword.size == 0) {
        passwordLbl.setForeground(Color.RED)
      }
    }
  })*/

  // set the ssh key
  val sshTxtField = new JTextField()
  sshTxtField.setUI(new HudTextFieldUI())
  val sshLbl = new JLabel("SSH Public Key: ", SwingConstants.TRAILING)
  sshLbl.setUI(new HudLabelUI())
  sshLbl.setOpaque(false)
  sshLbl.setLabelFor(sshTxtField)
  configLayout.add(sshLbl)
  configLayout.add(sshTxtField)

  // set the Kloud address
  val addressTxtField = new JTextField(defaultAddress)
  addressTxtField.setUI(new HudTextFieldUI())
  val addressLbl = new JLabel("Kloud address: ", SwingConstants.TRAILING)
  addressLbl.setUI(new HudLabelUI())
  addressLbl.setOpaque(false)
  addressLbl.setLabelFor(addressTxtField)
  configLayout.add(addressLbl)
  configLayout.add(addressTxtField)

  addressTxtField.addFocusListener(new FocusListener() {
    def focusGained(p1: FocusEvent) {
      addressLbl.setForeground(Color.WHITE)
    }

    def focusLost(p1: FocusEvent) {
      if (addressTxtField.getText == "") {
        addressLbl.setForeground(Color.RED)
      }
    }
  })

  SpringUtilities.makeCompactGrid(configLayout, 4, 2, 6, 6, 6, 6)


  // define the buttons available to submit something on Kloud
  val btSubmit = new JButton("Submit model")
  btSubmit.setUI(new HudButtonUI)
  btSubmit.addActionListener(new ActionListener {
    def actionPerformed(p1: ActionEvent) {
      ok_lbl.setForeground(Color.WHITE)
      ok_lbl.setText("")
      // check data (login, password, ssh_key must be defined, kloud address must also be set but a default value exists)
      if (loginTxtField.getText != "" /*&& passwordTxtField.getPassword.length > 0*/ && addressTxtField.getText != "") {
        val password = new String(passwordTxtField.getPassword)
        val model = editor.getPanel.getKernel.getModelHandler.getActualModel
        // send the current model of the editor on Kloud
        new Thread() {
          override def run() {
            if (sendModel(password, sshTxtField.getText, addressTxtField.getText + "/" + loginTxtField.getText + "/model", model)) {
              ok_lbl.setText("OK")
              ok_lbl.setForeground(Color.GREEN)
            } else {
              ok_lbl.setText("KO")
              ok_lbl.setForeground(Color.RED)
            }
          }
        }.start()
      } else {
        ok_lbl.setText("KO")
        ok_lbl.setForeground(Color.RED)
        if (loginTxtField.getText == "") {
          logger.warn("You need to use a login to access the Kloud")
        }
        /*if (passwordTxtField.getPassword.length == 0) {
          logger.warn("You may have a password to access the Kloud")
        }*/
        if (addressTxtField.getText == "") {
          logger.warn("You need to specify the address of the Kloud (default = {})", defaultAddress)
        }
      }
    }
  })

  val btRelease = new JButton("Release model")
  btRelease.setUI(new HudButtonUI)
  btRelease.addActionListener(new ActionListener {
    def actionPerformed(p1: ActionEvent) {
      ok_lbl.setForeground(Color.WHITE)
      ok_lbl.setText("")
      // check data (login, password, ssh_key must be defined, kloud address must also be set but a default value exist)
      if (loginTxtField.getText != "" /*&& passwordTxtField.getPassword.length > 0*/ && addressTxtField.getText != "") {
        val password = new String(passwordTxtField.getPassword)
        new Thread() {
          override def run() {
            // send a empty model to release all previous nodes already build
            if (release(password, addressTxtField.getText + "/" + loginTxtField.getText + "/release")) {
              ok_lbl.setText("OK")
              ok_lbl.setForeground(Color.GREEN)
            } else {
              ok_lbl.setText("KO")
              ok_lbl.setForeground(Color.RED)
            }
          }
        }.start()
      } else {
        ok_lbl.setText("KO")
        ok_lbl.setForeground(Color.RED)
        if (loginTxtField.getText == "") {
          logger.warn("You need to use a login to access the Kloud")
        } /* else if (passwordTxtField.getPassword.length == 0) {
          logger.warn("You may have a password to access the Kloud")
        }*/
        else if (addressTxtField.getText == "") {
          logger.warn("You need to specify the address of the Kloud (default = {})", defaultAddress)
        }
      }
    }
  })

  val ok_lbl = new JLabel("  ")
  ok_lbl.setUI(new HudLabelUI())
  ok_lbl.setOpaque(false)

  val submissionLayout = new JPanel(new FlowLayout(FlowLayout.CENTER))
  submissionLayout.add(btSubmit)
  submissionLayout.add(btRelease)
  submissionLayout.add(ok_lbl)
  submissionLayout.setOpaque(false)


  layoutPopup.add(layoutPopupTop, BorderLayout.NORTH)
  layoutPopup.add(configLayout, BorderLayout.CENTER)
  layoutPopup.add(submissionLayout, BorderLayout.SOUTH)
  newPopup.getContentPane.add(layoutPopup)
  newPopup.getJDialog.addComponentListener(new ComponentAdapter() {

    override def componentHidden(e: ComponentEvent) {
      hide()
    }
  })

  def display() {
    newPopup.getJDialog.setVisible(true)
    button.setEnabled(true)
  }

  def hide() {
    newPopup.getJDialog.setVisible(false)
    button.setEnabled(false)
  }

  private def sendModel(password: String, sshKey: String, address: String, model: ContainerRoot): Boolean = {
    val bodyBuilder = new StringBuilder
    /*
        bodyBuilder append "login="
        bodyBuilder append URLEncoder.encode(login, "UTF-8")*/
    //    bodyBuilder append "&password="
    bodyBuilder append "password="
    bodyBuilder append URLEncoder.encode(password, "UTF-8")
    bodyBuilder append "&ssh_key="
    bodyBuilder append URLEncoder.encode(sshKey, "UTF-8")
    if (model.getGroups.size > 0 && model.getNodes.size > 0) {
      bodyBuilder append "&model="
      bodyBuilder append URLEncoder.encode(KevoreeXmiHelper.instance$.saveToString(model, false), "UTF-8")
    }

    logger.debug("url=>" + address)
    try {
      val url = new URL(address)
      val connection = url.openConnection().asInstanceOf[HttpURLConnection]
      connection.setRequestMethod("POST")
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
      connection.setRequestProperty("Content-Length", Integer.toString(bodyBuilder.length))
      connection.setConnectTimeout(3000)
      connection.setDoOutput(true)
      val wr: OutputStreamWriter = new OutputStreamWriter(connection.getOutputStream)
      wr.write(bodyBuilder.toString())
      wr.flush()
      val rd = connection.getInputStream
      val bytes = new Array[Byte](2048)
      var length = 0
      val byteArrayOutputStream = new ByteArrayOutputStream()
      length = rd.read(bytes)
      while (length != -1) {
        byteArrayOutputStream.write(bytes, 0, length)
        length = rd.read(bytes)
      }
      wr.close()
      rd.close()

      var response = new String(byteArrayOutputStream.toByteArray, "UTF-8")

      var nbTry = 20
      // look the answer to know if the model has been correctly sent
      while (response.startsWith("<wait") && nbTry > 0) {
        logger.debug(response)
        nbTry = nbTry - 1
        Thread.sleep(3000)
        try {
          bodyBuilder.clear()
          bodyBuilder append "password="
          bodyBuilder append URLEncoder.encode(password, "UTF-8")
          //          val url = new URL(address + "/" + login)
          val connection = url.openConnection().asInstanceOf[HttpURLConnection]
          connection.setRequestMethod("POST")
          connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
          connection.setRequestProperty("Content-Length", "" + Integer.toString(bodyBuilder.length))
          connection.setConnectTimeout(3000)
          connection.setDoOutput(true)
          val wr: OutputStreamWriter = new OutputStreamWriter(connection.getOutputStream)
          wr.write(bodyBuilder.toString())
          wr.flush()
          val rd = connection.getInputStream
          val bytes = new Array[Byte](2048)
          var length = 0
          val byteArrayOutputStream = new ByteArrayOutputStream()
          length = rd.read(bytes)
          while (length != -1) {
            byteArrayOutputStream.write(bytes, 0, length)
            length = rd.read(bytes)
          }
          wr.close()
          rd.close()
          response = new String(byteArrayOutputStream.toByteArray, "UTF-8")
        } catch {
          case _@e =>
        }
      }
      if (response.startsWith("<wait")) {
        logger.debug("Timeout, unable to get your configuration model on the Kloud")
        false
      } else if (response.startsWith("<nack")) {
        try {
          val errorMessage = URLDecoder.decode(response, "UTF-8").split("error=\"")(1)
          logger.warn("Unable to release your configuration on the Kloud: {}", errorMessage.substring(0, errorMessage.indexOf("\"")))
        } catch {
          case _@e => logger.debug("Unable to catch error", e)
        }
        false
      } else {
        val lcommand = new LoadModelCommand()
        try {
          editor.getPanel.getKernel.getModelHandler.merge(KevoreeXmiHelper.instance$.loadString(response))
          PositionedEMFHelper.updateModelUIMetaData(editor.getPanel.getKernel)
          lcommand.setKernel(editor.getPanel.getKernel)
          lcommand.execute(editor.getPanel.getKernel.getModelHandler.getActualModel)
          true
        } catch {
          case _@e => logger.debug("Unable to load model:\n{}", response); false
        }
      }
    } catch {
      case _@e => logger.error("Unable to deploy on Kloud", e)
      false
    }
  }

  private def release(password: String, address: String): Boolean = {
    val bodyBuilder = new StringBuilder

    bodyBuilder append "&password="
    bodyBuilder append URLEncoder.encode(password, "UTF-8")

    logger.debug("url=>" + address)
    try {
      val url = new URL(address)
      val connection = url.openConnection().asInstanceOf[HttpURLConnection]
      connection.setRequestMethod("POST")
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
      connection.setRequestProperty("Content-Length", "" + Integer.toString(bodyBuilder.length))
      connection.setConnectTimeout(3000)
      connection.setDoOutput(true)
      val wr: OutputStreamWriter = new OutputStreamWriter(connection.getOutputStream)
      wr.write(bodyBuilder.toString())
      wr.flush()
      val rd = connection.getInputStream
      val byteArrayOutputStream = new ByteArrayOutputStream()
      val bytes = new Array[Byte](2048)
      var length = 0
      //      var line: String = rd.readLine
      length = rd.read(bytes)
      while (length != -1) {
        byteArrayOutputStream.write(bytes, 0, length)
        //        response append bytes + "\n"
        length = rd.read(bytes)
      }

      wr.close()
      rd.close()

      var response = new String(byteArrayOutputStream.toByteArray, "UTF-8")

      var nbTry = 20
      while (response.startsWith("<wait") && nbTry > 0) {
        logger.debug(response)
        nbTry = nbTry - 1
        Thread.sleep(3000)
        try {
          val url = new URL(address)
          val connection = url.openConnection().asInstanceOf[HttpURLConnection]
          connection.setRequestMethod("POST")
          connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
          connection.setRequestProperty("Content-Length", "" + Integer.toString(bodyBuilder.length))
          connection.setRequestProperty("HOST", "kevoree.org")
          connection.setConnectTimeout(3000)
          connection.setDoOutput(true)
          val wr: OutputStreamWriter = new OutputStreamWriter(connection.getOutputStream)
          wr.write(bodyBuilder.toString())
          wr.flush()
          val rd = connection.getInputStream
          val byteArrayOutputStream = new ByteArrayOutputStream()
          val bytes = new Array[Byte](2048)
          var length = 0
          //      var line: String = rd.readLine
          length = rd.read(bytes)
          while (length != -1) {
            byteArrayOutputStream.write(bytes, 0, length)
            //        response append bytes + "\n"
            length = rd.read(bytes)
          }

          wr.close()
          rd.close()

          response = new String(byteArrayOutputStream.toByteArray, "UTF-8")
        } catch {
          case _@e =>
        }
      }
      // look the answer to know if the model has been correctly sent
      if (response.startsWith("<wait")) {
        logger.debug("Timeout, unable to get your configuration model on the Kloud")
        false
      } else if (response.startsWith("<nack")) {
        try {
          val errorMessage = URLDecoder.decode(response, "UTF-8").split("error=\"")(1)
          logger.warn("Unable to release your configuration on the Kloud: {}", errorMessage.substring(0, errorMessage.indexOf("\"")))
        } catch {
          case _@e => logger.debug("Unable to catch error", e)
        }
        false
      } else if (response.startsWith("<ack")) {
        // TODO clean the model by removing all IP data and the user group
        true
      } else {
        logger.warn("Unknown response: {}", response)
        false
      }
    } catch {
      case _@e => logger.error("Unable to release model on Kloud", e)
      false
    }


  }


}
