package org.kevoree.library.javase.kestrelChannels

/**
 * Created by IntelliJ IDEA.
 * User: jedartois@gmail.com
 * Date: 01/12/11
 * Time: 11:53
 * To change this template use File | Settings | File Templates.
 */

object testServer extends App {

              var server = new KestrelServer("localhost",22133, "/var/spool/kestrel", "/var/log/kestrel/kestrel.log",false)


}