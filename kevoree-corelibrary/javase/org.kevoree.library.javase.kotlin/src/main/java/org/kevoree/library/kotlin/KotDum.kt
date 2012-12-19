package org.kevoree.library.kotlin.t2

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 21/10/12
 * Time: 19:45
 */


fun main(args: Array<String>) {
    var l = java.util.ArrayList<String>()
    l.add("Yop")
    l.forEach { a -> (println("elem=${a}")) }
    l.forEach { a -> (println("elem=${a}")) }
    l.forEach { a -> (println("elem=${a}")) }
    for(it in l){
        println("elem=${it}")
    }

    l.forEach { a -> binarySearch(a)  }
    l.forEach { a -> binarySearch(a)  }


}

public inline fun binarySearch(key: String) : Unit {
    println(3)
}
