package org.kevoree.tools.ui.editor.kloud

import com.explodingpixels.macwidgets.HudWindow
import org.kevoree.tools.ui.editor.KevoreeEditor
import javax.swing._
import event.{CaretEvent, CaretListener}
import java.awt.{FlowLayout, Color, BorderLayout}
import com.explodingpixels.macwidgets.plaf.{HudButtonUI, HudTextFieldUI, HudLabelUI}
import java.awt.event.{ActionEvent, ActionListener, FocusEvent, FocusListener}
import org.kevoree.tools.ui.editor.property.SpringUtilities
import org.kevoree.{KevoreeFactory, ContainerRoot}
import org.kevoree.framework.{KevoreePropertyHelper, Constants, KevoreePlatformHelper, KevoreeXmiHelper}
import java.net.{URLConnection, URL}
import java.io.{InputStreamReader, BufferedReader, OutputStreamWriter, ByteArrayOutputStream}
import org.slf4j.LoggerFactory

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 19/02/12
 * Time: 19:48
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class KloudForm (editor: KevoreeEditor) {
  var logger = LoggerFactory.getLogger(this.getClass)
  private val defaultAddress = "10.0.0.2:8080/kloud"

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
  val loginLbl = new JLabel("Login: ", SwingConstants.TRAILING);
  loginLbl.setUI(new HudLabelUI());
  loginLbl.setOpaque(false);
  loginLbl.setLabelFor(loginTxtField);
  configLayout.add(loginLbl)
  configLayout.add(loginTxtField)

  loginTxtField.addFocusListener(new FocusListener() {
    def focusGained (p1: FocusEvent) {
      loginLbl.setForeground(Color.WHITE)
    }

    def focusLost (p1: FocusEvent) {
      if (loginTxtField.getText == "") {
        loginLbl.setForeground(Color.RED)
      }
    }
  })

  // set the password
  val passwordTxtField = new JPasswordField()
  passwordTxtField.setUI(new HudTextFieldUI())
  val passwordLbl = new JLabel("Password: ", SwingConstants.TRAILING);
  passwordLbl.setForeground(Color.WHITE)
  passwordLbl.setUI(new HudLabelUI());
  passwordLbl.setOpaque(false);
  passwordLbl.setLabelFor(passwordTxtField);
  configLayout.add(passwordLbl)
  configLayout.add(passwordTxtField)

  passwordTxtField.addFocusListener(new FocusListener() {
    def focusGained (p1: FocusEvent) {
      passwordLbl.setForeground(Color.WHITE)
    }

    def focusLost (p1: FocusEvent) {
      if (passwordTxtField.getPassword.size == 0) {
        passwordLbl.setForeground(Color.RED)
      }
    }
  })

  // set the ssh key
  val sshTxtField = new JTextField()
  sshTxtField.setUI(new HudTextFieldUI())
  val sshLbl = new JLabel("SSH Public Key: ", SwingConstants.TRAILING);
  sshLbl.setUI(new HudLabelUI());
  sshLbl.setOpaque(false);
  sshLbl.setLabelFor(sshTxtField);
  configLayout.add(sshLbl)
  configLayout.add(sshTxtField)

  sshTxtField.addFocusListener(new FocusListener() {
    def focusGained (p1: FocusEvent) {
      sshLbl.setForeground(Color.WHITE)
    }

    def focusLost (p1: FocusEvent) {
      if (sshTxtField.getText == "") {
        sshLbl.setForeground(Color.RED)
      }
    }
  })

  // set the Kloud address
  val addressTxtField = new JTextField(defaultAddress)
  addressTxtField.setUI(new HudTextFieldUI())
  val addressLbl = new JLabel("Kloud address: ", SwingConstants.TRAILING);
  addressLbl.setUI(new HudLabelUI());
  addressLbl.setOpaque(false);
  addressLbl.setLabelFor(addressTxtField);
  configLayout.add(addressLbl)
  configLayout.add(addressTxtField)

  addressTxtField.addFocusListener(new FocusListener() {
    def focusGained (p1: FocusEvent) {
      addressLbl.setForeground(Color.WHITE)
    }

    def focusLost (p1: FocusEvent) {
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
    def actionPerformed (p1: ActionEvent) {
      // check data (login, password, ssh_key must be defined, kloud address must also be set but a default value exists)
      if (loginTxtField.getText != "" && passwordTxtField.getPassword.length > 0 && sshTxtField.getText != "" && addressTxtField.getText != "") {
        val password = new String(passwordTxtField.getPassword)
        val model = editor.getPanel.getKernel.getModelHandler.getActualModel
        // send the current model of the editor on Kloud
        sendModel(loginTxtField.getText, password, sshTxtField.getText, addressTxtField.getText, model)
        ok_lbl.setText("OK")
        ok_lbl.setForeground(Color.GREEN)
      } else {
        ok_lbl.setText("KO")
        ok_lbl.setForeground(Color.RED)
        if (loginTxtField.getText != "") {
          logger.warn("You need to use a login to access the Kloud")
        } else if (passwordTxtField.getPassword.length > 0) {
          logger.warn("You may have a password to access the Kloud")
        } else if (sshTxtField.getText != "") {
          logger.warn("You need to put a SSH public key to access the Kloud")
        } else if (addressTxtField.getText != "") {
          logger.warn("You need to specify the address of the Kloud (default = {})", defaultAddress)
        }
      }
    }
  })

  val btRelease = new JButton("Release model")
  btRelease.setUI(new HudButtonUI)
  btRelease.addActionListener(new ActionListener {
    def actionPerformed (p1: ActionEvent) {
      // check data (login, password, ssh_key must be defined, kloud address must also be set but a default value exist)
      if (loginTxtField.getText != "" && passwordTxtField.getPassword.length > 0 && sshTxtField.getText != "" && addressTxtField.getText != "") {
        val password = new String(passwordTxtField.getPassword)
        // send a empty model to release all previous nodes already build
        sendModel(loginTxtField.getText, password, sshTxtField.getText, addressTxtField.getText, KevoreeFactory.createContainerRoot)
        ok_lbl.setText("OK")
        ok_lbl.setForeground(Color.GREEN)
      } else {
        ok_lbl.setText("KO")
        ok_lbl.setForeground(Color.RED)
        if (loginTxtField.getText != "") {
          logger.warn("You need to use a login to access the Kloud")
        } else if (passwordTxtField.getPassword.length > 0) {
          logger.warn("You may have a password to access the Kloud")
        } else if (sshTxtField.getText != "") {
          logger.warn("You need to put a SSH public key to access the Kloud")
        } else if (addressTxtField.getText != "") {
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

  def display () {
    newPopup.getJDialog.setVisible(true)
  }

  def hide () {
    newPopup.getJDialog.setVisible(false)
  }

  private def sendModel (login: String, password: String, sshKey: String, address: String, model: ContainerRoot): Boolean = {
    val bodyBuilder = new StringBuilder
    bodyBuilder append "login="
    bodyBuilder append login
    bodyBuilder append "&"
    bodyBuilder append "password="
    bodyBuilder append password
    bodyBuilder append "&"
    bodyBuilder append "ssh_key="
    bodyBuilder append sshKey
    bodyBuilder append "&"
    bodyBuilder append "model="
    bodyBuilder append KevoreeXmiHelper.saveToString(model, false)

    logger.debug("url=>" + address)
    val url = new URL(address)
    val connection = url.openConnection()
    connection.getOutputStream
    connection.setConnectTimeout(3000)
    connection.setDoOutput(true)
    val wr: OutputStreamWriter = new OutputStreamWriter(connection.getOutputStream)
    wr.write(bodyBuilder.toString())
    wr.flush()
    val rd: BufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream))
    var line: String = rd.readLine
    val response = new StringBuilder
    while (line != null) {
      response append line + "\n"
      line = rd.readLine
    }
    wr.close()
    rd.close()

    // look the answer to know if the model has been correctly sent
    if (response.toString().contains("One of your nodes is accessible at this address:")) {
      true
    } else {
      // TODO log the answer to give some details about the failure
      false
    }
  }


}
