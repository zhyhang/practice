/**
 * 
 */
package com.zyh.test.cache.redis;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import redis.clients.jedis.Jedis;

/**
 * @author zhyhang
 *
 */
public class HyperLogLogMem {

	private static final LinkedTransferQueue<Jedis> POOL = new LinkedTransferQueue<>();

	static {
		for (int i = 0; i < 24; i++) {
			POOL.add(new Jedis("192.168.152.188", 6381));
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IntStream.range(0, 1000000).unordered().parallel().forEach(HyperLogLogMem::pfaddKey);
	}

	private static void pfaddKey(int ki) {
		String hllKey = "hll_key_" + ki;
		Jedis jedisc = null;
		try {
			final Jedis jedis = POOL.poll(1, TimeUnit.SECONDS);
			jedisc = jedis;
			String[] elements = IntStream.range(0, 10000).mapToObj(i -> {
				return String.valueOf(i + "-thisisatestvalue-" + ThreadLocalRandom.current().nextLong());
			}).toArray(String[]::new);
			jedis.pfadd(hllKey, elements);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedisc != null) {
				POOL.put(jedisc);
			}
		}
	}

}
