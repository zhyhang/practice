/**
 * 
 */
package com.zyh.test.j2se.lang.number;

/**
 * @author zhyhang
 *
 */
public class LongNumberOfTrailingZeros {
	public static void main(String[] args) {
		long l1024 = 1024L;
		long lmax = 0xffffffffffffffffL;
		long l1 = 1L;
		long l64 = 64L;
		long l31 = 31L;
		System.out.format("numberOfTrailingZeros: l1024[%s-%d], lmax[%s-%d], l1[%s-%d], l64[%s-%d], l32[%s-%d].\n",
				Long.toBinaryString(l1024), Long.numberOfTrailingZeros(1024L), Long.toBinaryString(lmax),
				Long.numberOfTrailingZeros(lmax), Long.toBinaryString(l1), Long.numberOfTrailingZeros(l1),
				Long.toBinaryString(l64), Long.numberOfTrailingZeros(l64), Long.toBinaryString(l31),
				Long.numberOfTrailingZeros(l31));
	}
}
