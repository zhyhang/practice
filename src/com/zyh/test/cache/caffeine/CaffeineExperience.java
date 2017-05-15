/**
 * 
 */
package com.zyh.test.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheWriter;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;

/**
 * @author zhyhang
 *
 */
public class CaffeineExperience {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Cache<String, Long> cache = Caffeine.newBuilder()
				.maximumSize(64)
				.build();
		for (int i = 0; i < 128; i++) {
			cache.put(String.valueOf(i), Long.valueOf(i));
		}
		// not accurate size
		System.out.format("cache size: %d\n", cache.estimatedSize());
		// map view as a concurrentmap, map.computeIfAbsent() same as cache.get(k,mappingfuction)
		cache.asMap().computeIfAbsent("hello", k->new Long(1978));
		// iterate can only get the not evicted entries
		cache.asMap().entrySet().stream().forEach(e->{
			System.out.println(e.getKey()+"->"+e.getValue());
		});
		
		// another cache listening writer
		Cache<String, Long> listenedCache = Caffeine.newBuilder()
				.maximumSize(16)
				.writer(new CacheWriter<String,Long>() {

					@Override
					public void write(String key, Long value) {
						System.out.println("write entry: "+key+"->"+value);
					}

					@Override
					public void delete(String key, Long value, RemovalCause cause) {
						System.out.println("delete entry: "+key+"->"+value+", type: "+cause.name());
					}
				})
				.build();
		// put, trigger write()
		for (int i = 0; i < 16; i++) {
			listenedCache.put(String.valueOf(i), Long.valueOf(i));
		}
		// remove, trigger delete(), with type EXPLICIT
		listenedCache.invalidate(String.valueOf(4));
		// remove again, trigger nothing
		listenedCache.invalidate(String.valueOf(4));
		// remove, trigger delete(), with type EXPLICIT
		listenedCache.asMap().remove(String.valueOf(13));
		// put-replace, only trigger write(), not trigger delete
		listenedCache.put(String.valueOf("1"), 2017L);
		// put-replace, only trigger write(), not trigger delete
		listenedCache.asMap().put(String.valueOf("3"), 2018L);
		// replace, only trigger write(), not trigger delete
		listenedCache.asMap().replace(String.valueOf("5"), 2019L);
		if(!listenedCache.asMap().containsKey(String.valueOf(13))){
			System.out.println("compute 13");
			//compute, load, not trigger the write and delete
			listenedCache.get(String.valueOf(13), key->Long.valueOf(2020L));
			listenedCache.invalidate(String.valueOf(13));
			listenedCache.asMap().computeIfAbsent(String.valueOf(13), key->Long.valueOf(2020L));
		}
		// put more exceed the max num trigger eviction then delete(), with type SIZE
		for (int i = 16; i < 24; i++) {
			listenedCache.put(String.valueOf(i), Long.valueOf(i));
		}
	}

}
