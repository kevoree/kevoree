package org.kevoree.library.sky.jails.process

import actors.{TIMEOUT, Actor}


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 13/03/12
 * Time: 10:44
 */

class ResultManagementActor extends Actor {

  //case class STOP()

  case class WAITINGFOR (timeout: Int)

  case class STARTING ()

  case class OUTPUT (data: String)

  case class ERROR (data: String)

  start()

  /*
  def stop() {
    this ! STOP()
  }*/

  def starting () {
    this ! STARTING()
  }

  def waitingFor (timeout: Int): (Boolean, String) = {
    (this !? WAITINGFOR(timeout)).asInstanceOf[(Boolean, String)]
  }

  def output (data: String) {
    this ! OUTPUT(data)
  }

  def error (data: String) {
    this ! ERROR(data)
  }

  var firstSender = null

  def act () {
    loop {
      react {
        //case STOP() => this.exit()
        case ERROR(data) =>
        case OUTPUT(data) =>
        case STARTING() => {
          var firstSender = this.sender
          react {
            //case STOP() => this.exit()
            case WAITINGFOR(timeout) => {
              firstSender = this.sender
              reactWithin(timeout) {
                //case STOP() => this.exit()
                case OUTPUT(data) => {
                  firstSender !(true, data)
                  exit()
                }
                case TIMEOUT => {
                  firstSender !(false, "Timeout exceeds.")
                  exit()
                }
                case ERROR(data) => {
                  firstSender !(false, data)
                  exit()
                }
              }
            }
            case OUTPUT(data) => {
              react {
                // case STOP() => this.exit()
                case WAITINGFOR(timeout) => {
                  firstSender !(true, data)
                  exit()
                }
              }
            }
            case ERROR(data) => {
              react {
                //case STOP() => this.exit()
                case WAITINGFOR(timeout) => {
                  firstSender !(false, data)
                  exit()
                }
              }
            }
          }
        }
      }
    }
  }
}
