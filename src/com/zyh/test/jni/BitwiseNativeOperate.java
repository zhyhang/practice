/**
 * 
 */
package com.zyh.test.jni;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author zhyhang
 *
 */
public class BitwiseNativeOperate {

	private final static String LIB_PATH = "/home/zhyhang/code/assembly/";
	private final static int LOOP_COUNT = 100;

	static {
		System.load(LIB_PATH.concat("bitwise-avx.so"));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long[] al = new long[2000];
		long[] bl = new long[2000];
		Arrays.setAll(al, i -> i + 1);
		Arrays.setAll(bl, i -> i + 2);
		// warm up
		for (int i = 0; i < 2; i++) {
			System.out.println(print_var_addr(al, bl));
		}
		long tsb = System.nanoTime();
		print_var_addr(al, bl);
		System.out.printf("union_native [%d]ns.\n", System.nanoTime() - tsb);
//		System.out.println(Arrays.toString(al));
//		System.out.println(Arrays.toString(bl));
		Arrays.setAll(al, i -> i + 1);
		Arrays.setAll(bl, i -> i + 2);
		// warm up
		for (int i = 0; i < 2; i++) {
			System.out.println(union(al, bl));
		}
		tsb = System.nanoTime();
		union(al, bl);
		System.out.printf("union_java [%d]ns.\n", System.nanoTime() - tsb);
		
		parallel(al, bl);

//		System.out.println(Arrays.toString(al));
//		System.out.println(Arrays.toString(bl));

	}
	
	public static void parallel(long[] al, long[] bl){
		ExecutorService es = Executors.newFixedThreadPool(20);
		long tsb = System.nanoTime();
		for (int i = 0; i < 10000; i++) {
			es.execute(()->{
//				print_var_addr(al,bl);
				union(al,bl);
			});
		}
		es.shutdown();
		try {
			es.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.printf("union_parallel [%d]ns.\n", System.nanoTime() - tsb);
	}

	public static native long print_var_addr(long[] al, long[] bl);

	public static long union(long[] al, long bl[]) {
		int c = 0;
		for (int j = 0; j < LOOP_COUNT; j++) {
			for (int i = 0; i < al.length; i++) {
				al[i] |= bl[i];
				c++;
			}
		}
		return c;
	}

}
