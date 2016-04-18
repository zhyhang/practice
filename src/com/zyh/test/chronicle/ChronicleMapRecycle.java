/**
 * 
 */
package com.zyh.test.chronicle;

import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.chronicle.map.WriteContext;
import net.openhft.lang.model.DataValueClasses;
import net.openhft.lang.values.LongValue;

/**
 * chronicle map put remove put remove ... <br>
 * <b>1.non-replicated map call remove(key), will real remove the entry from map; size--; put new key will allow</b><br>
 * <b>2.replicated map call remove(key), will only flag the entry to deleted; size--; put new key possibly throw illegal state exception </b><br>
 * <b>3.when remove a key with map.keySet().iterator.next(), then put another key, will throw exception, because the "iterator next" always remove the entry in first position. 
 * should go on the iterator randomly get a remove key. </b></br>
 * @author zhyhang
 *
 */
public class ChronicleMapRecycle {

	private static final ThreadLocal<LongValue> THREAD_LOCAL_LONGVALUE = new ThreadLocal<LongValue>() {
		@Override
		protected LongValue initialValue() {
			return DataValueClasses.newDirectInstance(LongValue.class);
		}
	};

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int maxEntry = 10000;
		File f = Files.createTempFile("cmap-recycle-test", ".dat").toFile();
		// create map
		ChronicleMapBuilder<String, LongValue> mapBuilder = ChronicleMapBuilder.of(String.class, LongValue.class)
				.entries(maxEntry);
		ChronicleMap<String, LongValue> map = mapBuilder.createPersistedTo(f);
		for (int i = 0; i < 20 * maxEntry; i++) {
			try (WriteContext<String, LongValue> context = map.acquireUsingLocked(String.valueOf(i),
					THREAD_LOCAL_LONGVALUE.get())) {
				context.value().setValue(i);
			}
			if (i % 1000 == 0) {
				System.out.format("count[%d], max entry[%d],current size[%d]\n", i, maxEntry, map.longSize());
			}

			// remove by generated random key
			if (i >= maxEntry - 1) {
				while (true) {
					String removeKey = String.valueOf(ThreadLocalRandom.current().nextInt(i));
					if (null != map.remove(removeKey) || map.size() == 0) {
						break;
					}
				}
			}

			// remove using iterator, must go to random key, not only using "map.keySet().iterator().next"
//			if (i >= maxEntry - 1) {
//				String removeKey = null;
//				int randomPos = ThreadLocalRandom.current().nextInt(maxEntry);
//				Iterator<String> ksIterator = map.keySet().iterator();
//				while (randomPos-- >= 0 && ksIterator.hasNext()) {
//					removeKey = ksIterator.next();
//				}
//				map.remove(removeKey);
//			}

		}

		map.close();
	}

}
