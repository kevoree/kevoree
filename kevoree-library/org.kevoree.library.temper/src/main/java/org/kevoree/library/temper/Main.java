package org.kevoree.library.temper;

/**
 * Created by IntelliJ IDEA.
 * User: ffouquet
 * Date: 08/02/11
 * Time: 16:44
 * To change this template use File | Settings | File Templates.
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {

        System.setProperty("jna.library.path", "/Users/ffouquet/Documents/DEV/dukeboard_github/kevoree/kevoree-library/org.kevoree.library.temper/src/main/native");

        TemperImpl.INSTANCE.initialize();
        for (int i = 0; i < 10; i++) {
            System.out.println(TemperImpl.INSTANCE.getTemperature());
            Thread.sleep(1000);
        }
        TemperImpl.INSTANCE.freeTemper();
    }


}
