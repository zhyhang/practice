package com.zyh.test.j2se.string.hash;

import net.openhft.hashing.LongHashFunction;

/**
 * https://github.com/OpenHFT/Zero-Allocation-Hashing
 * @author zhyhang
 *
 */
public class ZeroAllocationHashing {
	public static void main(String[] args) {
		String hashStr="https://fms.ipinyou.com/a.jsp";
		System.out.format("city hash code: %d,java hash code: %d.\n", LongHashFunction.city_1_1().hashChars(hashStr),hashStr.hashCode());
	}
}
