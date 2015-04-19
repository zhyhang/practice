/**
 * 
 */
package com.zyh.test.perform.gc;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 展示Gc的暂停时间
 * 
 * @author zhyhang
 *
 */
public class PauseTimeShow {

	private final static AtomicLong counter = new AtomicLong(0);

	private final static AtomicLong timecostTotal = new AtomicLong(0);

	private static long timecostMax = 0;

	private static long timecostMin = Long.MAX_VALUE;

	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				print();
			}

		});
	}

	public static void main(String[] argv) {
		byte[] bsStatic = new byte[1024 * 1024 * 500];
		while (true) {
			long ts = System.currentTimeMillis();
			// reuse
			// byte[] bs = bsStatic;
			// allocate big data
			byte[] bs = new byte[1024 * 1024 * 500];
			for (int i = 0; i < bs.length; i++) {
				bs[i] = (byte) i;
			}
			// release
			bs = null;
			long timecost = System.currentTimeMillis() - ts;
			counter.incrementAndGet();
			timecostTotal.addAndGet(timecost);
			if (timecost > timecostMax) {
				timecostMax = timecost;
			}
			if (timecost < timecostMin) {
				timecostMin = timecost;
			}
			print();
			System.out.print(timecost);
		}
	}

	private static void print() {
		System.out.println();
		System.out.format(
				"Counter[%d]TotalTime[%d]AvgTime[%d]MaxTime[%d]Mintime[%d](unit:ms)",
				counter.longValue(),
				timecostTotal.longValue(),
				counter.longValue() > 2 ? (timecostTotal.longValue() - timecostMax - timecostMin)
						/ (counter.longValue() - 2) : timecostTotal.longValue() / counter.longValue(), timecostMax,
				timecostMin);
	}

}
