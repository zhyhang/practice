/**
 * 
 */
package com.zyh.test.chronicle;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import net.openhft.chronicle.hash.replication.TcpTransportAndNetworkConfig;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.chronicle.map.WriteContext;
import net.openhft.lang.model.DataValueClasses;
import net.openhft.lang.values.LongValue;

/**
 * chronicle map put remove put remove ... <br>
 * <b>1.non-replicated map call remove(key), will real remove the entry from
 * map; size--; put new key will allow</b><br>
 * <b>2.replicated map call remove(key), will only flag the entry to deleted;
 * size--; put new key possibly throw illegal state exception </b><br>
 * <b>3.when remove a key with map.keySet().iterator.next(), then put another
 * key, will throw exception, because the "iterator next" always remove the
 * entry in first position. should go on the iterator randomly get a remove
 * key. </b></br>
 * 
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

		/**
		 * replicated map, remove(key) will do nothing but flag the entry is
		 * deleted, i.e. will not release the memory space.
		 * 
		 * <pre>
		 * TcpTransportAndNetworkConfig tcpConfig = TcpTransportAndNetworkConfig
		 * 		.of(7802, new InetSocketAddress("10.1.1.3", 7802)).heartBeatInterval(10L, TimeUnit.SECONDS)
		 * 		.autoReconnectedUponDroppedConnection(true);
		 * ChronicleMapBuilder<String, LongValue> mapBuilder = ChronicleMapBuilder.of(String.class, LongValue.class)
		 * 		.entries(maxEntry).replication((byte) 10, tcpConfig);
		 * </pre>
		 */

		TcpTransportAndNetworkConfig tcpConfig = TcpTransportAndNetworkConfig
				.of(7802, new InetSocketAddress("10.1.1.3", 7802)).heartBeatInterval(10L, TimeUnit.SECONDS)
				.autoReconnectedUponDroppedConnection(true);
		ChronicleMapBuilder<String, LongValue> mapBuilderRep = ChronicleMapBuilder.of(String.class, LongValue.class)
				.entries(maxEntry).replication((byte) 10, tcpConfig);

		// create non-replicated map then replicated will normally remove key
		ChronicleMapBuilder<String, LongValue> mapBuilder = ChronicleMapBuilder.of(String.class, LongValue.class)
				.entries(maxEntry);
		ChronicleMap<String, LongValue> map = mapBuilder.createPersistedTo(f);

		ChronicleMap<String, LongValue> mapRep = mapBuilderRep.createPersistedTo(f);
		for (int i = 0; i < 20 * maxEntry; i++) {
			try (WriteContext<String, LongValue> context = mapRep.acquireUsingLocked(String.valueOf(i),
					THREAD_LOCAL_LONGVALUE.get())) {
				context.value().setValue(i);
			}
			if (i % 1000 == 0) {
				System.out.format("count[%d], max entry[%d],current size[%d]\n", i, maxEntry, mapRep.longSize());
			}

			// remove by generated random key
			if (i >= maxEntry - 1) {
				while (true) {
					String removeKey = String.valueOf(ThreadLocalRandom.current().nextInt(i));
					if (null != mapRep.remove(removeKey) || mapRep.size() == 0) {
						break;
					}
				}
			}

			// remove using iterator, must go to random key, not only using
			// "map.keySet().iterator().next"
			// if (i >= maxEntry - 1) {
			// String removeKey = null;
			// int randomPos = ThreadLocalRandom.current().nextInt(maxEntry);
			// Iterator<String> ksIterator = map.keySet().iterator();
			// while (randomPos-- >= 0 && ksIterator.hasNext()) {
			// removeKey = ksIterator.next();
			// }
			// map.remove(removeKey);
			// }

		}

		map.close();
		mapRep.close();
	}

}
