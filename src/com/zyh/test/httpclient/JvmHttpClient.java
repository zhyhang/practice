/**
 * 
 */
package com.zyh.test.httpclient;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;import java.util.Scanner;


/**
 * @author zhyhang
 *
 */
public class JvmHttpClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConcurrentRequest();
	}

	private static void ConcurrentRequest() {
		System.out.println("Begin_Request");
		long tsb = System.currentTimeMillis();
		try {
			Thread[] ths = new Thread[2];
			Arrays.setAll(
					ths,
					i -> {
						return new Thread(() -> {
							// http://192.168.144.135:8180/template/tomcat/readme
						String url = "http://cn.bing.com";
								try {
									HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
									conn.setDoOutput(true);
									conn.setDoInput(true);
									conn.setConnectTimeout(1000);
									conn.setReadTimeout(1000);
									conn.setRequestProperty("Content-type", "application/json");   
									conn.setRequestMethod("POST");
									long tsbms = System.currentTimeMillis();
									conn.connect();
									System.out.printf("connection time cost: %d.\n",System.currentTimeMillis() - tsbms);
									tsbms = System.currentTimeMillis();
									Scanner scanner = new Scanner(conn.getInputStream(),"utf-8");
									while(scanner.hasNextLine()){
										scanner.nextLine();
									}
									scanner.close();
							        System.out.printf("transport time cost: %d.\n",System.currentTimeMillis() - tsbms);
							        conn.disconnect();
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
