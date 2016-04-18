/**
 * 
 */
package com.zyh.test.chronicle;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.hash.replication.TcpTransportAndNetworkConfig;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.lang.model.DataValueClasses;
import net.openhft.lang.values.LongValue;

/**
 * try chronicle map tcp replication
 * 
 * @author zhyhang
 *
 */
public class ChronicleMapReplicatedRemove {
	private static ChronicleMap<String, LongValue> map;
	private final static transient Logger logger = LoggerFactory.getLogger(ChronicleMapReplicatedRemove.class);
	private static ThreadLocal<LongValue> cacheValue=ThreadLocal.withInitial(()->{return DataValueClasses.newDirectInstance(LongValue.class);});
	private static ScheduledExecutorService ses=Executors.newScheduledThreadPool(4);
	private static boolean runAsServer=false; 

	public static void main(String[] argv) throws Exception {
		checkArguments(argv);
		init(argv);
		TimeUnit.MINUTES.sleep(180);
	}

	private static void checkArguments(String[] argv) {
		if (argv.length < 4 || argv[0].equals("-h") || argv[0].equals("--help")) {
			System.out.format("Usage: <remote host> <port> <local identifier> <runAsServer>\n");
			System.out.format("\tmaster-master pattern, using same port at two servers. \n");
			System.out.format("\tlocal identifier muster number(<128), distinct among server. \n");
			System.out.format("\trun as server(close and rebuild) when true, else as client. \n");
			System.exit(-1);
		}
	}

	private static void init(String[] argv) throws Exception {
		String host = argv[0];
		int port = Integer.parseInt(argv[1]);
		byte identifier = Byte.parseByte(argv[2]);
		runAsServer=Boolean.valueOf(argv[3]);
		// create map
		long maxEntries=5000000;
		File f = Files.createTempFile("cmap-replicate-remove-test", ".dat").toFile();
		f.deleteOnExit();
		TcpTransportAndNetworkConfig tcpConfig = TcpTransportAndNetworkConfig
				.of(port, new InetSocketAddress(host, port)).heartBeatInterval(10L, TimeUnit.SECONDS)
				.autoReconnectedUponDroppedConnection(true);
		ChronicleMapBuilder<String, LongValue> mapBuilder = ChronicleMapBuilder.of(String.class, LongValue.class)
				.entries(maxEntries).replication(identifier, tcpConfig);
		map = mapBuilder.createPersistedTo(f);
		// schedule print info
		int flagNum=10;
		String flagPrefix="flag-key-";
		ses.scheduleAtFixedRate(()->{
			logger.info("map-info: size[{}], file at [{}].",map.longSize(),map.file().getName());
			for (int i = 0; i < flagNum; i++) {
				String key=flagPrefix.concat(String.valueOf(i));
				if(map.containsKey(key)){
					logger.info("map-info: exists key[{}].",key);
				}
			}
		}, 5, 5, TimeUnit.SECONDS);
		// fill entries to map
		LongValue v = cacheValue.get();
		v.setValue(0);
		// fill 10 flag keys to check if exists replicated from remote
		for (int i = 0; i < flagNum; i++) {
			map.put(flagPrefix.concat(String.valueOf(i)), v);
		}
		try {
			long l=0;
			while (l<maxEntries*2) {
				long rl = ThreadLocalRandom.current().nextLong();
				LongValue value = cacheValue.get();
				value.setValue(rl);
				map.put(String.valueOf(rl), value);
			}
		} catch (Exception e) {
			logger.info("init map exceed max entries", e);
		}
		// schedule put - remove 
		ses.scheduleAtFixedRate(ChronicleMapReplicatedRemove::putRemove, 3, 3, TimeUnit.SECONDS);
	}
	
	private static void putRemove(){
		int num=10;
		for (int i = 0; i < num; i++) {
			long rl = ThreadLocalRandom.current().nextLong();
			map.remove(String.valueOf(rl));
		}
		for (int i = 0; i < num; i++) {
			long rl = ThreadLocalRandom.current().nextLong();
			LongValue value = cacheValue.get();
			value.setValue(rl);
			try{
				map.put(String.valueOf(rl), value);
			} catch (Exception e) {
				logger.info("put-remove map exceed max entries", e);
			}
		}
		for (int i = 0; i < 10; i++) {
			String key="flag-key-".concat(String.valueOf(i));
			map.remove(key);
		}
	}

}
