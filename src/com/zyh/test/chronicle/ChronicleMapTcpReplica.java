/**
 * 
 */
package com.zyh.test.chronicle;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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
public class ChronicleMapTcpReplica {
	private static final String KEY_INCREMENT = "key-increment";
	private static final long VALUE_INIT=90000000;
	private static String host;
	private static int port;
	private static byte identifier;
	private static String file;
	private static String persistFile;
	private static ChronicleMap<String, LongValue> map;
	private final static transient Logger logger = LoggerFactory.getLogger(ChronicleMapTcpReplica.class);

	public static void main(String[] argv) throws Exception {
		checkArguments(argv);
		init(argv);
		// replication between two server with different keys
		// diffKeyReplica();
		// test long add
		incr();

	}

	private static void checkArguments(String[] argv) {
		if (argv.length < 5 || argv[0].equals("-h") || argv[0].equals("--help")) {
			System.out.format("Usage: <remote host> <port> <local identifier> <persist file> <key-value file>\n");
			System.out.format("\tmaster-master pattern, using same port at two servers. \n");
			System.out.format("\tlocal identifier muster number(<128), distinct among server. \n");
			System.out.format("\tkey(string) value(long) splitter(tab) \n");
			System.exit(-1);
		}
	}

	private static void init(String[] argv) throws Exception {
		host = argv[0];
		port = Integer.parseInt(argv[1]);
		identifier = Byte.parseByte(argv[2]);
		persistFile = argv[3];
		file = argv[4];
		// create map
		File f = new File(persistFile);
		f.deleteOnExit();
		TcpTransportAndNetworkConfig tcpConfig = TcpTransportAndNetworkConfig
				.of(port, new InetSocketAddress(host, port)).heartBeatInterval(1L, TimeUnit.SECONDS)
				.autoReconnectedUponDroppedConnection(true);
		ChronicleMapBuilder<String, LongValue> mapBuilder = ChronicleMapBuilder.of(String.class, LongValue.class)
				.entries(100000L).replication(identifier, tcpConfig);
		map = mapBuilder.createPersistedTo(f);
		String key1=KEY_INCREMENT + "-1";
		String key2=KEY_INCREMENT + "-2";
		if (identifier == (byte) 1) { // only master node create the key
			LongValue value = map.newValueInstance();
			value.setValue(VALUE_INIT);
			map.put(key1, value);
			value=map.newValueInstance();
			value.setValue(VALUE_INIT);
			map.put(key2, value);
		}
		while(!map.containsKey(key1) || !map.containsKey(key2) ){
			TimeUnit.SECONDS.sleep(1);
		}
		logger.info("{}-{},{}-{}",key1,map.get(key1),key2,map.get(key2));
	}

	private static void printMap() throws IOException {
		// output
		try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(Paths.get(file + ".new")))) {
			map.forEach((k, v) -> {
				pw.println(k + "\t" + v.getValue());
			});
		}
		System.out.println("see the file " + file + ".new");
	}

	private static void end(long sleepSeconds) throws InterruptedException, IOException {
		// wait for other server replica
		TimeUnit.SECONDS.sleep(sleepSeconds);

		printMap();

		// close map
		map.close();

		System.exit(0);
	}

	private static void diffKeyReplica() throws Exception {
		Files.lines(Paths.get(file)).forEach(line -> {
			String[] kv = line.split("[\\s]+");
			LongValue value = DataValueClasses.newDirectInstance(LongValue.class);
			value.setValue(Long.parseLong(kv[1]));
			map.put(kv[0], value);
		});
		end(60);
	}

	private static void incr() throws Exception {
		ExecutorService es = Executors.newFixedThreadPool(10);
		int totalAdd=100000000;
		String key = KEY_INCREMENT + "-" + Byte.valueOf(identifier);
		AtomicLong top = new AtomicLong(totalAdd);
		logger.info("increasing-start, ts[{}]...",System.currentTimeMillis());
		for (int i = 0; i < 10; i++) {
			es.execute(() -> {
				while (top.getAndDecrement() > 0) {
					LongValue value = map.get(key);
					if(null!=value){
						value.addAtomicValue(1);
						synchronized (key) {
							map.update(key, value);// trigger replication
						}
					}
				}
			});
		}
		es.shutdown();
		es.awaitTermination(5, TimeUnit.MINUTES);
		logger.info("increasing-end, ts[{}].",System.currentTimeMillis());
		long ts=System.currentTimeMillis();
		AtomicBoolean isOver= new AtomicBoolean(false);
		while(!isOver.get() && System.currentTimeMillis() - ts < TimeUnit.SECONDS.toMillis(120)){
			isOver.set(true);;
			map.forEach((k,v)->{
				if(v.getValue()!=totalAdd+VALUE_INIT){
					isOver.set(false);
				}
			});
		}
		if(isOver.get()){
			end(1);
		}else{
			end(60); // wait 60 seconds again
		}
	}

}
