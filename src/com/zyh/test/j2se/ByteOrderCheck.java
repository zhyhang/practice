package com.zyh.test.j2se;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Java大端，小端测试
 * @author zhyhang
 *
 */
public class ByteOrderCheck {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("JVM's endian :"+ByteOrder.BIG_ENDIAN+".");
		System.out.println("This machine's endian :"+ByteOrder.nativeOrder().toString()+".");
		long along=0x1234567890abcdefl;
		System.out.println("DEC:\t"+along);
		System.out.println("HEX:\t0x"+Long.toHexString(along));
		System.out.println();
		System.out.println("By\t"+ByteOrder.LITTLE_ENDIAN+":");
		ByteBuffer bb = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(along);
		byte[] longBytes=bb.array();
		System.out.printf("put and read the long value:[0x%x]\n",bb.getLong(0));
		System.out.println("the long value in memory:");
		System.out.println("low memory address -> high memory address");
		printLongBytes(longBytes);
		System.out.println();
		System.out.println();
		System.out.println("By\t"+ByteOrder.BIG_ENDIAN+":");
		bb = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(along);
		longBytes=bb.array();
		System.out.printf("put and read the long value:[0x%x]\n",bb.getLong(0));
		System.out.println("the long value in memory:");
		System.out.println("low memory address -> high memory address");
		printLongBytes(longBytes);
		System.out.println();
		System.out.println();
		System.out.println(ByteOrder.BIG_ENDIAN+"\thigh byte storage in low memory address.");
		System.out.println(ByteOrder.LITTLE_ENDIAN+"\tlow byte storage in low memory address.");
	}

	private static void printLongBytes(byte[] longBytes) {
		System.out.print("HEX:0x");
		for (int i = 0; i < longBytes.length; i++) {
			byte b=longBytes[i];
			String wholeHex=Integer.toHexString((int)b);
			System.out.print(wholeHex.substring(wholeHex.length()-2, wholeHex.length()));
		}
	}

}
