/**
 * 
 */
package com.zyh.test.httpclient;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import jodd.http.HttpRequest;
import jodd.http.HttpResponse;

/**
 * @author zhyhang
 *
 */
public class JoddHttpClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.printf("http.keepAlive: %s.\n",System.getProperty("http.keepAlive"));
		System.out.printf("http.maxConnections: %s.\n",System.getProperty("http.maxConnections"));
		ConcurrentRequest();
	}

	private static void ConcurrentRequest() {
		System.out.println("Begin_Request");
		long tsb = System.currentTimeMillis();
		try {
			Thread[] ths = new Thread[1];
			Arrays.setAll(
					ths,
					i -> {
						return new Thread(() -> {
							// http://192.168.144.135:8180/template/tomcat/readme
							// http://dsp.ipinyou.com/test.jsp
							// http://cm.ipinyou.com/query/cookie.jsp
						String url = "http://cm.ipinyou.com/query/cookie.jsp";
								try {
								    HttpRequest httpRequest = HttpRequest.get(url);
									long tsbms = System.currentTimeMillis();
								    HttpResponse response = httpRequest.send();
								    response.accept();
							        System.out.printf("transport time cost: %d.\n",System.currentTimeMillis() - tsbms);
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
