/**
 * 
 */
package net.openhft.chronicle.map;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import net.openhft.chronicle.hash.replication.SingleChronicleHashReplication;
import net.openhft.chronicle.hash.replication.TcpTransportAndNetworkConfig;
import net.openhft.chronicle.map.VanillaChronicleMap.Segment;
import net.openhft.chronicle.map.VanillaChronicleMap.SegmentState;
import net.openhft.lang.io.MultiStoreBytes;
import net.openhft.lang.model.DataValueClasses;
import net.openhft.lang.threadlocal.ThreadLocalCopies;
import net.openhft.lang.values.LongValue;

/**
 * @author zhyhang
 *
 */
public class EntryFreeHandler {

	private static final ThreadLocal<LongValue> LOCAL_CACHE_LONGVALUE = ThreadLocal.withInitial(() -> {
		return DataValueClasses.newDirectInstance(LongValue.class);
	});

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int maxEntry = 10000;
		File f = Files.createTempFile("cmap-entry-free-test.0.", ".dat").toFile();
		TcpTransportAndNetworkConfig tcpConfig = TcpTransportAndNetworkConfig
				.of(0, new InetSocketAddress("10.1.1.3", 7802)).heartBeatInterval(10L, TimeUnit.SECONDS)
				.autoReconnectedUponDroppedConnection(true);
		SingleChronicleHashReplication rep = SingleChronicleHashReplication.builder().bootstrapOnlyLocalEntries(true)
				.tcpTransportAndNetwork(tcpConfig).createWithId((byte) 10);
		ChronicleMapBuilder<String, LongValue> mapBuilderRep = ChronicleMapBuilder.of(String.class, LongValue.class)
				.entries(maxEntry).replication(rep);
		ChronicleMap<String, LongValue> map = mapBuilderRep.createPersistedTo(f);
		ReplicatedChronicleMap mapRep = (ReplicatedChronicleMap) map;
		putValue(mapRep, 20);
		removeOddValue(mapRep, 20);
		lookupInSeg(mapRep);
		lookupInSeg(mapRep);
		putValue(mapRep, 20);
		lookupInSeg(mapRep);
		mapRep.close();
	}

	private static void lookupInSeg(ReplicatedChronicleMap map) {
		for (Segment segment : map.segments) {
			// 判断一个segment是否有size，没有size不遍历？也不对，这个segment都被delete了，size=0，就更应该清除，这样会不会引起将文件全部读入内存？
			lookupInSingleSeg(map, segment);
		}
		System.out.format("map size [%d].\n", map.size());
	}

	private static void lookupInSingleSeg(ReplicatedChronicleMap map, Segment segment) {
		ThreadLocalCopies copies = SegmentState.getCopies(null);
		SegmentState segmentState = SegmentState.get(copies);
		MultiMap hashLookup = segment.hashLookup();
		SearchState searchState = segmentState.searchState;
		hashLookup.forEach((k, v) -> {
			hashLookup.startSearch(k, searchState);
			long pos = hashLookup.nextPos(searchState);
			segment.readLock(null);
			try {
				MultiStoreBytes entry = segment.reuse(segmentState.tmpBytes, segment.offsetFromPos(pos));
				long keySize = map.keySizeMarshaller.readSize(entry);
				boolean deleted = segment.isDeleted(entry, keySize);
				long valueSize = map.valueSizeMarshaller.readSize(entry);
				Entry readEntry = segment.getEntry(segmentState, pos);
				System.out.format("lookup entry <%s,%d>, deleted? [%s].\n", readEntry.getKey(),
						((LongValue) readEntry.getValue()).getValue(), String.valueOf(deleted));
				// really remove
				if (deleted) {
					hashLookup.removePrevPos(searchState);
					//entry.positionAddr() + valueSize - entry.startAddr();
					long entrySizeInBytes = segment.entrySize(keySize, valueSize);
					Method method = Segment.class.getDeclaredMethod("free", new Class[] { long.class, int.class });
					method.setAccessible(true);
					method.invoke(segment, new Object[] { pos, segment.inChunks(entrySizeInBytes) });
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				segment.readUnlock();
			}
		});
	}

	private static void putValue(ChronicleMap<String, LongValue> map, int size) {
		for (int i = 0; i < size; i++) {
			LongValue v = LOCAL_CACHE_LONGVALUE.get();
			v.setValue(i);
			map.put(String.valueOf(i), v);
		}
	}

	private static void removeOddValue(ChronicleMap<String, LongValue> map, int size) {
		for (int i = 0; i < size; i++) {
			if (i % 2 == 1) {
				map.remove(String.valueOf(i));
			}
		}
	}

}
