package org.kevoree.library.javase.kinect.osc;
/*
 * Copyright (c) 1997-1999 The Java Apache Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * 4. The names "Apache JServ", "Apache JServ Servlet Engine" and 
 *    "Java Apache Project" must not be used to endorse or promote products 
 *    derived from this software without prior written permission.
 *
 * 5. Products derived from this software may not be called "Apache JServ"
 *    nor may "Apache" nor "Apache JServ" appear in their names without 
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *    
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Group. For more information
 * on the Java Apache Project and the Apache JServ Servlet Engine project,
 * please see <http://java.apache.org/>.
 *
 */


import java.io.*;

/**
 * Static methods for managing byte arrays
 * (all methods follow Big Endian order
 * where most significant bits are in front).
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author Ben Chun
 * @version 1.8 (added toFloat and toDouble methods)
 */
public class Bytes {

	/**
	 * Build an int from first 4 bytes of the array.
	 *
	 * @param b the byte array to convert.
	 */
	public static int toInt(byte[] b) {
		return (((int) b[3]) & 0xFF) +
				((((int) b[2]) & 0xFF) << 8) +
				((((int) b[1]) & 0xFF) << 16) +
				((((int) b[0]) & 0xFF) << 24);
	}

	/**
	 * Build a long from first 8 bytes of the array.
	 *
	 * @param b the byte array to convert.
	 */
	public static long toLong(byte[] b) {
		return (((long) b[7]) & 0xFF) +
				((((long) b[6]) & 0xFF) << 8) +
				((((long) b[5]) & 0xFF) << 16) +
				((((long) b[4]) & 0xFF) << 24) +
				((((long) b[3]) & 0xFF) << 32) +
				((((long) b[2]) & 0xFF) << 40) +
				((((long) b[1]) & 0xFF) << 48) +
				((((long) b[0]) & 0xFF) << 56);
	}

	/**
	 * Build a float from the first 4 bytes of the array.
	 *
	 * @param b the byte array to convert.
	 */
	public static float toFloat(byte[] b) {
		int i = toInt(b);
		return Float.intBitsToFloat(i);
	}

	/**
	 * Build a double-precision float from the first 8 bytes of the array.
	 *
	 * @param b the byte array to convert.
	 */
	public static double toDouble(byte[] b) {
		long l = toLong(b);
		return Double.longBitsToDouble(l);
	}


	/**
	 * Returns a 4-byte array built from an int.
	 *
	 * @param n the number to convert.
	 */
	public static byte[] toBytes(int n) {
		return toBytes(n, new byte[4]);
	}


	/**
	 * Build a 4-byte array from an int.
	 * No check is performed on the array length.
	 *
	 * @param n the number to convert.
	 * @param b the array to fill.
	 */
	public static byte[] toBytes(int n, byte[] b) {
		b[3] = (byte) (n);
		n >>>= 8;
		b[2] = (byte) (n);
		n >>>= 8;
		b[1] = (byte) (n);
		n >>>= 8;
		b[0] = (byte) (n);

		return b;
	}

	/**
	 * Returns a 8-byte array built from a long.
	 *
	 * @param n the number to convert.
	 */
	public static byte[] toBytes(long n) {
		return toBytes(n, new byte[8]);
	}

	/**
	 * Build a 8-byte array from a long.
	 * No check is performed on the array length.
	 *
	 * @param n the number to convert.
	 * @param b the array to fill.
	 */
	public static byte[] toBytes(long n, byte[] b) {
		b[7] = (byte) (n);
		n >>>= 8;
		b[6] = (byte) (n);
		n >>>= 8;
		b[5] = (byte) (n);
		n >>>= 8;
		b[4] = (byte) (n);
		n >>>= 8;
		b[3] = (byte) (n);
		n >>>= 8;
		b[2] = (byte) (n);
		n >>>= 8;
		b[1] = (byte) (n);
		n >>>= 8;
		b[0] = (byte) (n);

		return b;
	}

	/**
	 * Compares two byte arrays for equality.
	 *
	 * @return true if the arrays have identical contents
	 */
	public static boolean areEqual(byte[] a, byte[] b) {
		int aLength = a.length;
		if (aLength != b.length) return false;

		for (int i = 0; i < aLength; i++)
			if (a[i] != b[i]) return false;

		return true;
	}

	/**
	 * Appends two bytes array into one.
	 */
	public static byte[] append(byte[] a, byte[] b) {
		byte[] z = new byte[a.length + b.length];
		System.arraycopy(a, 0, z, 0, a.length);
		System.arraycopy(b, 0, z, a.length, b.length);
		return z;
	}

	/**
	 * Appends three bytes array into one.
	 */
	public static byte[] append(byte[] a, byte[] b, byte[] c) {
		byte[] z = new byte[a.length + b.length + c.length];
		System.arraycopy(a, 0, z, 0, a.length);
		System.arraycopy(b, 0, z, a.length, b.length);
		System.arraycopy(c, 0, z, a.length + b.length, c.length);
		return z;
	}

	/**
	 * Gets the end of the byte array given.
	 *
	 * @param b   byte array
	 * @param pos the position from which to start
	 * @return a byte array consisting of the portion of b between pos and
	 *         the end of b.
	 */
	public static byte[] copy(byte[] b, int pos) {
		return copy(b, pos, b.length - pos);
	}

	/**
	 * Gets a sub-set of the byte array given.
	 *
	 * @param b	  byte array
	 * @param pos	the position from which to start
	 * @param length the number of bytes to copy from the original byte array
	 *               to the new one.
	 * @return a byte array consisting of the portion of b starting at pos
	 *         and continuing for length bytes, or until the end of b is reached,
	 *         which ever occurs first.
	 */
	public static byte[] copy(byte[] b, int pos, int length) {
		byte[] z = new byte[length];
		System.arraycopy(b, pos, z, 0, length);
		return z;
	}

	/**
	 * Merges a bytes array into another starting from the
	 * given positions.
	 */
	public static void merge(byte[] src, byte[] dest, int srcpos, int destpos, int length) {
		System.arraycopy(src, srcpos, dest, destpos, length);
	}

	/**
	 * Merges a bytes array into another starting from the
	 * given position.
	 */
	public static void merge(byte[] src, byte[] dest, int pos) {
		System.arraycopy(src, 0, dest, pos, src.length);
	}

	/**
	 * Merges a bytes array into another.
	 */
	public static void merge(byte[] src, byte[] dest) {
		System.arraycopy(src, 0, dest, 0, src.length);
	}

	/**
	 * Merges a bytes array into another starting from the
	 * given position.
	 */
	public static void merge(byte[] src, byte[] dest, int pos, int length) {
		System.arraycopy(src, 0, dest, pos, length);
	}

	private static final char[] hexDigits = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};

	/**
	 * Returns a string of hexadecimal digits from a byte array,
	 * starting at offset and continuing for length bytes.
	 */
	public static String toString(byte[] b, int offset, int length) {
		char[] buf = new char[length * 2];

		for (int i = offset, j = 0, k; i < offset + length; i++) {
			k = b[i];
			buf[j++] = hexDigits[(k >>> 4) & 0x0F];
			buf[j++] = hexDigits[k & 0x0F];
		}

		return new String(buf);
	}

	/**
	 * Returns a string of hexadecimal digits from a byte array..
	 */
	public static String toString(byte[] b) {
		return toString(b, 0, b.length);
	}
}
