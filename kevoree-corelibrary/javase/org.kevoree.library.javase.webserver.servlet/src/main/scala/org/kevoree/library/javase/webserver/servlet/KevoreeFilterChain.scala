package org.kevoree.library.javase.webserver.servlet

import javax.servlet.{ServletResponse, ServletRequest, FilterChain}
import actors.{TIMEOUT, Actor}
import org.kevoree.library.javase.webserver.{KevoreeHttpResponse, KevoreeHttpRequest}
import collection.mutable
import org.slf4j.LoggerFactory
import org.kevoree.library.javase.webserver.impl.KevoreeHttpResponseImpl
import com.sun.org.apache.bcel.internal.generic.AllocationInstruction
import collection.immutable.HashMap
import javax.servlet.http.{HttpServletRequestWrapper, HttpServletResponseWrapper}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/08/12
 * Time: 09:14
 *
 * @author Erwan Daubert
 * @version 1.0
 */
case class FILTER_REQUEST (request: KevoreeServletRequest, response: KevoreeServletResponse)

case class SEND_FILTERED_REQUEST (request: KevoreeServletRequest, actorID: Int, firstSender: scala.actors.OutputChannel[scala.Any])

case class RECEIVE_FILTERED_RESPONSE (response: KevoreeHttpResponse)

case class FILTER_RESPONSE (response: KevoreeHttpResponse)

case class CLOSE ()

case class ALLOC ()

case class FREED (actorID: Int)

class KevoreeFilterChain (filterPage: AbstractFilterPage) extends Actor with FilterChain {
  val logger = LoggerFactory.getLogger(this.getClass)

  var ids = new mutable.HashMap[Int, Int]()
  var handlers = new mutable.HashMap[Int, InternalFilterChainActor]()
  var freeIDS = new mutable.Stack[Int]()

  def staticInit () {
    for (i <- 0 until 100) {
      // TODO maybe replace 3000 with a parameter ?
      handlers(i) = new InternalFilterChainActor(3000, this)
      handlers(i).start()
      freeIDS.push(i)
    }
  }

  def getFilterPage: AbstractFilterPage = filterPage


  /*@throws(classOf[IOException])
  @throws(classOf[ServletException])*/
  def doFilter (request: ServletRequest, response: ServletResponse) {
    logger.debug("doFilter")
    logger.debug(request+" - "+response)
    (request, response) match {
      case t: (KevoreeServletRequest, KevoreeServletResponse) => {
        logger.debug("execute doFilter with KevoreeServletRequest and KevoreeServletResponse")
        val resp = (this !? FILTER_REQUEST(t._1, t._2)).asInstanceOf[KevoreeHttpResponse]
        t._2.populateFromKevoreeResponse(resp)
      }
      /*case t: (KevoreeServletRequest, KevoreeServletResponse) => {
        // TODO wrap the wrapper onto KevoreeServletResponse
        logger.debug("execute doFilter with KevoreeServletRequest and HttpServletResponseWrapper")
//        val response = new KevoreeServletWrappedResponse()
        val resp = (this !? FILTER_REQUEST(t._1, t._2)).asInstanceOf[KevoreeHttpResponse]
//        response.populateFromKevoreeResponse(resp)
      }*/
     /* case t: (HttpServletRequestWrapper, HttpServletResponseWrapper) => {
        // TODO wrap the wrapper onto KevoreeServletResponse
        logger.debug("execute doFilter with KevoreeServletRequest and HttpServletResponseWrapper")
//        val response = new KevoreeServletWrappedResponse()
        val resp = (this !? FILTER_REQUEST(t._1, t._2)).asInstanceOf[KevoreeHttpResponse]
//        response.populateFromKevoreeResponse(resp)
      }*/
      case _ => logger.debug("Unable to manage this kind of request: {}", request.getClass)
    }
  }

  def killActors () {
    this ! CLOSE()
    for (i <- 0 until 100) {
      handlers(i) ! CLOSE()
    }
  }

  def receiveFilterResponse (response: KevoreeHttpResponse) {
    this ! FILTER_RESPONSE(response)
  }

  def act () {
    loop {
      react {
        case FILTER_REQUEST(request, response) => {
          logger.debug("actor processing for FILTER_REQUEST")
          val handlerID = freeIDS.pop()
          ids(request.getTokenID) = handlerID
          handlers(handlerID) ! SEND_FILTERED_REQUEST(request, handlerID, sender)
        }
        case FILTER_RESPONSE(response) => {
          logger.debug("actor processing for FILTER_RESPONSE")
          ids(response.getTokenID) match {
            case id: Int => {
              handlers(id) ! RECEIVE_FILTERED_RESPONSE(response)
            }
            case _ => logger.debug("Unable to find the request related to this reponse: {}", response.getTokenID)
          }
        }
        case FREED(actorID) =>
          freeIDS.push(actorID)
        case CLOSE() => exit()
        case e: AnyRef => logger.warn("Unknown Actor message: {}", e)
      }
    }
  }
}
