/**
 * 
 */
package com.zyh.test.https;

import org.apache.hc.client5.http.methods.CloseableHttpResponse;
import org.apache.hc.client5.http.methods.HttpGet;
import org.apache.hc.core5.http.entity.EntityUtils;

import com.zyh.test.google.api.Flash2Html;

/**
 * @author zhyhang
 *
 */
public class HttpsCall {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			System.out.println("count:" + i);
			HttpGet httpget = new HttpGet("https://www.baidu.com");
			httpget.setHeader("X-SSL-Secure", "true");
			try (CloseableHttpResponse response = Flash2Html.HTTPS_CLIENT.execute(httpget);) {
				String respStr = EntityUtils.toString(response.getEntity());
				if (respStr.indexOf("url=http://www.baidu.com/") < 0) {
					System.out.println(respStr);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
