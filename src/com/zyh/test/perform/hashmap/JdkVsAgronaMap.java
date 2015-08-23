/**
 * 
 */
package com.zyh.test.perform.hashmap;

import java.util.HashMap;
import java.util.Map;

import io.netty.util.internal.ThreadLocalRandom;
import uk.co.real_logic.agrona.collections.Long2ObjectHashMap;

/**
 * Conclusion: AgronaMap as same as JdkHashMap in performance.
 * @author zhyhang
 *
 */
public class JdkVsAgronaMap {

	private final static int MAX_SIZE = 1024 * 64;

	private final static int PUT_SIZE = 60000;

	private final static long MAX_KEY = 6000000;

	public static void main(String... argv) {
		for (int i = 0; i < 10; i++) {
			System.out.println("AgronaMap:");
			long tsb = System.nanoTime();
			Map<Long, Object> map = new Long2ObjectHashMap<>(MAX_SIZE, 0.9);
			long newTc = System.nanoTime() - tsb;
			System.out.printf("new size [%d] map time cost [%d].\n", MAX_SIZE, newTc);
			runTest(map);
			System.out.println("HashMap:");
			tsb = System.nanoTime();
			map = new HashMap<>(MAX_SIZE, 0.9f);
			newTc = System.nanoTime() - tsb;
			System.out.printf("new size [%d] map time cost [%d].\n", MAX_SIZE, newTc);
			runTest(map);
		}
	}

	private static void runTest(Map<Long, Object> map) {
		long putTc = 0;
		for (int i = 100000; i < 100000+PUT_SIZE; i++) {
			Long key=Long.valueOf(i);
//			Long key = ThreadLocalRandom.current().nextLong(MAX_KEY);
			Object value = new Object();
			long tsb = System.nanoTime();
			map.get(key);
			map.put(key, value);
			map.containsKey(ThreadLocalRandom.current().nextLong(MAX_KEY));
			putTc += System.nanoTime() - tsb;
		}
		System.out.printf("put [%d] objects time cost [%d].\n", PUT_SIZE, putTc);

	}

}
