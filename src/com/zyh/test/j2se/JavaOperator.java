package com.zyh.test.j2se;

import java.util.Arrays;
import java.util.GregorianCalendar;

public class JavaOperator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("a,b,".split(","));
		System.out.println("a,b".split(","));
		System.out.println("a".split(","));
		System.out.println(",,".split(","));
		
		System.out.println(b() && a());
		System.out.println(b() & a());
		
		GregorianCalendar cale=new GregorianCalendar();
		cale.setTimeInMillis(1377485730358L);
		System.out.println(cale);
		System.out.println(Arrays.deepToString("192.168.144.52:7200,192.168.144.52:7300,192.168.144.53:7200,192.168.144.53:7300,192.168.144.54:7200,192.168.144.54:7300,192.168.144.55:7200,192.168.144.55:7300,192.168.144.56:7200,192.168.144.56:7300,192.168.144.57:7200,192.168.144.57:7300,192.168.144.58:7200,192.168.144.58:7300,192.168.144.59:7200,192.168.144.59:7300,192.168.144.16:7200,192.168.144.16:7300,192.168.144.20:7200,192.168.144.20:7300,192.168.144.131:7200,192.168.144.131:7300,192.168.144.132:7200,192.168.144.132:7300".split(",")));
		
	}
	
	private static boolean a(){
		System.out.println("in a()");
		return true;
	}
	
	private static  boolean b(){
		System.out.println("in b()");
		return false;
	}

}
