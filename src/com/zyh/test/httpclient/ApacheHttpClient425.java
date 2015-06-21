/**
 * 
 */
package com.zyh.test.httpclient;

import java.util.Arrays;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;

/**
 * @author zhyhang
 *
 */
public class ApacheHttpClient425 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		executeRequest();
	}

	private static void executeRequest() {
		System.out.println("Begin_Request");
		long tsb = System.currentTimeMillis();
		CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(
				RequestConfig.custom().setConnectionRequestTimeout(250).setConnectTimeout(500)
				.setSocketTimeout(500).build()).build();
		Thread[] ths = new Thread[200];
		Arrays.setAll(ths, i -> {
			return new Thread(() -> {
				// http://192.168.144.135:8180/template/tomcat/readme
				HttpGet get = new HttpGet("http://192.168.144.135:8180/template/tomcat/readme");
				HttpContext context = HttpClientContext.create();
				try {
					CloseableHttpResponse resp = client.execute(get, context);
					resp.getEntity().getContent().close();
					System.out.println("Response_OK");
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		});
		// serialized execute
		Arrays.stream(ths).forEach(t -> {
			t.start();
			try {
				t.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		System.out.printf("time_cost(ms): %d.\n", System.currentTimeMillis() - tsb);

	}
}
