/**
 * 
 */
package com.zyh.test.j2se.lang.number;

import java.nio.ByteBuffer;

/**
 * @author zhyhang
 *
 */
public class ByteToLong {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// pos(0) is a type, next 8 byte is long value
		ByteBuffer bb = ByteBuffer.allocate(9);
		bb.position(1);
		long lv = 0x1980ffff;
		bb.putLong(lv);
		long plv = byteToLong(bb.array());
		System.out.format("orig long value/parse long vlue: %d/%d", lv, plv);

	}

	private static Long byteToLong(byte[] id) {
		long v = 0;
		for (int i = 1; i <= 8; i++) {
			v |= 0xff00000000000000L >>> (i - 1) * 8 & id[i] << 64 - i * 8;
		}
		return Long.valueOf(v);
	}

}
