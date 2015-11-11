/**
 * 
 */
package com.zyh.test.chronicle;

import java.io.File;
import java.util.concurrent.TimeUnit;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.lang.values.LongValue;

/**
 * performance test( get/update)
 * 
 * @author zhyhang
 *
 */
public class ChronicleMapPeform {

	public static void main(String[] argv) throws Exception {
		// a openhft longvalue map
		File f = File.createTempFile("chronicle-map-atomic-test-longvalue.", ".data");
		f.deleteOnExit();
		int size = 2000000;
		ChronicleMap<String, LongValue> map = ChronicleMapBuilder.of(String.class, LongValue.class).entries(size)
				.lockTimeOut(500, TimeUnit.MILLISECONDS).createPersistedTo(f);
		// fill map
		for (int i = 0; i < size; i++) {
			LongValue value = map.newValueInstance();
			value.setValue(i);
			map.put("k".concat(String.valueOf(i)), value);
		}
		map.close();
		// re-open
		ChronicleMap<String, LongValue> map1 = ChronicleMapBuilder.of(String.class, LongValue.class).entries(size)
				.lockTimeOut(500, TimeUnit.MILLISECONDS).createPersistedTo(f);
		// iterate
		long tsb = System.currentTimeMillis();
		int times = 10;
		for (int i = 0; i < times; i++) {
			map1.forEach((k, v) -> {
				map1.update(k, v);
			});
		}
		System.out.format("map size[%d], iterate count[%d], update time cost[%d]s. ", size, times * size,
				TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - tsb));
		map1.close();
	}

}
