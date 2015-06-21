/**
 * 
 */
package com.zyh.test.api;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * @author zhyhang
 *
 */
public class SnBiApiRequest {

	private final static String DATA_FILE = "sn_bi.xml";

	private final static int POOL_SIZE = 200;

	private static List<String> mockDatas = new ArrayList<String>();

	private static LinkedTransferQueue<HttpClient> clientPool = new LinkedTransferQueue<HttpClient>();

	private static ScheduledExecutorService ses = Executors.newScheduledThreadPool(POOL_SIZE);

	private static ScheduledExecutorService logEs = Executors.newScheduledThreadPool(1);

	private static AtomicLong requestCount = new AtomicLong(0);

	private static AtomicLong requestCountLast = new AtomicLong(0);

	private static CloseableHttpClient client = null;

	public static void main(String[] args) {

		// read mock data from files
		try (Stream<String> stream = Files.lines(Paths.get(DATA_FILE))) {
			stream.forEach(line -> mockDatas.add(line));
		} catch (Exception e) {
		}
		// init client pool
		for (int i = 0; i < POOL_SIZE; i++) {
			clientPool.add(HttpClients
					.custom()
					.setDefaultRequestConfig(
							RequestConfig.custom().setSocketTimeout(500).setConnectTimeout(1000).build()).build());
		}
		// init http client
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(1000);
		cm.setDefaultMaxPerRoute(500);// indicate create how many connections per host
		client = HttpClients
				.custom()
				.setConnectionManager(cm)
				.setDefaultRequestConfig(
						RequestConfig.custom().setConnectionRequestTimeout(250).setConnectTimeout(500)
								.setSocketTimeout(500).build()).build();
		// add hook when application will be shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				client.close();
			} catch (Exception e) {
			}
		}));
		// request console output
		System.out.printf("%s\t%s\t%s\n", "TimeStamp", "RequestCount", "Qps");
		logEs.scheduleAtFixedRate(
				() -> {
					long rc = requestCount.get();
					long rcLast = requestCountLast.getAndSet(rc);
					System.out.printf("%s\t%d\t%d\n",
							LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")), rc, rc
									- rcLast);
				}, 1, 1, TimeUnit.SECONDS);

		// mock http post
		int qps = 100;
		if (args != null && args.length > 0) {
			qps = Integer.parseInt(args[0]);
		}
		qps = qps < 1 ? 100 : qps;

		int reqPer10ms = qps / 100;
		int reqPer100ms = (qps - reqPer10ms * 100) / 10;
		int reqPerSecond = qps % 10;

		while (true) {
			long tsb = System.nanoTime();
			if (reqPer10ms > 0) {
				scheduleQps(10, 100, reqPer10ms);
			}
			if (reqPer100ms > 0) {
				scheduleQps(100, 10, reqPer100ms);
			}
			if (reqPerSecond > 0) {
				scheduleQps(1000, 1, reqPerSecond);
			}
			long timeElapse = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - tsb);
			if (timeElapse < TimeUnit.SECONDS.toMicros(1)) {
				try {
					TimeUnit.MICROSECONDS.sleep(TimeUnit.SECONDS.toMicros(1) - timeElapse);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private static void scheduleQps(int timePerSegment, int segments, int qpsPerSegment) {
		for (int i = 0; i < segments; i++) {
			for (int j = 0; j < qpsPerSegment; j++) {
				ses.schedule(
						() -> {
							if (mockDatas.isEmpty()) {
								return;
							}
							int index = new Random().nextInt(mockDatas.size());
							String requestData = mockDatas.get(index);
							if (null == requestData || requestData.trim().isEmpty()) {
								return;
							}
							StringEntity entity = new StringEntity(requestData, StandardCharsets.UTF_8);
							HttpPost post = new HttpPost(
									"http://192.168.152.220:9901/openapi/customized/bi/suning/m/c3VuaW5n");
							post.setEntity(entity);
							try (CloseableHttpResponse response = client.execute(post, HttpClientContext.create());) {
								if (response.getEntity() != null && response.getEntity().getContent() != null) {
									response.getEntity().getContent().close(); // release connect
								}
								requestCount.incrementAndGet();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}, i * timePerSegment, TimeUnit.MILLISECONDS);
			}
		}
	}
}
