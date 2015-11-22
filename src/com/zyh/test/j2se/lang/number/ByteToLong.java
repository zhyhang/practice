/**
 * 
 */
package com.zyh.test.j2se.lang.number;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * @author zhyhang
 *
 */
public class ByteToLong {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ByteBuffer bb = ByteBuffer.allocate(8);
		byte[] bbbs=bb.array();
//		long lv = -0xffff000000001l;
		long lv= 0xffff;
		bb.putLong(lv);
		long timeBb=0;
		long timeBtoL=0;
		long plv = 0;
		for (int i = 0; i < 100000000; i++) {
			long tsb = System.nanoTime();
			bb.position(0);
			plv = bb.getLong();
			timeBb+=System.nanoTime() - tsb;
			tsb=System.nanoTime();
			plv = byteToLong(bbbs);
			timeBtoL+=System.nanoTime() - tsb;
		}
		System.out.format("orig long value/parse long vlue: %d/%d\n", lv, plv);
		System.out.format("time cost ByteBuffer/byteToLong[%d/%d]ms\n", TimeUnit.NANOSECONDS.toMillis(timeBb), 
				TimeUnit.NANOSECONDS.toMillis(timeBtoL));

	}

	private static long byteToLong(byte[] id) {
		long v = 0;
		for (int i = 0; i < Long.BYTES; i++) {
			v |= 0xff00000000000000L >>> i * Byte.SIZE & (long) id[i] << Long.SIZE - (i + 1) * Byte.SIZE;
		}
		return v;
	}

}
