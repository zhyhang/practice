/**
 * 
 */
package com.zyh.test.chronicle;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
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

	private static ScheduledThreadPoolExecutor es = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(32);
	
	private final static AtomicLong TotalRequest = new AtomicLong();

	private final static AtomicLong TotalError = new AtomicLong();

	private final static AtomicLong TotalTimecost = new AtomicLong();

	private static String url;

	private static DeltaAdThreshold[] deltaThres;
	
	private static String method;
	
	private static int sizePerReq;
	
	private static int reqCount;

	static {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(400);
		cm.setDefaultMaxPerRoute(200);
		client = HttpClients.custom()
				.setConnectionManager(cm).setDefaultRequestConfig(RequestConfig.custom()
						.setConnectionRequestTimeout(250).setConnectTimeout(500).setSocketTimeout(60000).build())
				.build();
		es.scheduleAtFixedRate(PersisCacheCall::printLog, 30, 20, TimeUnit.SECONDS);
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		checkArguments(args);
		url = args[0];
		method = args[1];
		sizePerReq = Integer.parseInt(args[2]);
		reqCount = Integer.parseInt(args[3]);
		Runtime.getRuntime().addShutdownHook(new Thread(PersisCacheCall::hookShutdown));
		if ("put".equalsIgnoreCase(method)) {
			putBatch(sizePerReq, reqCount);
		}
		System.exit(0);
	}
	
	private static void printLog(){
		logger.info(
				method + "-threshold-info, sizePerReq[{}], totalRequest[{}], totalError(error and not 200 status code)[{}], totalTimecost[{}]ms.",
				sizePerReq, TotalRequest.get(), TotalError.get(), TotalTimecost.get());		
	}

	private static void hookShutdown() {
		try {
			es.shutdownNow();
			printLog();
			int waitMinutes=1;
			logger.info(method + "-threshold-waiting-end, waiting {} minutes...",waitMinutes);
			es.awaitTermination(waitMinutes, TimeUnit.MINUTES);
			printLog();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private static void putBatch(int sizePerReq, int reqCount) {
		logger.info("put-threshold-begin, sizePerReq[{}].", sizePerReq);
		// prepare data
		deltaThres = new DeltaAdThreshold[sizePerReq];
		Arrays.parallelSetAll(deltaThres, i -> new DeltaAdThreshold((byte) 1, 1000000 + i, System.currentTimeMillis(),
				new int[] { 1111111, 111111, 1111 }));
		// send request
		try {
			long lastSendReq = 0;
			while (reqCount < 0 || reqCount-- > 0) {
				es.execute(PersisCacheCall::putBatchTask);
				// Control speed
				int taskSize=es.getQueue().size();
				if (TotalRequest.get() - lastSendReq > 80 || ThreadLocalRandom.current().nextDouble() < 0.01 || taskSize > 1000 ) {
					lastSendReq = TotalRequest.get();
					printLog();
					TimeUnit.SECONDS.sleep(1+taskSize / 1000);
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
			ByteArrayEntity entity = new ByteArrayEntity(DeltaAdThreshold.toBytes(deltaThres));
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

final class DeltaAdThreshold implements Serializable {

	public final static int BYTES = 29;
	private static final long serialVersionUID = -8243877799040040995L;
	private byte type;
	private long id;
	private long ts;
	private int[] deltas;

	public DeltaAdThreshold(byte type, long id, long ts, int[] deltas) {
		this.type = type;
		this.id = id;
		this.ts = ts;
		this.deltas = deltas;
	}

	public byte getType() {
		return type;
	}

	public long getId() {
		return id;
	}

	public long getTs() {
		return ts;
	}

	public int[] getDeltas() {
		return deltas;
	}

	public static byte[] toBytes(DeltaAdThreshold[] daThres) {
		if (daThres == null || daThres.length == 0) {
			return null;
		}
		int length = BYTES * daThres.length;
		ByteBuffer bb = ByteBuffer.allocate(length);
		for (int i = 0; i < daThres.length; i++) {
			DeltaAdThreshold thres=daThres[i];
			bb.put(thres.getType());
			bb.putLong(thres.getId());
			bb.putLong(thres.getTs());
			for (int j = 0; j < thres.getDeltas().length; j++) {
				bb.putInt(thres.getDeltas()[j]);
			}
		}
		return bb.array();
	}

}
