package org.kevoree.library.javase.kestrelChannels

import com.twitter.logging.config.{FileHandlerConfig, LoggerConfig}
import com.twitter.ostrich.admin.RuntimeEnvironment
import com.twitter.util.{JavaTimer, Timer, Duration, StorageUnit}
import java.net._
import java.nio._
import java.nio.channels._
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.TimeUnit
import net.lag.kestrel.config.{Protocol, QueueBuilder, QueueConfig}
import net.lag.kestrel.{Kestrel, PersistentQueue}
import scala.collection.mutable
import com.twitter.conversions.string._
import com.twitter.conversions.storage._
import com.twitter.ostrich.admin.config._
import net.lag.kestrel.config._
import com.twitter.logging.Logger
import com.twitter.logging.config._
import actors.Actor._
import actors.DaemonActor
import java.lang.Thread

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 30/11/11
 * Time: 14:53
 * To change this template use File | Settings | File Templates.
 */

class KestrelServer(host : String,port :Int,queuePath : String ,filepathlog :String) {

  private val PORT = this.port
  private var kestrel: Kestrel = null
  private val log = Logger.get(getClass)


  val runtime = RuntimeEnvironment(this, Array())
  Kestrel.runtime = runtime

  runServer()


  def runServer() = {
    new Thread(){
      override  def run() {
        val defaultConfig = new QueueBuilder() {
          defaultJournalSize = 16.megabytes
          maxMemorySize = 128.megabytes
          maxJournalSize = 1.gigabyte
        }.apply()

        // make a queue specify max_items and max_age
        val UpdatesConfig = new QueueBuilder() {
          maxSize = 128.megabytes
          maxMemorySize = 16.megabytes
          maxJournalSize = 128.megabytes
        }

        //""
        kestrel = new Kestrel(defaultConfig, List(UpdatesConfig),host,
          Some(PORT), None,queuePath, Protocol.Ascii, None, None, 1)

        kestrel.start()
      }
    }.start()
  }

  def stopServer()= {
    kestrel.shutdown()

  }

  def reloadServer() = {
    //todo change conf
    kestrel.reload()
  }

  val   config = new LoggerConfig {
    level = Level.DEBUG
    handlers = new FileHandlerConfig {
      filename = filepathlog
      roll = Policy.Never
    }
  }
  config()

}