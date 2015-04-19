/**
 * 
 */
package com.zyh.test.perform.gc;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Show gc pause effect on application response
 * 
 * @author zhyhang
 *
 */
public class PauseTimeShow {

	private final static AtomicLong counter = new AtomicLong(0);

	private final static AtomicLong timecostTotal = new AtomicLong(0);

	private static long timecostMax = 0;

	private static long timecostMin = Long.MAX_VALUE;

	private static long temp;

	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (counter.longValue() == 0) {
					return;
				}
				print();
				System.out.println();
			}

		});
	}

	public static void main(String[] argv) {
		if (argv.length < 1 || !argv[0].equals("-gc") && !argv[0].equals("-nogc")) {
			System.out.println("Usage:\n\tjava\t" + PauseTimeShow.class.getSimpleName() + "\t-gc/-nogc");
			System.exit(1);
		}
		final boolean isGc = argv[0].equals("-gc");
		// create a thread making garbage
		new Thread(new Runnable() {
			@Override
			public void run() {
				int size = 1024 * 1024 * 500;
				byte[] bsStatic = new byte[size];
				while (true) {
					// allocate big data
					byte[] bs = null;
					if (isGc) {
						bs = new byte[size];
					} else {
						bs = bsStatic;
					}
					temp = bs.length;
				}
			}
		}).start();
		while (true) {
			long ts = System.currentTimeMillis();
			long sum = 0;
			for (int i = 0; i < 800000000; i++) {
				sum++;
			}
			temp = sum;
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
			System.out.format("CurrentTimeCost[%d]", timecost);
		}
	}

	private static void print() {
		System.out.println();
		System.out.format("[%1$tY%1$tm%1$td%1$tH%1$tM%1$tS,%1$tL]", System.currentTimeMillis());
		System.out.format(
				"Counter[%d]TotalTime[%d]AvgTime[%d]MaxTime[%d]Mintime[%d](unit:ms)",
				counter.longValue(),
				timecostTotal.longValue(),
				counter.longValue() > 2 ? (timecostTotal.longValue() - timecostMax - timecostMin)
						/ (counter.longValue() - 2) : timecostTotal.longValue() / counter.longValue(), timecostMax,
				timecostMin);
	}

}
