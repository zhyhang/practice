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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory.getLogger(EntryFreeHandler.class);

	private static final ThreadLocal<LongValue> LOCAL_CACHE_LONGVALUE = ThreadLocal.withInitial(() -> {
		return DataValueClasses.newDirectInstance(LongValue.class);
	});

	private static final int maxEntry = 10000;
	private static final int port = 8102;
	private static final String remoteHost = "192.168.144.58";
	private static final InetSocketAddress[] remotes = new InetSocketAddress[0];
	// private static final InetSocketAddress[] remotes = new
	// InetSocketAddress[]{new InetSocketAddress(remoteHost,port)};
	private static ReplicatedChronicleMap map;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		map = createReplicatedMap(Files.createTempFile("cmap-entry-free-test.0.", ".dat").toString());
		putValue(map, 20);
		removeOddValue(map, 20);
		lookupInSeg(map);
		lookupInSeg(map);
		putValue(map, 20);
		lookupInSeg(map);
		map.close();
	}

	private static ReplicatedChronicleMap createReplicatedMap(String mapFile) {
		try {
			File f = new File(mapFile);
			TcpTransportAndNetworkConfig tcpConfig = TcpTransportAndNetworkConfig.of(port, remotes)
					.heartBeatInterval(10L, TimeUnit.SECONDS).autoReconnectedUponDroppedConnection(true);
			SingleChronicleHashReplication rep = SingleChronicleHashReplication.builder()
					.bootstrapOnlyLocalEntries(true).tcpTransportAndNetwork(tcpConfig).createWithId((byte) 10);
			ChronicleMapBuilder<String, LongValue> mapBuilderRep = ChronicleMapBuilder.of(String.class, LongValue.class)
					.entries(maxEntry).replication(rep);
			ChronicleMap<String, LongValue> map = mapBuilderRep.createPersistedTo(f);
			return (ReplicatedChronicleMap) map;
		} catch (Exception e) {
			logger.error("createmap-error", e);
			return null;
		}
	}

	private static ChronicleMap<String, LongValue> createPersistentMap() {
		try {
			ChronicleMapBuilder<String, LongValue> mapBuilderRep = ChronicleMapBuilder.of(String.class, LongValue.class)
					.entries(maxEntry);
			ChronicleMap<String, LongValue> map = mapBuilderRep
					.createPersistedTo(Files.createTempFile("cmap-entry-free-test.1.", ".dat").toFile());
			return map;
		} catch (Exception e) {
			logger.error("createmap-error", e);
			return null;
		}
	}

	private static void lookupInSeg(ReplicatedChronicleMap map) {
		for (Segment segment : map.segments) {
			// 判断一个segment是否有size，没有size不遍历？也不对，这个segment都被delete了，size=0，就更应该清除，这样会不会引起将文件全部读入内存？
			lookupInSingleSeg(map, segment);
		}
		logger.info("map size [{}].\n", map.size());
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
				logger.info("lookup entry <{},{}>, deleted? [{}].", readEntry.getKey(),
						((LongValue) readEntry.getValue()).getValue(), String.valueOf(deleted));
				// really remove
				if (deleted) {
					hashLookup.removePrevPos(searchState);
					// entry.positionAddr() + valueSize - entry.startAddr();
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

	private static ReplicatedChronicleMap zipMap(ChronicleMap<String, LongValue> oldMap) {
		ChronicleMap<String, LongValue> newMap = createPersistentMap();
		newMap.putAll(oldMap);
		String newFile = newMap.file().toString();
		newMap.close();
		oldMap.close();
		ReplicatedChronicleMap newMapRep = (ReplicatedChronicleMap) createReplicatedMap(newFile);
		logger.info("zipping-map complete.");
		return newMapRep;
	}

}
