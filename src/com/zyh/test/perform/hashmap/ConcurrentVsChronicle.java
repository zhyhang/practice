/**
 * 
 */
package com.zyh.test.perform.hashmap;

import java.io.File;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.openhft.chronicle.map.ChronicleMapBuilder;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * Concurrent hash map performance on Jdk7 and Jdk8
 * 
 * @author zhyhang
 *
 */
public class ConcurrentVsChronicle {

	public static void main(String[] args) throws Exception {
		// prepare random strings
		final int sampleNum = 1024 * 1024 * 2;
		final String[] samples = new String[sampleNum];
		ChronicleMapBuilder<String, String> builder = ChronicleMapBuilder.of(String.class, String.class);
		final String basePath = System.getProperty("java.io.tmpdir") + File.separator + "test";
		File cacheFile = new File(basePath + File.separator + "chromicleMap.data");
		final ConcurrentMap<String, String> cmap = builder.createPersistedTo(cacheFile);
		final int strLength = 72;
		for (int i = 0; i < samples.length; i++) {
			samples[i] = RandomStringUtils.randomAlphabetic(strLength);
		}
		final String constKey = "Hello World!";
		// start executors
		final int runingThread = 128;
		final AtomicInteger putCounter = new AtomicInteger(0);
		final AtomicInteger getCounter = new AtomicInteger(0);
		ExecutorService es = Executors.newFixedThreadPool(runingThread);
		long tsBegin = System.nanoTime();
		for (int i = 0; i < runingThread; i++) {
			es.execute(new Runnable() {

				@Override
				public void run() {
					for (int l = 0; l < sampleNum; l++) {
						// put
						int index;
						if ((index = putCounter.getAndIncrement()) < sampleNum) {
							cmap.put(samples[index], samples[index]);
						} else {
							putCounter.set(0);
						}
						// get
						if ((index = getCounter.getAndIncrement()) < sampleNum) {
							cmap.get(samples[index]);
							if (Math.random() < 0.382) { // remove 38.2%
								cmap.remove(samples[index]);
							}
						} else {
							cmap.get(constKey);
							getCounter.set(0);
						}
					}
				}
			});
		}
		es.shutdown();
		es.awaitTermination(1, TimeUnit.DAYS);
		long tsEnd = System.nanoTime();
		long costNs = tsEnd - tsBegin;
		System.out.format("Put/get [%d] strings to concurrent map size=[%d]. Iterate [%d] times.\n", sampleNum,
				cmap.size(), runingThread);
		System.out.format("Total time cost [%d] ns, i.e [%d] ms, [%d] s.\n", costNs,
				TimeUnit.NANOSECONDS.toMillis(costNs), TimeUnit.NANOSECONDS.toSeconds(costNs));
		System.exit(0);
	}

}
