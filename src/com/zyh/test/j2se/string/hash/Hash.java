package com.zyh.test.j2se.string.hash;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class Hash {
	public static void main(String[] args) {
		String str = "Java Programming Language";
//		System.out.println(str + " hashCode(): " + str.hashCode() + " new Java 7 hashCode used by collections: "
//				+ sun.misc.Hashing.stringHash32(str));
//		System.out.println(str + " hashCode(): " + str.hashCode() + " new Java 7 hashCode used by collections: "
//				+ sun.misc.Hashing.stringHash32(str));
		double d=12222222222.11111111d;
		BigDecimal bd=new BigDecimal(d);
//		bd=bd.setScale(3, BigDecimal.ROUND_HALF_UP);
		
		System.out.println(Double.toString(d));
		System.out.println(bd.toString());
		NumberFormat f = NumberFormat.getNumberInstance();
		f.setGroupingUsed(false);
		System.out.println(f.format(d));
		double newD=Math.round((d+0.0005d)*1000d)/1000d;
		System.out.println(newD);
		
		System.out.println("test end");//OK
		
		System.out.println(-1l * Integer.MAX_VALUE);
	}
}