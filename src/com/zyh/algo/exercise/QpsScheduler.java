/**
 * 
 */
package com.zyh.algo.exercise;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhyhang
 *
 */
public class QpsScheduler implements Closeable {

	private int threadNum = Runtime.getRuntime().availableProcessors();

	private final ScheduledExecutorService ses;

	public QpsScheduler(int threadNum) {
		if (threadNum > 0) {
			this.threadNum = threadNum;
		}
		ses = Executors.newScheduledThreadPool(this.threadNum);
	}

	public void scheduleQps(int qps, Runnable task, long totalRequests) {
		qps = qps < 1 ? 100 : qps;
		int reqPer10ms = qps / 100;
		int reqPer100ms = (qps - reqPer10ms * 100) / 10;
		int reqPerSecond = qps % 10;
		AtomicLong requestCount = new AtomicLong(0);
		sendRequest(10, reqPer10ms, task, requestCount, totalRequests);
		sendRequest(100, reqPer100ms, task, requestCount, totalRequests);
		sendRequest(1000, reqPerSecond, task, requestCount, totalRequests);
	}

	private void sendRequest(int timePerSegment, int qpsPerSegment, Runnable task, AtomicLong requestCount,
			long totalRequests) {
		ses.scheduleAtFixedRate(() -> {
			for (int i = 0; i < qpsPerSegment; i++) {
				if (requestCount.incrementAndGet() < totalRequests) {
					task.run();
				}
			}
		}, timePerSegment, timePerSegment, TimeUnit.MILLISECONDS);
	}

	@Override
	public void close() throws IOException {
		ses.shutdownNow();
		try {
			ses.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
	}

}
