package org.kevoree.library.reasoner.ecj;

public class testReplace {

   
    public static void main(String[] args) {
        String firstString = "${type} => ${component}";
        firstString = firstString.replace("${type}", "coucou");
        System.out.println(firstString);

    }

}
