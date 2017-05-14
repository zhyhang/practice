package com.zyh.test.cache.ehcache;

import static java.net.URI.create;
import static org.ehcache.clustered.client.config.builders.ClusteredResourcePoolBuilder.clusteredDedicated;
import static org.ehcache.clustered.client.config.builders.ClusteringServiceConfigurationBuilder.cluster;
import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder;
import static org.ehcache.config.builders.ResourcePoolsBuilder.heap;
import static org.ehcache.config.units.MemoryUnit.MB;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.clustered.client.config.builders.ClusteredStoreConfigurationBuilder;
import org.ehcache.clustered.common.Consistency;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.slf4j.Logger;

import net.sf.ehcache.Element;

public class ClusteredEhCache {

	private static final Logger LOGGER = getLogger(ClusteredEhCache.class);

	public static void main(String[] args) {
		LOGGER.info("Creating clustered cache manager");
		final URI uri = create("terracotta://192.168.144.58:9520/clustered");
		try (CacheManager cacheManager = newCacheManagerBuilder()
				.with(cluster(uri).autoCreate().defaultServerResource("default-resource"))
				.withCache("basicCache",
						newCacheConfigurationBuilder(Long.class, String.class,
								heap(100).offheap(1, MB).with(clusteredDedicated(5, MB)))
										.add(ClusteredStoreConfigurationBuilder.withConsistency(Consistency.EVENTUAL)))
				.build(true); CacheManager cacheManagerl = newCacheManagerBuilder().build(true)) {
			// clustered ehcache3.0
			Cache<Long, String> ccache3 = cacheManager.getCache("basicCache", Long.class, String.class);
			// chm
			ConcurrentMap<Long, String> cm = new ConcurrentHashMap<>();
			// standalone ehcache3.0
			Cache<Long, String> lcache3 = cacheManagerl.createCache("myCache",
					newCacheConfigurationBuilder(Long.class, String.class, ResourcePoolsBuilder.heap(100)).build());
			// standalone ehcache2.0
			final net.sf.ehcache.CacheManager cacheManager2 = new net.sf.ehcache.CacheManager();
			cacheManager2.addCache("hello-world");
			final net.sf.ehcache.Cache lcache2 = cacheManager2.getCache("hello-world");
			lcache2.put(new Element(1L, "Hello, World."));
			lcache2.put(new Element(2L, "Hello, World.."));
			lcache2.put(new Element(3L, "Hello, World.."));

			LOGGER.info("Putting to cache");
			ccache3.put(1L, "no.1 value!");
			ccache3.put(2L, "no.2 value!");
			ccache3.put(3L, "no.3 value!");

			cm.put(1L, "no1 value!");
			cm.put(2L, "no2 value!");
			cm.put(3L, "no3 value!");

			lcache3.put(1L, "no1 value!");
			lcache3.put(2L, "no2 value!");
			lcache3.put(3L, "no3 value!");

			LOGGER.info("Getting from cache");
			LOGGER.info(ccache3.get(1L));
			LOGGER.info(ccache3.get(2L));
			LOGGER.info(ccache3.get(3L));

			LOGGER.info("begin warmup ccache3...");
			for (int i = 0; i < 1; i++) {
				ccache3.get(3L);
			}
			LOGGER.info("end warmup ccache3.");
			long tsb = System.currentTimeMillis();
			for (int i = 0; i < 10000000; i++) {
				ccache3.get(3L);
				ccache3.get(2L);
				ccache3.get(1L);
			}
			long tse = System.currentTimeMillis();
			LOGGER.info("Time cost clustered ehc3 (ms): " + (tse - tsb));

			for (int i = 0; i < 100000; i++) {
				cm.get(1L);
			}
			tsb = System.currentTimeMillis();
			for (int i = 0; i < 10000000; i++) {
				cm.get(1L);
				cm.get(2L);
				cm.get(3L);
			}
			tse = System.currentTimeMillis();
			LOGGER.info("Time cost concurrentmap (ms): " + (tse - tsb));

			for (int i = 0; i < 100000; i++) {
				lcache3.get(1L);
			}
			tsb = System.currentTimeMillis();
			for (int i = 0; i < 10000000; i++) {
				lcache3.get(1L);
				lcache3.get(2L);
				lcache3.get(3L);
			}
			tse = System.currentTimeMillis();
			LOGGER.info("Time cost local ehc3 (ms): " + (tse - tsb));

			for (int i = 0; i < 100000; i++) {
				lcache2.get(1L);
			}
			tsb = System.currentTimeMillis();
			for (int i = 0; i < 10000000; i++) {
				lcache2.get(1L);
				lcache2.get(2L);
				lcache2.get(3L);
			}
			tse = System.currentTimeMillis();
			LOGGER.info("Time cost local ehc2 (ms): " + (tse - tsb));

			LOGGER.info("Closing cache manager");

		} catch (Exception e) {
			e.printStackTrace();
		}

		LOGGER.info("Exiting");

		System.exit(0);
	}
}
