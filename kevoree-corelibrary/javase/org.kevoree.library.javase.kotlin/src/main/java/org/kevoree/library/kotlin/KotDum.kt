package org.kevoree.library.kotlin.t2

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 21/10/12
 * Time: 19:45
 * To change this template use File | Settings | File Templates.
 */



fun main(args : Array<String>) {
    var l = java.util.ArrayList<String>()
    l.add("Yop")
    l.forEach { a -> (println("elem=${a}"))}
}
