/**
 * 
 */
package com.zyh.test.chronicle;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhyhang
 *
 */
public class PersisCacheCall {

	private static Logger logger = LoggerFactory.getLogger(PersisCacheCall.class);

	private static CloseableHttpClient client;

	private static ExecutorService es = Executors.newFixedThreadPool(32);

	private final static AtomicLong TotalRequest = new AtomicLong();

	private final static AtomicLong TotalError = new AtomicLong();

	private final static AtomicLong TotalTimecost = new AtomicLong();
	
	private static String url;
	
	private static long[][] deltaThres;

	static {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(400);
		cm.setDefaultMaxPerRoute(200);
		client = HttpClients.custom()
				.setConnectionManager(cm).setDefaultRequestConfig(RequestConfig.custom()
						.setConnectionRequestTimeout(250).setConnectTimeout(500).setSocketTimeout(60000).build())
				.build();
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		checkArguments(args);
		url=args[0];
		String method = args[1];
		int sizePerReq = Integer.parseInt(args[2]);
		int reqCount = Integer.parseInt(args[3]);
		if ("put".equalsIgnoreCase(method)) {
			putBatch(sizePerReq, reqCount);
		}
		es.shutdown();
		if (reqCount >= 0) {
			logger.info(method + "-threshold-waiting-end...");
			es.awaitTermination(10, TimeUnit.MINUTES);
			logger.info(
					method + "-threshold-end, sizePerReq[{}], totalRequest[{}], totalError(error and not 200 status code)[{}], totalTimecost[{}]ms.",
					sizePerReq, TotalRequest.get(), TotalError.get(), TotalTimecost.get());
		}
	}

	private static void putBatch(int sizePerReq, int reqCount) {
		logger.info("put-threshold-begin, sizePerReq[{}].", sizePerReq);
		// prepare data
		deltaThres = new long[sizePerReq][];
		Arrays.parallelSetAll(deltaThres, i -> {
			long[] thres = new long[8];
			Arrays.setAll(thres, index -> 1);
			thres[0] = i;
			thres[1] = System.currentTimeMillis();
			return thres;
		});
		// send request
		try {
			long lastSendReq = 0;
			while (reqCount < 0 || reqCount-->0) {
				es.execute(PersisCacheCall::putBatchTask);
				// Control speed
				if (TotalRequest.get() - lastSendReq > 80 || ThreadLocalRandom.current().nextDouble() < 0.01) {
					lastSendReq = TotalRequest.get();
					logger.info(
							"put-threshold-info, sizePerReq[{}], totalRequest[{}], totalError(error and not 200 status code)[{}], totalTimecost[{}]ms.",
							sizePerReq, TotalRequest.get(), TotalError.get(), TotalTimecost.get());
					TimeUnit.SECONDS.sleep(1);
				}
			}
		} catch (Exception e) {
			logger.error("put-threshold-error:", e);
		}
	}

	private static void putBatchTask() {
		try {
			TotalRequest.incrementAndGet();
			HttpPost post = new HttpPost(url);
			SerializableEntity entity = new SerializableEntity(deltaThres, true);
			entity.setChunked(true);
			post.setEntity(entity);
			long tsb = System.currentTimeMillis();
			CloseableHttpResponse resp = client.execute(post, HttpClientContext.create());
			resp.getEntity().getContent().close();
			if (resp.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
				TotalError.incrementAndGet();
			}
			TotalTimecost.addAndGet(System.currentTimeMillis() - tsb);
		} catch (Exception e) {
			TotalError.incrementAndGet();
			logger.error("put-threshold-task-error:", e);
		}
	}

	private static void checkArguments(String[] args) {
		if (args.length < 4 || args[0].equals("-h") || args[0].equals("--help")) {
			System.out.format("Usage: <url> <put/get> <size per-request> <request count>\n");
			System.out.format("\turl: persis service http url. \n");
			System.out.format("\tput: put thresholds to persis. get: query thresholds from persis. \n");
			System.out.format("\tsize per-request: numbers of keys per-request to persis. \n");
			System.out.format(
					"\trequest count: numbers of total requests sending to persis. negative (<0) number indicats continue loop. \n");
			System.exit(-1);
		}
	}

}
