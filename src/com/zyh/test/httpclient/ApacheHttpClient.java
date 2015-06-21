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
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

/**
 * @author zhyhang
 *
 */
public class ApacheHttpClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConcurrentRequest();
	}

	private static void ConcurrentRequest() {
		System.out.println("Begin_Request");
		long tsb = System.currentTimeMillis();
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(400);
		cm.setDefaultMaxPerRoute(200);// indicate create how many connections per host
		try (CloseableHttpClient client = HttpClients
				.custom()
				.setConnectionManager(cm)
				.setDefaultRequestConfig(
						RequestConfig.custom().setConnectionRequestTimeout(250).setConnectTimeout(500)
								.setSocketTimeout(500).build()).build();) {
			Thread[] ths = new Thread[200];
			Arrays.setAll(ths, i -> {
				return new Thread(() -> {
					// http://192.168.144.135:8180/template/tomcat/readme
					HttpGet get = new HttpGet("http://192.168.144.135:8180/template/tomcat/readme");
					HttpContext context = HttpClientContext.create();
					try {
						CloseableHttpResponse resp = client.execute(get, context);
						resp.getEntity().getContent().close();// it's important,must be executed before response.close()
						System.out.println("Response_OK");

					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			});
			Arrays.stream(ths).forEach(t -> t.start());
			Arrays.stream(ths).forEach(t -> {
				try {
					t.join();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.printf("time_cost(ms): %d.\n", System.currentTimeMillis() - tsb);

	}
}
