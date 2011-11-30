package org.kevoree.library.javase.kestrelChannels

import net.lag.kestrel.Kestrel
import com.twitter.logging.config.{FileHandlerConfig, LoggerConfig}
import com.twitter.ostrich.admin.RuntimeEnvironment._
import net.lag.kestrel.config.{Protocol, QueueBuilder}
import com.twitter.ostrich.admin.RuntimeEnvironment
import com.twitter.logging.{Level, Policy, Logger}

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 30/11/11
 * Time: 14:53
 * To change this template use File | Settings | File Templates.
 */

class KestrelServer {

  val PORT = 22133
  var kestrel: Kestrel = null

  private val log = Logger.get(getClass)

  val   config = new LoggerConfig {
    level = Level.DEBUG
    handlers = new FileHandlerConfig {
      filename = "/var/log/kestrel/kestrel.log"
      roll = Policy.Never
    }
  }

  config()

  val runtime = RuntimeEnvironment(this, Array())
  Kestrel.runtime = runtime

  def run() = {

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


    kestrel = new Kestrel(defaultConfig, List(UpdatesConfig), "localhost",
      Some(PORT), None, "/var/spool/kestrel", Protocol.Ascii, None, None, 1)

    kestrel.start()
  }

  makeServer()
}