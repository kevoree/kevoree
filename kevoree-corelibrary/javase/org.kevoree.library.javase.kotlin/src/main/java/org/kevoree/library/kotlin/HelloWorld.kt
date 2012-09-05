package org.kevoree.library.kotlin



public val KotlinHelloString : String = "Hello from Kotlin!!!"

public fun getHelloStringFromJava() : String {
    return KotlinHelloWorld.JavaHelloString.sure();
}

class KotlinHelper() {

    fun sayHello(dropHelper : String) : String {
         return "helloKotLinHelper ${dropHelper}//${getHelloStringFromJava()}"
    }

    fun sayHelloToAll() : Unit {
        for (x in 1..5){
            print(x)
        }
    }


}

