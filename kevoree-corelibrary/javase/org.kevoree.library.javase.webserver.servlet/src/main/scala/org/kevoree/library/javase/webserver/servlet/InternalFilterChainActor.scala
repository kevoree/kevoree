package org.kevoree.library.javase.webserver.servlet

import actors.{TIMEOUT, Actor}
import org.kevoree.library.javase.webserver.impl.KevoreeHttpResponseImpl
import org.slf4j.LoggerFactory

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/08/12
 * Time: 18:26
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class InternalFilterChainActor (timeout: Int, filterChain: KevoreeFilterChain) extends Actor {
  val logger = LoggerFactory.getLogger(this.getClass)
  def act () {
    loop {
      react {
        case SEND_FILTERED_REQUEST(request, actorID, firstSender) => {
          logger.debug("actor processing from SEND_FILTERED_REQUEST")
//          val firstSender = sender
          filterChain.getFilterPage.sendFilteredRequest(request.kevRequest)
          var done = false
          loopWhile(!done) {
              reactWithin(timeout) {
                case RECEIVE_FILTERED_RESPONSE(response) => {
                  logger.debug("actor processing from RECEIVE_FILTERED_RESPONSE")
                  firstSender ! response
                  filterChain ! FREED(actorID)
                  done = true
                }
                case TIMEOUT => {
                  val result = new KevoreeHttpResponseImpl
                  result.setTokenID(request.getTokenID)
                  result.setStatus(504)
                  result.setContent("Kevoree Servlet Timeout")
                  firstSender ! result
                  filterChain ! FREED(actorID)
                  done = true
                }
                case CLOSE() => exit()
                case e : AnyRef => logger.warn("Unknown Actor message: {}", e)
              }
            }
          }
        case CLOSE() => exit()
        case e : AnyRef => logger.warn("Unknown Actor message: {}", e)
      }
    }

  }
}

