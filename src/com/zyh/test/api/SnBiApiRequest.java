/**
 * 
 */
package com.zyh.test.api;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @author zhyhang
 *
 */
public class SnBiApiRequest {
	
	private static List<String> mockDatas = new ArrayList<String>();
	
	private static LinkedTransferQueue<HttpClient>  clientPool = new LinkedTransferQueue<HttpClient>();
	
	private static ScheduledExecutorService ses = Executors.newScheduledThreadPool(100);
	
	private static AtomicLong requestCount = new AtomicLong(0);
	
	public static void main(String[] args) {
		
		// read mock data from files
		try(Stream<String> stream = Files.lines(Paths.get(""))){
			stream.forEach(line->mockDatas.add(line));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// init client pool
		for (int i = 0; i < 100; i++) {
			clientPool.add(HttpClients.createDefault());
		}
		// mock http post
		int qps = 100;
		if(args!=null && args.length>0){
			qps = Integer.parseInt(args[0]);
		}
		qps=qps<1?100:qps;
		
		int reqPer10ms = qps / 100;
		int reqPer100ms = (qps - reqPer10ms * 100) / 10;
		int reqPerSecond = qps % 10;
		if(reqPer10ms > 0){
			for (int i = 0; i < 100; i++) {
				for (int j = 0; j < reqPer10ms; j++) {
					ses.schedule(()->{ requestCount.incrementAndGet();}, i* 10, TimeUnit.MILLISECONDS);
				}
			}
		}
		
		
		// request console output
		System.out.println("TimeStamp\tRequestCount\n");
		ses.scheduleAtFixedRate(()->{System.out.printf("%s\t%d\n", "",requestCount.get());}, 1, 1, TimeUnit.SECONDS);

		


		

	}
}
