package org.kevoree.library.javase.kestrelChannels

import java.io.{File, FileInputStream}
import java.util.concurrent.CountDownLatch
import scala.collection.mutable
import com.twitter.conversions.storage._
import com.twitter.conversions.time._
import net.lag.kestrel.Kestrel
import net.lag.kestrel.PersistentQueue
import net.lag.kestrel.QItem
import net.lag.kestrel.config.QueueConfig
import net.lag.kestrel.config.QueueBuilder
import net.lag.kestrel.config.Protocol
import com.twitter.util._
import net.lag.kestrel.PersistentQueue
import org.kevoree.framework.message.Message


/**
 * Created by IntelliJ IDEA.
 * User: jedartois@gmail.com
 * Date: 14/12/11
 * Time: 09:29
 * To change this template use File | Settings | File Templates.
 */

object testPersitentQueue extends App {

  val timer = new JavaTimer()

  val builder = new QueueBuilder {
    defaultJournalSize = 64.bytes
  }

  val queueConfig =     builder.apply();

  // attention si utilisation PerstistentQueue il faut reload
  val q = new PersistentQueue("work", "/var/spool/kestrel", queueConfig, timer, null)

  q.setup()

  var msgToEnqueue = new Message()
  msgToEnqueue.setContent("jed")

  q.add(KevoreeUtil.toBinary(msgToEnqueue))

  q.close()

  exit()
}