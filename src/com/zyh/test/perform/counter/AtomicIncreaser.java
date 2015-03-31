/**
 * 
 */
package com.zyh.test.perform.counter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Atomic Counter Performance PK on Jdk8
 * 
 * @author zhyhang
 *
 */
public class AtomicIncreaser {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		final long increCount = 100000000l;
		final LongAdder adder = new LongAdder();
		final AtomicLong along = new AtomicLong(0);
		int nThreads = 128;
		ExecutorService esAdder = Executors.newFixedThreadPool(nThreads);
		ExecutorService esAlong = Executors.newFixedThreadPool(nThreads);
		long tsBegin = System.nanoTime();
		for (int i = 0; i < nThreads; i++) {
			esAdder.execute(() -> {
				while (adder.longValue() < increCount) {
					adder.increment();
				}
			});
		}
		esAdder.shutdown();
		esAdder.awaitTermination(1, TimeUnit.DAYS);
		long adderCostNs = System.nanoTime() - tsBegin;
		tsBegin = System.nanoTime();
		for (int i = 0; i < nThreads; i++) {
			esAlong.execute(() -> {
				while (along.get() < increCount) {
					along.incrementAndGet();
				}
			});
		}
		esAlong.shutdown();
		esAlong.awaitTermination(1, TimeUnit.DAYS);
		long alongCostNs = System.nanoTime() - tsBegin;
		System.out
				.format("Add [%d] times by [%d] threads. Time cost (ms) LongAdder[value=%d] vs AtomicLong[value=%d]: [%d] vs [%d]",
						increCount, nThreads, adder.longValue(),
						along.longValue(),
						TimeUnit.NANOSECONDS.toMillis(adderCostNs),
						TimeUnit.NANOSECONDS.toMillis(alongCostNs));

	}

}
