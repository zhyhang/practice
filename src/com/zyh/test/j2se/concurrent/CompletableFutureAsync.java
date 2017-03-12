/**
 * 
 */
package com.zyh.test.j2se.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhyhang
 *
 */
public class CompletableFutureAsync {

	/**
	 * @param args
	 * @throws Exception
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws Exception {
		AtomicInteger runCounter = new AtomicInteger(0);
		Runnable timeWaitRun = () -> {
			int sequence = runCounter.incrementAndGet();
			System.out.println("task" + sequence + "\tsleeping...");
			try {
				TimeUnit.MILLISECONDS.sleep(1000*sequence);
				System.out.println("task" + sequence + "\twake");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
		// issue 3 task
		CompletableFuture<Void> cfs = CompletableFuture.allOf(CompletableFuture.runAsync(timeWaitRun),
				CompletableFuture.runAsync(timeWaitRun), CompletableFuture.runAsync(timeWaitRun));
		System.out.println("waiting...");
		cfs.get(3, TimeUnit.SECONDS);
//		cfs.get(2, TimeUnit.SECONDS); // will throw timeout exception, because task3 not complete
		System.out.println("all complete normally!");

	}

}
