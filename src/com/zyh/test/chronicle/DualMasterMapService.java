/**
 * 
 */
package com.zyh.test.chronicle;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpServer;
import com.zyh.test.utils.JdkHttpServerUtil;

import net.openhft.chronicle.hash.replication.TcpTransportAndNetworkConfig;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

/**
 * double chronicle map replication, receiving remote put data<br>
 * 
 * <pre>
 * run the service will start a http server listening at port 3128.<br>
 * add value to spec key:  /add?s=key1_addtotal_addtoday,key2..., e.g. /add?s=k1_2_2,k2_3_3<br>
 * print map: /print<br>
 * stop the server: /stop
 * </pre>
 * 
 * @author zhyhang
 *
 */
@SuppressWarnings("restriction")
public class DualMasterMapService {

	private final static String STORAGE_FILE = DualMasterMapService.class.getSimpleName() + "-cmap.dat";

	private final static String OUTPUT_FILE = DualMasterMapService.class.getSimpleName() + "-cmap-entry.txt";

	private final static transient Logger LOGGER = LoggerFactory.getLogger(DualMasterMapService.class);

	private ChronicleMap<String, AdThreshold> map;

	private final ConcurrentMap<String, AtomicLong[]> benchMap = new ConcurrentHashMap<>();

	private final AtomicLong counter = new AtomicLong(0);

	private final AtomicLong[] timeCost = new AtomicLong[] { new AtomicLong(0), new AtomicLong(0) };

	private final ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

	private DualMasterMapService(String remoteHost, int port, byte identifier) {
		try {
			File f = new File(STORAGE_FILE);
			if (f.exists()) {
				f.delete();
			}
			f.deleteOnExit();
			TcpTransportAndNetworkConfig tcpConfig = TcpTransportAndNetworkConfig
					.of(port, new InetSocketAddress(remoteHost, port)).heartBeatInterval(2L, TimeUnit.SECONDS)
					.autoReconnectedUponDroppedConnection(true);
			ChronicleMapBuilder<String, AdThreshold> mapBuilder = ChronicleMapBuilder
					.of(String.class, AdThreshold.class).entries(3000000L).replication(identifier, tcpConfig);
			map = mapBuilder.createPersistedTo(f);
		} catch (Exception e) {
			LOGGER.error("create map error:", e);
		}
		ses.scheduleAtFixedRate(this::printMap, 2, 10, TimeUnit.MINUTES);
	}

	private void addThreshold(Map<String, AdThreshold> deltaThrhds) {
		if (null == deltaThrhds) {
			return;
		}
		counter.incrementAndGet();
		long tsb = System.nanoTime();
		deltaThrhds.entrySet().parallelStream().forEach(this::addEntryValue);
		timeCost[0].addAndGet(System.nanoTime() - tsb);
		tsb = System.nanoTime();
		deltaThrhds.entrySet().parallelStream().forEach(this::addBenchEntryValue);
		timeCost[1].addAndGet(System.nanoTime() - tsb);
	}

	private void addEntryValue(Map.Entry<String, AdThreshold> e) {
		addMapValue(e.getKey(), e.getValue());
	}

	private void addBenchEntryValue(Map.Entry<String, AdThreshold> e) {
		addBenchMapValue(e.getKey(), e.getValue());
	}

	private void addMapValue(String id, AdThreshold delta) {
		try {
			AdThreshold threshold = map.computeIfAbsent(id, k -> map.newValueInstance());
			threshold.addAtomicTodayCost(delta.getTodayCost());
			threshold.addAtomicTotalCost(delta.getTotalCost());
			synchronized (id) {
				map.update(id, threshold); // trigger replication
			}
		} catch (Exception e) {
			LOGGER.error("chronicle-map add error:", e);
		}
	}

	private void addBenchMapValue(String id, AdThreshold delta) {
		AtomicLong[] threshold = benchMap.computeIfAbsent(id,
				k -> new AtomicLong[] { new AtomicLong(0), new AtomicLong(0) });
		threshold[0].addAndGet(delta.getTotalCost());
		threshold[1].addAndGet(delta.getTodayCost());
	}

	private void printMap() {
		try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(Paths.get(OUTPUT_FILE)))) {
			pw.format("chronicle-map: size[%d], push counter[%d], timecost[%d]ms.\n", map.size(), counter.get(),
					TimeUnit.NANOSECONDS.toMillis(timeCost[0].get()));
			pw.format("bench-map: size[%d], push counter[%d], timecost[%d]ms.\n", benchMap.size(), counter.get(),
					TimeUnit.NANOSECONDS.toMillis(timeCost[1].get()));
			map.forEach((k, v) -> {
				AtomicLong[] bvalues = benchMap.get(k);
				bvalues = bvalues == null ? new AtomicLong[] { new AtomicLong(0), new AtomicLong(0) } : bvalues;
				pw.format("%s[%d(%d),%d(%d)]\n", k, v.getTotalCost(), bvalues[0].get(), v.getTodayCost(),
						bvalues[1].get());
			});
		} catch (Exception e) {
			LOGGER.error("print map error:", e);
		}
	}

	private void handleAdd(com.sun.net.httpserver.HttpExchange xchg) {
		try {
			String idDeltasStr = JdkHttpServerUtil.queryToMap(xchg.getRequestURI().getQuery()).get("s");
			Map<String, AdThreshold> deltaMap = new HashMap<>();
			String[] idDeltas = idDeltasStr == null ? new String[0] : idDeltasStr.split(",");
			for (String idDelta : idDeltas) {
				String[] splitIdDelta = idDelta.split("_");
				AdThreshold threshold = new AdThresholdImp();// must not use map.newValueInstance()
				threshold.setTotalCost(Long.parseLong(splitIdDelta[1]));
				threshold.setTodayCost(Long.parseLong(splitIdDelta[2]));
				deltaMap.put(splitIdDelta[0], threshold);
			}
			addThreshold(deltaMap);
			xchg.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
			xchg.getResponseBody().close();
		} catch (Exception e) {
			LOGGER.error("handle-add error:", e);
		}
	}

	private void handlePrint(com.sun.net.httpserver.HttpExchange xchg) {
		try {
			ses.execute(this::printMap);
			String output = "Please see the file: " + Paths.get(OUTPUT_FILE).toFile().getCanonicalPath();
			xchg.sendResponseHeaders(HttpURLConnection.HTTP_OK, output.length());
			OutputStream os = xchg.getResponseBody();
			os.write(output.getBytes());
			os.close();
		} catch (Exception e) {
			LOGGER.error("handle-print error:", e);
		}
	}

	private void handleStop(com.sun.net.httpserver.HttpExchange xchg) {
		try {
			ses.execute(this::printMap);
			ses.shutdown();
			ses.awaitTermination(5, TimeUnit.MINUTES);
			byte[] output = "service has stopped".getBytes();
			xchg.sendResponseHeaders(HttpURLConnection.HTTP_OK, output.length);
			OutputStream os = xchg.getResponseBody();
			os.write(output);
			os.close();
			System.exit(0);
		} catch (Exception e) {
			LOGGER.error("handle-stop error:", e);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		checkArguments(args);
		String remoteHost = args[0];
		int port = Integer.parseInt(args[1]);
		byte identifier = Byte.parseByte(args[2]);
		DualMasterMapService service = new DualMasterMapService(remoteHost, port, identifier);
		// start http server
		int httpPort = 3128;
		LOGGER.info("http server startup at port {} ...", httpPort);
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(httpPort), 0);
			server.createContext("/add", service::handleAdd);
			server.createContext("/print", service::handlePrint);
			server.createContext("/stop", service::handleStop);
			server.setExecutor(Executors.newFixedThreadPool(20));
			server.start();
			LOGGER.info("http server started, ok.");
		} catch (IOException e) {
			LOGGER.error("http server startup error:", e);
		}
	}

	private static void checkArguments(String[] args) {
		if (args.length < 3 || args[0].equals("-h") || args[0].equals("--help")) {
			System.out.format("Usage: <remote host> <port> <local identifier>\n");
			System.out.format("\tmaster-master pattern, using same port at two servers. \n");
			System.out.format("\tlocal identifier muster number(<128), distinct among servers. \n");
			System.exit(-1);
		}
	}

	public static interface AdThreshold {
		long getTotalCost();

		void setTotalCost(long value);

		long addAtomicTotalCost(long delta);

		long getTodayCost();

		void setTodayCost(long value);

		long addAtomicTodayCost(long delta);
	}
	
	public static class AdThresholdImp implements AdThreshold {
		
		private long total;
		
		private long today;

		@Override
		public long getTotalCost() {
			return total;
		}

		@Override
		public void setTotalCost(long value) {
			this.total=value;
		}

		@Override
		public long addAtomicTotalCost(long delta) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long getTodayCost() {
			return today;
		}

		@Override
		public void setTodayCost(long value) {
			this.today=value;
		}

		@Override
		public long addAtomicTodayCost(long delta) {
			throw new UnsupportedOperationException();
		}
	}

}
