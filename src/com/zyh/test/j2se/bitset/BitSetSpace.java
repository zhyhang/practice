package com.zyh.test.j2se.bitset;

import java.util.BitSet;

public class BitSetSpace {

	public static void main(String[] args) {
		BitSet bs = new BitSet();
		bs.set(2, 10);
		bs.set(11, 30);
		// generate 1000 bits
		for (int i = 0; i < 1000; i++) {
			int bIndex = (int) (Integer.MAX_VALUE * Math.random());
			if (bIndex > Integer.MAX_VALUE - 5) {
				bIndex = Integer.MAX_VALUE - 5;
			}
			bs.set(bIndex);
			bs.set(bIndex + 1);
			bs.set(bIndex + 2);
			bs.set(bIndex + 3);
		}
		bitIntervalOutput(0, bs);
		System.out.println(bs.length());
		System.out.println(bs.toString());
	}
	
	private static void bitIntervalOutput(long offset,BitSet bs){
		int little = bs.nextSetBit(0);
		int b = little;
		int e = b;
		for (int i = little + 1; i < bs.length(); i++) {
			if (!bs.get(i)) {
				System.out.println("[" + (b-offset) + "," + (e-offset) + "]");
				if (i < bs.length() - 1) {
					b = bs.nextSetBit(i + 1);
					e = b;
					i = b;
				}
			} else {
				e = i;
			}
		}
		System.out.println("[" + (b-offset) + "," + (e-offset) + "]");
	}

}
