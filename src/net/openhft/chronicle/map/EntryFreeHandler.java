/**
 * 
 */
package net.openhft.chronicle.map;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
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
 * Test replicated chronicle map Result:<br>
 * 1.remove key, without real free memory<br>
 * 2.zip and switch to new map, also replicated removed entries from another node<br>
 * 3.can hack code to free the removed (flag deleted) entries<br>
 * 4.all the node free meanwhile then zip, can real remove the entries and zip<br>
 * @author zhyhang
 *
 */
public class EntryFreeHandler {

	private static final Logger logger = LoggerFactory.getLogger(EntryFreeHandler.class);

	private static final ThreadLocal<LongValue> LOCAL_CACHE_LONGVALUE = ThreadLocal.withInitial(() -> {
		return DataValueClasses.newDirectInstance(LongValue.class);
	});
	private volatile static ReplicatedChronicleMap map;

	private static final int maxEntry = 10000;
	private static final int portRep = 9102;
	private static final String remoteHost = "192.168.144.55";
	private static final int putKeyNum = 20;

	private static final byte identifier = 10; // TODO
	private static final InetSocketAddress[] remotes = new InetSocketAddress[0]; // TODO
	// private static final InetSocketAddress[] remotes = new
	// InetSocketAddress[]{new InetSocketAddress(remoteHost,portRep)};
	private static final boolean getKeyRun = true;// TODO
	private static final int beforePutWaitSeconds = 20;// TODO
	private static final boolean initPutSomeValues = true;// TODO
	private static final int afterPutWaitSeconds = 20;// TODO
	private static final boolean freeDeletedEntries = true;// TODO
	private static final boolean zipMap = true;// TODO

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		map = createReplicatedMap(Files.createTempFile("cmap-entry-free-test.0.", ".dat").toString(), portRep);
		
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
			if (getKeyRun) {
				getKeys();
			}
			lookupInSeg(map, freeDeletedEntries);
		} , 3, 3, TimeUnit.SECONDS);
		
		if (initPutSomeValues) {
			TimeUnit.SECONDS.sleep(beforePutWaitSeconds);
			putValue(map, putKeyNum);
			TimeUnit.SECONDS.sleep(afterPutWaitSeconds);
			logger.info("step1-put-and-replicated");
			lookupInSeg(map, false);
			removeOddValue(map, putKeyNum);
			logger.info("step2-remove");
			lookupInSeg(map, false);
			TimeUnit.SECONDS.sleep(afterPutWaitSeconds);
			logger.info("step3-replicated-after-remove-free[{}]",freeDeletedEntries);
			lookupInSeg(map, false);
			if (zipMap) {
				zipMap();
				TimeUnit.SECONDS.sleep(afterPutWaitSeconds);
				logger.info("step4-zip-and-replicated");
				lookupInSeg(map, false);
			}
		}
		TimeUnit.MINUTES.sleep(60);
		map.close();
	}

	private static ReplicatedChronicleMap createReplicatedMap(String mapFile, int port) {
		try {
			File f = new File(mapFile);
			TcpTransportAndNetworkConfig tcpConfig = TcpTransportAndNetworkConfig.of(port, remotes)
					.heartBeatInterval(10L, TimeUnit.SECONDS).autoReconnectedUponDroppedConnection(true);
			SingleChronicleHashReplication rep = SingleChronicleHashReplication.builder()
					.tcpTransportAndNetwork(tcpConfig).createWithId(identifier);
			ChronicleMapBuilder<String, LongValue> mapBuilderRep = ChronicleMapBuilder.of(String.class, LongValue.class)
					.entries(maxEntry).replication(rep);
			ChronicleMap<String, LongValue> map = mapBuilderRep.createPersistedTo(f);
			return (ReplicatedChronicleMap) map;
		} catch (Exception e) {
			logger.error("createmap-error", e);
			return null;
		}
	}

	private static void lookupInSeg(ReplicatedChronicleMap map, boolean free) {
		logger.info("lookup-deleted begin, map size [{}].", map.size());
		for (Segment segment : map.segments) {
			lookupInSingleSeg(map, segment, free);
		}
		logger.info("lookup-deleted end, map size [{}].\n", map.size());
	}

	private static void lookupInSingleSeg(ReplicatedChronicleMap map, Segment segment, boolean free) {
		ThreadLocalCopies copies = SegmentState.getCopies(null);
		SegmentState segmentState = SegmentState.get(copies);
		MultiMap hashLookup = segment.hashLookup();
		SearchState searchState = segmentState.searchState;
		hashLookup.forEach((k, v) -> {
			hashLookup.startSearch(k, searchState);
			long pos = hashLookup.nextPos(searchState);
			if (pos == -1) {
				return;
			}
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
				if (deleted && free) {
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

	private static void zipMap() {
		try {
			ReplicatedChronicleMap newMap = createReplicatedMap(
					Files.createTempFile("cmap-entry-free-test.1.", ".dat").toString(), 0);
			newMap.putAll(map);
			ChronicleMap<String, LongValue> oldMap = map;
			map = newMap;
			logger.info("zipping-map switch temp map.");
			lookupInSeg(newMap, false);
			oldMap.close();
			// TODO should delete old file releasing memory
			map = createReplicatedMap(map.file().toString(), portRep);
			logger.info("zipping-map complete.");
		} catch (Exception e) {
			logger.error("zipping-map error", e);
		}
	}

	private static void getKeys() {
		logger.info("getKeys running...");
		for (int i = 0; i < 10; i++) {
			String key = String.valueOf(ThreadLocalRandom.current().nextInt(putKeyNum * 10));
			map.get(key);
		}
	}

}
