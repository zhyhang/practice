/**
 * 
 */
package com.zyh.test.cache.ehcache;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.slf4j.LoggerFactory;

/**
 * @author zhyhang
 *
 */
public class Level3StoreTier {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try (PersistentCacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.with(CacheManagerBuilder
						.persistence("/data/cache/eh/" + Level3StoreTier.class.getSimpleName() + ".dat"))
				.withCache("pcache",
						CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
								ResourcePoolsBuilder.newResourcePoolsBuilder().heap(1024, EntryUnit.ENTRIES)
										.offheap(8, MemoryUnit.MB).disk(2, MemoryUnit.GB, true)))
				.withCache("pcache1",
						CacheConfigurationBuilder
								.newCacheConfigurationBuilder(Long.class, StoreValue.class,
										ResourcePoolsBuilder.newResourcePoolsBuilder().heap(1024, EntryUnit.ENTRIES)
												.offheap(8, MemoryUnit.MB).disk(2, MemoryUnit.GB, true)))
				.build(true);) {
			Cache<Long, String> pcache = persistentCacheManager.getCache("pcache", Long.class, String.class);
			Cache<Long, StoreValue> pcache1 = persistentCacheManager.getCache("pcache1", Long.class, StoreValue.class);
			Cache<Long, String> pcache2 = persistentCacheManager.createCache("pcache2",
					CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
							ResourcePoolsBuilder.newResourcePoolsBuilder().heap(1024, EntryUnit.ENTRIES)
									.offheap(8, MemoryUnit.MB).disk(2, MemoryUnit.GB, true))
							.build());
			int totalSize = 100_000;
			// pcache.clear();
			// pcache1.clear();

			String value = "Ehcache is an open source, standards-based cache that boosts performance, offloads your database, and simplifies scalability. It's the most widely-used Java-based cache because it's robust, proven, full-featured, and integrates with other popular libraries and frameworks. Ehcache scales from in-process caching, all the way to mixed in-process/out-of-process deployments with terabyte-sized caches.";
			for (int i = 0; i < totalSize; i++) {
				// pcache1.put(Long.valueOf(i), new StoreValue(i, "zyh",
				// Arrays.asList(Byte.valueOf("01"),Byte.valueOf("02"),Byte.valueOf("03"))));
				// pcache2.put(Long.valueOf(i), value);
			}
			AtomicLong csize = new AtomicLong(0);
			LoggerFactory.getLogger(Level3StoreTier.class).info("begin cache read.");
			pcache2.forEach(e -> {
				csize.incrementAndGet();
			});
			LoggerFactory.getLogger(Level3StoreTier.class).info("cache size: {}", csize.get());
		}
	}

}

class StoreValue implements Serializable {

	StoreValue(long no, String name, List<Byte> fingerPrint) {
		super();
		this.no = no;
		this.name = name;
		this.fingerPrint = fingerPrint;
	}

	private long no;

	private String name;

	private List<Byte> fingerPrint;
}
