package com.zyh.test.j2se.bitset;

import java.util.BitSet;

/**
 * Bit set using for ip segments
 * @author zhyhang
 *
 */
public class BitSetSpace {
	
	private static final long ONE_32 = 0xFFFFFFFFL;

	public static void main(String[] args) {
		BitSet bs = new BitSet();
		bs.set(0, 10);
		bs.set(11, 30);
		bs.set(36);
		bs.set(Integer.MAX_VALUE-1);
		// generate 100 bits
		for (int i = 0; i < 100; i++) {
			int bIndex = (int) (Integer.MAX_VALUE * Math.random());
			if (bIndex > Integer.MAX_VALUE - 5) {
				bIndex = Integer.MAX_VALUE - 5;
			}
			bs.set(bIndex);
			bs.set(bIndex + 1);
			bs.set(bIndex + 2);
			bs.set(bIndex + 3);
		}
//		longBsIntervalOutput(new BitSet[]{bs,bs});
		System.out.println(bs.length());
//		System.out.println(bs.toString());
		System.out.println((int)((ONE_32 +Integer.MAX_VALUE-2 )% Integer.MAX_VALUE));
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
	
	private static void longBsIntervalOutput(BitSet[] bss){
		long b = -1;
		long e = b;
		for (int bssi = 0; bssi < bss.length; bssi++) {
			long offset=bssi*(long)Integer.MAX_VALUE;
			for (int bsi = 0; bsi < bss[bssi].length(); bsi++) {
				if (!bss[bssi].get(bsi)) {
					if(b!=-1){
						System.out.println("[" + (int)(b & ONE_32) + "," + (int)(e & ONE_32) + "]");
						b=-1;
					}
				} else {
					e = bsi+offset;
					b=b==-1?bsi+offset:b;
				}
			}
		}
		if(b!=-1){
			System.out.println("[" + (int)(b & ONE_32) + "," + (int)(e & ONE_32) + "]");
		}
	}
	
	private static void addIpInterval(BitSet[] bss, int bip, int eip) {
		long lbip = bip & ONE_32;
		long leip = eip & ONE_32;
		while (lbip <= leip) {
			int bssi = (int) (lbip / Integer.MAX_VALUE);
			int bsi = (int) (lbip % Integer.MAX_VALUE);
			bss[bssi].set(bsi);
			lbip++;
		}
	}

}
