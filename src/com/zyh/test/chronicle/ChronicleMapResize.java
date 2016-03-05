/**
 * 
 */
package com.zyh.test.chronicle;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import net.openhft.chronicle.hash.replication.TcpTransportAndNetworkConfig;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.chronicle.map.WriteContext;
import net.openhft.lang.model.DataValueClasses;
import net.openhft.lang.values.LongValue;

/**
 * Test can resize on persistent map.<br>
 * <b>Answer:</b>
 * persistent to a file with max entry 100w, cannot resize to 200w, although restart with 200w max entry config.<br>
 * <b>But,</b> alternately, can create two tcp replicated map, <br>
 * first is max 100w with already persistent data, <br>
 * second replicate from first with 200w max entry config init from empty persistent file.<br> 
 * 
 * @author zhyhang
 *
 */
public class ChronicleMapResize {

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
		int maxEntry = 20000;// 50000;//20000;
		int localport = 8102;
		int peerport = 9102;
		String localfile = "/data/temp/cmap-resize-test.dat";// "d:/temp/cmap-resize-test-1.dat";
		byte identifier = 10;// 20
		// create map
		File f = new File(localfile);
		TcpTransportAndNetworkConfig tcpConfig = TcpTransportAndNetworkConfig
				.of(localport, new InetSocketAddress("192.168.0.100", peerport)).heartBeatInterval(1L, TimeUnit.SECONDS)
				.autoReconnectedUponDroppedConnection(true);
		ChronicleMapBuilder<String, LongValue> mapBuilder = ChronicleMapBuilder.of(String.class, LongValue.class)
				.entries(maxEntry).replication(identifier, tcpConfig);
		ChronicleMap<String, LongValue> map = mapBuilder.createPersistedTo(f);
		for (int i = 0; i < 4 * maxEntry; i++) {
			try (WriteContext<String, LongValue> context = map.acquireUsingLocked(String.valueOf(i),
					THREAD_LOCAL_LONGVALUE.get())) {
				context.value().setValue(i);
			}
		}
		System.out.format("max entry[%d],current size[%d]", maxEntry, map.size());
		TimeUnit.MINUTES.sleep(10);
		map.close();
	}

}
