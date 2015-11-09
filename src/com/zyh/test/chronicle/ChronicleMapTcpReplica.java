/**
 * 
 */
package com.zyh.test.chronicle;

import java.io.File;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

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
	private static String host;
	private static int port;
	private static byte identifier;
	private static String file;
	private static String persistFile;

	public static void main(String[] argv) throws Exception {
		if (argv.length < 5 || argv[0].equals("-h") || argv[0].equals("--help")) {
			System.out.format("Usage: <remote host> <port> <local identifier> <persist file> <key-value file>\n");
			System.out.format("\tmaster-master pattern, using same port at two servers. \n");
			System.out.format("\tlocal identifier muster number(<128), distinct among server. \n");
			System.out.format("\tkey(string) value(long) splitter(tab) \n");
			System.exit(-1);
		}
		host = argv[0];
		port = Integer.parseInt(argv[1]);
		identifier = Byte.parseByte(argv[2]);
		persistFile = argv[3];
		file = argv[4];

		// create map
		TcpTransportAndNetworkConfig tcpConfig = TcpTransportAndNetworkConfig
				.of(port, new InetSocketAddress(host, port)).heartBeatInterval(1L, TimeUnit.SECONDS);
		ChronicleMapBuilder<String, LongValue> mapBuilder = ChronicleMapBuilder.of(String.class, LongValue.class)
				.entries(100000L).replication(identifier, tcpConfig);
		ChronicleMap<String, LongValue> map = mapBuilder.createPersistedTo(new File(persistFile));

		Files.lines(Paths.get(file)).forEach(line -> {
			String[] kv = line.split("[\\s]+");
			LongValue value = DataValueClasses.newDirectInstance(LongValue.class);
			value.setValue(Long.parseLong(kv[1]));
			map.put(kv[0], value);
		});

		// wait for other server replica
		TimeUnit.SECONDS.sleep(60);
		
		//output
		try(PrintWriter pw=new PrintWriter(Files.newBufferedWriter(Paths.get(file+".new")))){
			map.forEach((k,v)->{
				pw.println(k+"\t"+v.getValue());
			});
		}
		
		System.out.println("see the file "+file+".new");
		
		// close map
		map.close();

	}

}
