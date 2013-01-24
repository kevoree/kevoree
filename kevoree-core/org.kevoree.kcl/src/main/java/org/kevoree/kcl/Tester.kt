package org.kevoree.kcl

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 1/24/13
 * Time: 12:47 AM
 */

fun main(args : Array<String>){
    println("HelloKCL")

    val kcl1 = KevoreeJarClassLoader()
    kcl1.add("/Users/duke/Documents/dev/dukeboard/kevoree-kotlin/kevoree-core/org.kevoree.model/target/org.kevoree.model-1.9.0-SNAPSHOT.jar")

    println(kcl1.loadClass("org.kevoree.ContainerRoot"))


    val kcl12 = KevoreeJarClassLoader()
    kcl12.addSubClassLoader(kcl1)
    println(kcl1.loadClass("org.kevoree.ChannelType"))

}
