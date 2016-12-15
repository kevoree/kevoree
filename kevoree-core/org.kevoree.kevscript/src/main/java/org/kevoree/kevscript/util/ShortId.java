package org.kevoree.kevscript.util;

import java.util.Random;

/**
 *
 * Created by leiko on 12/14/16.
 */
public class ShortId {

    private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String gen() {
        final StringBuilder builder = new StringBuilder();
        final Random random = new Random();
        for (int i = 0; i < 6; i++) {
            builder.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return builder.toString();
    }
}
