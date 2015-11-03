/**
 * 
 */
package com.zyh.test.memory.footprint;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Estimate Concurrent hashmap memory footprint.
 * 
 * @author zhyhang
 *
 */
public class CHMCapacity {

	private static ConcurrentHashMap<String, int[]> mapInts = new ConcurrentHashMap<>();

	private static ConcurrentHashMap<String, AtomicInteger[]> mapAtoms = new ConcurrentHashMap<>();

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
//		intCapacity();
		atomicCapacity();
	}

	private static void intCapacity() throws InterruptedException{
		TimeUnit.SECONDS.sleep(3);
		int count=5000000;
		for (int i = 0; i < count; i++) {
			mapInts.put("nt:a:d:"+Long.valueOf(i), new int[]{ThreadLocalRandom.current().nextInt(),
					ThreadLocalRandom.current().nextInt(),ThreadLocalRandom.current().nextInt(),
					ThreadLocalRandom.current().nextInt(),ThreadLocalRandom.current().nextInt(),
					ThreadLocalRandom.current().nextInt()});
		}
		System.gc();
		System.out.format("Concurrent hash map: [%d]entries, string key, int[6] value, memory footprint [%d] bytes.\n", mapInts.size(),
				Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		TimeUnit.SECONDS.sleep(2);
	}
	
	private static void atomicCapacity() throws InterruptedException{
		TimeUnit.SECONDS.sleep(3);
		int count=1000000;
		for (int i = 0; i < count; i++) {
			mapAtoms.put("nt:a:d:"+Long.valueOf(i), new AtomicInteger[]{new AtomicInteger(ThreadLocalRandom.current().nextInt()),
					new AtomicInteger(ThreadLocalRandom.current().nextInt()),new AtomicInteger(ThreadLocalRandom.current().nextInt()),
							new AtomicInteger(ThreadLocalRandom.current().nextInt()),new AtomicInteger(ThreadLocalRandom.current().nextInt()),
									new AtomicInteger(ThreadLocalRandom.current().nextInt())});
		}
		System.gc();
		System.out.format("Concurrent hash map: [%d]entries, string key, int[6] value, memory footprint [%d] bytes.\n", mapAtoms.size(),
				Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		TimeUnit.SECONDS.sleep(2);
	}

}
