/**
 * 
 */
package com.zyh.test.google.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.sync.CloseableHttpClient;
import org.apache.hc.client5.http.impl.sync.HttpClients;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * convert a flash file (swf) to html(5)
 * @author zhyhang
 *
 */
public interface Flash2Html {
	
	String ERROR_PREFIX = "error-f2h5-convertion: ";

	int TIMEOUT_MILLIS = 90000;
	int CONN_TOTAL = 2000;
	int CONN_PER_HOST = 400;
	
	Pattern PATTERN_RUNTIME_JS = Pattern.compile("https://www.gstatic.com/.+/runtime.js");
	String REPLACE_RUNTIME_JS = "http://fm.p0y.cn/j/swiffy/v7.4/runtime.js";

	CloseableHttpClient HTTPS_CLIENT = initHttpsClient();
	
	Logger logger = LoggerFactory.getLogger(Flash2Html.class);

	static CloseableHttpClient initHttpsClient() {
		SSLContext sslcontext = null;
		try {
			// Trust own CA and all self-signed certs
			sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Allow TLSv1 protocol only
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		PoolingHttpClientConnectionManager poolingmgr = new PoolingHttpClientConnectionManager();
		poolingmgr.setMaxTotal(CONN_TOTAL);
		poolingmgr.setDefaultMaxPerRoute(CONN_PER_HOST);
		CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setConnectionRequestTimeout(TIMEOUT_MILLIS)
						.setConnectTimeout(TIMEOUT_MILLIS).setSocketTimeout(TIMEOUT_MILLIS).build())
				.setConnectionManager(poolingmgr).setSSLSocketFactory(sslsf).build();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				httpclient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
		return httpclient;
	}

	/**
	 * convert a specified flash swf to html5 string
	 * 
	 * @param fileName
	 *            a full path flash swf file
	 * @return <b>html5 string if success</b><br>
	 *         <b>if error, return string is start with
	 *         "error-f2h5-convertion: "</b>
	 */
	String convert(String fileName);
	
	default String processHtml(String html5) {
		Matcher matcher = PATTERN_RUNTIME_JS.matcher(html5);
		if (matcher.find()) {
			return matcher.replaceAll(REPLACE_RUNTIME_JS);
		}else{
			logger.error("not found the pattern {} in html {}", PATTERN_RUNTIME_JS.toString(), html5);
		}
		return html5;
	}
}
