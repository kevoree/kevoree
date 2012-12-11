package org.kevoree.library.sky.api.nodeType.helper;/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that performs some subnet calculations given a network address and a subnet mask. 
 * @see http://www.faqs.org/rfcs/rfc1519.html
 * @author <rwinston@apache.org>
 * @since 2.0
 */
public class SubnetUtils {

    private static final String IP_ADDRESS = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})";
    private static final String SLASH_FORMAT = IP_ADDRESS + "/(\\d{1,3})";
    private static final Pattern addressPattern = Pattern.compile(IP_ADDRESS);
    private static final Pattern cidrPattern = Pattern.compile(SLASH_FORMAT);
    private static final int NBITS = 32;

    /*
     * Convert a dotted decimal format address to a packed integer format
     */
    private static int toInteger(String address) {
        Matcher matcher = addressPattern.matcher(address);
        if (matcher.matches()) {
            return matchAddress(matcher);
        }
        else
            throw new IllegalArgumentException("Could not parse [" + address + "]");
    }

    /*
     * Convenience method to extract the components of a dotted decimal address and 
     * pack into an integer using a regex match
     */
    private static int matchAddress(Matcher matcher) {
        int addr = 0;
        for (int i = 1; i <= 4; ++i) { 
            int n = (rangeCheck(Integer.parseInt(matcher.group(i)), 0, 255));
            addr |= ((n & 0xff) << 8*(4-i));
        }
        return addr;
    }

    /*
     * Convert a packed integer address into a 4-element array
     */
    private static int[] toArray(int val) {
        int ret[] = new int[4];
        for (int j = 3; j >= 0; --j)
            ret[j] |= ((val >>> 8*(3-j)) & (0xff));
        return ret;
    }

    /*
     * Convenience function to check integer boundaries
     */
    private static int rangeCheck(int value, int begin, int end) {
        if (value >= begin && value <= end)
            return value;

        throw new IllegalArgumentException("Value out of range: [" + value + "]");
    }

    /*
     * Count the number of 1-bits in a 32-bit integer using a divide-and-conquer strategy
     * see Hacker's Delight section 5.1 
     */
    private static int pop(int x) {
        x = x - ((x >>> 1) & 0x55555555); 
        x = (x & 0x33333333) + ((x >>> 2) & 0x33333333); 
        x = (x + (x >>> 4)) & 0x0F0F0F0F; 
        x = x + (x >>> 8); 
        x = x + (x >>> 16); 
        return x & 0x0000003F; 
    } 

    /** Convert two dotted decimal addresses to a single xxx.xxx.xxx.xxx/yy format
     * by counting the 1-bit population in the mask address. (It may be better to count 
     * NBITS-#trailing zeroes for this case)
     * @return a CIDR address
     */
    public static String toCidrNotation(String addr, String mask) {
        return addr + "/" + pop(toInteger(mask));
    }

    /**
     * Convert a CIDR notation (xxx.xxx.xxx.xxx/yy) format to two dotted decimal addresses
     * @return an array of two dotted deciman addresses
     */
    public static String[] fromCidrNotation(String cidrNotation) {
        String[] element = cidrNotation.split("/");
        if (element.length > 1) {
            int[] mask = toArray(Integer.parseInt(element[1]));
            return new String[] {element[0], mask[0] + "." + mask[1]+"." + mask[2] + "." + mask[3]};
        } else {
            return new String[]{element[0], "255.255.255.0"};
        }
    }
}
