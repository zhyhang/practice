package com.zyh.test.j2se.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class VolatileField {

//	private volatile long counter = 0;// thread un-safe
	private AtomicLong counter = new AtomicLong(0);// thread safe

	public static void main(String... argv) throws InterruptedException {
		final VolatileField vf = new VolatileField();
		ExecutorService tpool = Executors.newFixedThreadPool(8);
		for (int i = 0; i < 10000; i++) {
			tpool.submit(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < 10000; i++) {
//						vf.counter++;
						vf.counter.incrementAndGet();
					}
				}
			});
		}
		tpool.shutdown();
		tpool.awaitTermination(5, TimeUnit.MINUTES);
		System.out.println(vf.counter);
	}

}
