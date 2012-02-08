package org.kevoree.library.sky.provider

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/01/12
 * Time: 15:16
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object HTMLHelper {

  def generateSimpleSubmissionFormHtml (targetURL: String, url : String): String = {

    val cssPath = url + "/css/bootstrap.min.css"

    <html>
      <head>
          <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
          <meta charset="utf-8"/>
          <link href={cssPath} rel="stylesheet"/>
      </head>
      <body>
        <form method="post" action={targetURL} enctype="multipart/form-data">
          <table>
            <tr>
              <td>Login:</td>
              <td>
                  <input type="text" name="login" maxlength="20" size="20"/>
              </td>
              <td>Password:</td>
              <td>
                  <input type="password" name="password" maxlength="20" size="20"/>
              </td>
            </tr>
            <tr>
              <td>Model:</td>
              <td>
                  <input type="file" name="model"/>
              </td>
            </tr>
            <tr>
              <td>Public SSH key:</td>
              <td>
                <textarea name="ssh_key" cols="80" rows="3"></textarea>
              </td>
            </tr>
            <tr>
              <td colspan="2" align="center">
                  <input type="submit" value="Submit"/>
              </td>
            </tr>
          </table>
        </form>
      </body>
    </html>
      .toString()
  }

  def generateValidSubmissionPageHtml (targetURL: String, login: String, address: String, url : String): String = {

    val cssPath = url + "/css/bootstrap.min.css"
    <html>
      <head>
          <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
          <meta charset="utf-8"/>
          <link href={cssPath} rel="stylesheet"/>
      </head>
      <body>
        <p>
          Thanks
          {login}
          .
            <br/>
          Submission accepted and deployed.
            <br/>
          One of your nodes is accessible at this address:
          {address}
        </p>
      </body>
    </html>.toString()
  }

  def generateUnvalidSubmissionPageHtml (targetURL: String, login: String, exception: String, url : String): String = {

    val cssPath = url + "/css/bootstrap.min.css"
    <html>
      <head>
          <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
          <meta charset="utf-8"/>
          <link href={cssPath} rel="stylesheet"/>
      </head>
      <body>
        <p>
          Sorry
          {login}
          .
            <br/>
          Submission cannot be accepted:
            <br/>{exception}
        </p>
      </body>
    </html>.toString()
  }

  def generateFailToLoginPageHtml (login: String, url : String): String = {
    val cssPath = url + "/css/bootstrap.min.css"
    <html>
      <head>
          <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
          <meta charset="utf-8"/>
          <link href={cssPath} rel="stylesheet"/>
      </head>
      <body>
        <p>
          Sorry
          {login}
          .
            <br/>
          You are an unknown user of this Kloud.
        </p>
      </body>
    </html>.toString()
  }
}