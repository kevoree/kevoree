package org.kevoree.library.javase.gossiperNetty.sendNotification

import org.kevoree.library.javase.gossiperNetty.{ModificationGenerator, BootStrapAppComplex, Configuration}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 04/08/11
 * Time: 10:32
 */

object RunCompleteExperimentation extends App {

  var alwaysAskModel = false
  var sendNotification = true
  var delay = 10000
  var reconfigurationDelay = 20000
  var experimentationTime = 60000
  //var nodeFile = ""

  if (args.contains("alwaysAskModel")) {
    alwaysAskModel = true
  } else if (args.contains("-alwaysAskModel")) {
    alwaysAskModel = false
  }

  if (args.contains("sendNotification")) {
    sendNotification = true
  } else if (args.contains("-sendNotification")) {
    sendNotification = false
  }

  args.filter(arg => arg.startsWith("delay=")).foreach {
    arg => delay = Integer.parseInt(arg.substring("delay=".size, arg.size))
  }

  args.filter(arg => arg.startsWith("nodeFile=")).foreach {
    arg => Configuration.nodeFile = arg.substring("nodeFile=".size, arg.size);
  }

  args.filter(arg => arg.startsWith("reconfigurationDelay=")).foreach {
    arg => reconfigurationDelay = Integer.parseInt(arg.substring("reconfigurationDelay=".size, arg.size))
  }

  args.filter(arg => arg.startsWith("experimentationTime=")).foreach {
    arg => experimentationTime = Integer.parseInt(arg.substring("experimentationTime=".size, arg.size))
  }

  Configuration.build()


  println(Configuration.packets.mkString("\n"))
  println("followingPlatform = " + Configuration.followingPlatform)
  println("sendNotification = " + sendNotification)
  println("alwaysAskModel = " + alwaysAskModel)
  println("delay = " + delay)
  println("")
  println("reconfiguration delay = " + reconfigurationDelay)
  println("experimentation Time = " + experimentationTime)

  // !!!!!!!!!!!! Agent must be started !!!!!!!!!
  BootStrapAppComplex
    .bootStrap(Configuration.packets, Configuration.logServer, Configuration.ips, sendNotification, alwaysAskModel,
                delay)

  Thread.sleep(60000)
  val modificationGenerator = new ModificationGenerator(Configuration.ips)
  val endTime = System.currentTimeMillis() + experimentationTime

  while (System.currentTimeMillis() < endTime) {
    // run modification modification
    modificationGenerator.doAction(Configuration.followingPlatform)

    // sleep to wait the reconfiguration delay
    Thread.sleep(reconfigurationDelay)
  }
}