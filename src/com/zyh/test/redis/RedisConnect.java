package com.zyh.test.redis;

import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RedisConnect {

	public static void main(String... argv) {
		PrintWriter logger = null;
		int successCons = 0;
		try {
			logger = new PrintWriter(System.getProperty("user.home") + "/redis.connect.log."
					+ System.currentTimeMillis());
		} catch (Exception e) {
			System.err.println(e);
			return;
		}
		Socket socket = null;

		for (int i = 0; i < 600; i++) {
			long ts = 0;
			try {
				socket = new Socket();
				socket.setReuseAddress(true);
				socket.setKeepAlive(true); // Will monitor the TCP connection is
											// valid
				socket.setTcpNoDelay(true); // Socket buffer Whetherclosed, to
											// ensure timely delivery of data
				socket.setSoLinger(true, 0); // Control calls close () method,
												// the underlying socket is
												// closed immediately
				ts = System.currentTimeMillis();
				socket.connect(new InetSocketAddress("192.168.144.57", 6481), 500);
				socket.close();
				logger.println(++successCons);
				logger.flush();
				Thread.sleep(200);
			} catch (Exception ex) {
				logger.print(System.currentTimeMillis() - ts);
				logger.println(ex);
				logger.flush();
			}
		}
		logger.close();
	}

}
