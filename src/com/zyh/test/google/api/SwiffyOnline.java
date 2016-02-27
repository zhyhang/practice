package com.zyh.test.google.api;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.sync.CloseableHttpClient;
import org.apache.hc.client5.http.impl.sync.HttpClients;
import org.apache.hc.client5.http.methods.CloseableHttpResponse;
import org.apache.hc.client5.http.methods.HttpPost;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.entity.ContentType;
import org.apache.hc.core5.http.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * call google swiffy service to convert flash swf file to html5.
 * 
 * @author zhyhang
 */
public class SwiffyOnline {

	public static final String ERROR_PREFIX = "error-f2h5-convertion: ";

	protected static Logger logger = LoggerFactory.getLogger(SwiffyOnline.class);

	private static final int TIMEOUT_MILLIS = 60000;

	private static final String URL_HTTPS_WWW_GOOGLEAPIS = "https://google-swiffy.appspot.com/upload";

	private static final Pattern PATTERN_RUNTIME_JS = Pattern.compile("https://www.gstatic.com/.+/runtime.js");

	private static final String REPLACE_RUNTIME_JS = "http://fm.p0y.cn/j/swiffy/v7.4/runtime.js";

	private static CloseableHttpClient httpclient;

	static {
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
		httpclient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setConnectionRequestTimeout(TIMEOUT_MILLIS)
						.setConnectTimeout(TIMEOUT_MILLIS).setSocketTimeout(TIMEOUT_MILLIS).build())
				.setSSLSocketFactory(sslsf).build();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				httpclient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
	}

	public final static void main(String[] args) throws Exception {
		String respHtml = new SwiffyOnline().convert("/home/zhyhang/temp/57.swf");
		System.out.println(respHtml);
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
	public String convert(String fileName) {
		
        HttpPost httppost = new HttpPost(URL_HTTPS_WWW_GOOGLEAPIS);
		
		 FileBody bin = new FileBody(new File(fileName));
         StringBody comment = new StringBody("A binary file of some kind", ContentType.TEXT_PLAIN);

         HttpEntity reqEntity = MultipartEntityBuilder.create()
                 .addPart("bin", bin)
//                 .addPart("comment", comment)
//                 .setContentType(ContentType.MULTIPART_FORM_DATA)
                 .build();

         
         httppost.setHeader("Host","google-swiffy.appspot.com");
         httppost.setHeader("Accept-Encoding", "gzip,deflate,sdch");
         httppost.setHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:44.0) Gecko/20100101 Firefox/44.0");
         httppost.setHeader("Origin", "https://google-developers.appspot.com");
         httppost.setHeader("Referer", "https://google-developers.appspot.com/swiffy/convert/upload_945c8a7aa0714560e00b8a9e935f9be1.frame?hl=en&redesign=true");
         httppost.setEntity(reqEntity);
         
         System.out.println("executing request " + httppost.getRequestLine());
         try(CloseableHttpResponse response = httpclient.execute(httppost)) {
             System.out.println("----------------------------------------");
             System.out.println(response.getStatusLine());
             HttpEntity resEntity = response.getEntity();
             if (resEntity != null) {
                 System.out.println("Response content length: " + resEntity.getContentLength());
                 System.out.println("Response content detail: " + EntityUtils.toString(resEntity));
             }
             EntityUtils.consume(resEntity);
         }catch(Exception e){
        	 e.printStackTrace();
         }
         return null;
         

	}

	private String processHtml(String html5) {
		Matcher matcher = PATTERN_RUNTIME_JS.matcher(html5);
		if (matcher.find()) {
			return matcher.replaceAll(REPLACE_RUNTIME_JS);
		}
		return html5;
	}

}

