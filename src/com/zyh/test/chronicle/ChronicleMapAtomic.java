/**
 * 
 */
package com.zyh.test.chronicle;

import java.io.File;
import java.util.concurrent.TimeUnit;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.lang.model.DataValueClasses;
import net.openhft.lang.values.LongValue;

/**
 * Atomically operating test
 * 
 * @author zhyhang
 *
 */
public class ChronicleMapAtomic {

	public static void main(String[] argv) throws Exception {
		// a openhft longvalue map
		File f = File.createTempFile("chronicle-map-atomic-test-longvalue.", ".data");
		Long key = Long.valueOf(19);
		ChronicleMap<Long, LongValue> map = ChronicleMapBuilder.of(Long.class, LongValue.class).entries(100)
				.lockTimeOut(500, TimeUnit.MILLISECONDS).createPersistedTo(f);
		map.put(key, map.newValueInstance());
		long v = map.get(key).getValue();
		map.get(key).addAtomicValue(5);
		map.close();
		map = ChronicleMapBuilder.of(Long.class, LongValue.class).entries(100).lockTimeOut(500, TimeUnit.MILLISECONDS)
				.createPersistedTo(f);
		System.out.format("longvalue[%d] after add atomic[%d]\n", v, map.get(key).getValue());
		map.close();

		// a aggregation of longvalue map
		f = File.createTempFile("chronicle-map-atomic-test-longvalues.", ".data");
		ChronicleMap<Long, StatsValues> map1 = ChronicleMapBuilder.of(Long.class, StatsValues.class).entries(30000)
				.lockTimeOut(500, TimeUnit.MILLISECONDS).createPersistedTo(f);
		StatsValues sv = DataValueClasses.newDirectInstance(StatsValues.class);
		sv.setStats1(DataValueClasses.newDirectInstance(LongValue.class));
		sv.setStats2(DataValueClasses.newDirectInstance(LongValue.class));
		map1.put(key, sv);
		StatsValues svu = map1.get(key);
		svu.getStats1().addAtomicValue(100);
		svu.getStats2().addAtomicValue(200);
		map1.close();
		map1 = ChronicleMapBuilder.of(Long.class, StatsValues.class).entries(30000)
				.lockTimeOut(500, TimeUnit.MILLISECONDS).createPersistedTo(f);
		StatsValues sv1 = map1.get(key);
		System.out.format("stats value[%d,%d] after add atomic[%d,%d]\n", sv.getStats1().getValue(),
				sv.getStats2().getValue(), sv1.getStats1().getValue(), sv1.getStats2().getValue());
		map1.close();
	}

	public static interface StatsValues {
		LongValue getStats1();

		void setStats1(LongValue v);

		LongValue getStats2();

		void setStats2(LongValue v);
	}

}
