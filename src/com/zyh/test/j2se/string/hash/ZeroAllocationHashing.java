package com.zyh.test.j2se.string.hash;

import net.openhft.hashing.LongHashFunction;

/**
 * https://github.com/OpenHFT/Zero-Allocation-Hashing
 * @author zhyhang
 *
 */
public class ZeroAllocationHashing {
	public static void main(String[] args) {
		System.out.format("long hash code: %d.\n", LongHashFunction.murmur_3().hashChars("hello world"));
	}
}
