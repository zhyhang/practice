package com.zyh.test.google.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.sync.CloseableHttpClient;
import org.apache.hc.client5.http.impl.sync.HttpClients;
import org.apache.hc.client5.http.methods.CloseableHttpResponse;
import org.apache.hc.client5.http.methods.HttpPost;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.entity.EntityUtils;
import org.apache.hc.core5.http.entity.StringEntity;
import org.apache.hc.core5.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * call google swiffy service to convert flash swf file to html5.
 * 
 * @author zhyhang
 */
public class SwiffyRmi {

	public static final String ERROR_PREFIX = "error-f2h5-convertion: ";

	protected static Logger logger = LoggerFactory.getLogger(SwiffyRmi.class);

	private static final int TIMEOUT_MILLIS = 60000;

	private static final String URL_HTTPS_WWW_GOOGLEAPIS = "https://www.googleapis.com/rpc?key=AIzaSyCC_WIu0oVvLtQGzv4-g7oaWNoc-u8JpEI";

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
		String respHtml = new SwiffyRmi().convert("/home/zhyhang/temp/57.swf");
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
		SwiffyRequest swiffyRequest = new SwiffyRequest(fileName);
		if (swiffyRequest.getParams().getInput() == null || swiffyRequest.getParams().getInput().trim().isEmpty()) {
			logger.error("cannot read the file  {}", fileName);
			return ERROR_PREFIX + "cannot read the file " + fileName;
		}
		HttpPost httppost = new HttpPost(URL_HTTPS_WWW_GOOGLEAPIS);
		String apiReqStr = JSON.toJSONString(swiffyRequest);
		logger.debug("request-json: {}", apiReqStr);
		StringEntity postJson = new StringEntity(apiReqStr);
		httppost.setHeader("Host", "www.googleapis.com");
		httppost.setHeader("Cache-Control", "no-cache");
		postJson.setContentType("application/json");
		httppost.setEntity(postJson);

		logger.info("executing swiffy service request {}", httppost.getRequestLine());
		try (CloseableHttpResponse response = httpclient.execute(httppost)) {
			HttpEntity entity = response.getEntity();
			logger.info("swiffy service response {}", response.getStatusLine());
			String respJson = EntityUtils.toString(entity);
			if (ThreadLocalRandom.current().nextDouble() < 0.01) {
				logger.info("response json: {}", respJson);
			}
			SwiffyResponse respSwiffy = JSON.parseObject(respJson, SwiffyResponse.class);
			Optional<SwiffyRespDetail> respDetail = Optional.ofNullable(respSwiffy).map(SwiffyResponse::getResult)
					.map(SwiffyRespResult::getResponse);
			if (respDetail.isPresent()) {
				EntityUtils.consume(entity);
				return parseHtml(respDetail.get());
			} else {
				EntityUtils.consume(entity);
				logger.error("call swiffy api error, respone: {}", respJson);
				return ERROR_PREFIX + "call swiffy api error";
			}
		} catch (Exception e) {
			logger.error("call swiffy api error", e);
			return ERROR_PREFIX + "call swiffy api error";
		}
	}

	private String parseHtml(SwiffyRespDetail respDetail) {
		if ("SUCCESS".equalsIgnoreCase(respDetail.getStatus()) && respDetail.getOutput() != null
				&& respDetail.getOutput().trim().length() > 0) {
			String base64Data = respDetail.getOutput().replace('-', '+').replace('_', '/');
			byte[] decodedData = Base64.getDecoder().decode(base64Data);
			try {
				GZIPInputStream zis = new GZIPInputStream(new ByteArrayInputStream(decodedData));
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				int data = -1;
				while ((data = zis.read()) != -1) {
					bos.write(data);
				}
				return processHtml(new String(bos.toByteArray()));
			} catch (IOException e) {
				String errMsg = "cannot convert swiffy output data";
				logger.error(errMsg + "{}", respDetail.getOutput());
				return ERROR_PREFIX + errMsg;
			}
		} else {
			return ERROR_PREFIX + respDetail.getStatus();
		}

	}

	private String processHtml(String html5) {
		Matcher matcher = PATTERN_RUNTIME_JS.matcher(html5);
		if (matcher.find()) {
			return matcher.replaceAll(REPLACE_RUNTIME_JS);
		}
		return html5;
	}

}

class SwiffyRequest {
	private String apiVersion = "v1";
	private String method = "swiffy.convertToHtml";
	private SwiffyReqParams params;

	public SwiffyRequest(String file) {
		params = new SwiffyReqParams(file);
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public SwiffyReqParams getParams() {
		return params;
	}

	public void setParams(SwiffyReqParams params) {
		this.params = params;
	}

}

class SwiffyReqParams {
	private String client = "IPinyou F2H5 Converter from Swiffy";

	private String input = null;

	public SwiffyReqParams(String file) {
		try (FileInputStream fis = new FileInputStream(file)) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int data;
			while ((data = fis.read()) != -1) {
				bos.write(data);
			}
			input = Base64.getEncoder().encodeToString(bos.toByteArray()).replace('+', '-').replace('/', '_');
		} catch (Exception e) {
			SwiffyRmi.logger.error("read file {} error", file, e);
		}
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

}

class SwiffyResponse {
	private SwiffyRespResult result;

	public SwiffyRespResult getResult() {
		return result;
	}

	public void setResult(SwiffyRespResult result) {
		this.result = result;
	}

}

class SwiffyRespResult {
	private SwiffyRespDetail response;

	public SwiffyRespDetail getResponse() {
		return response;
	}

	public void setResponse(SwiffyRespDetail response) {
		this.response = response;
	}

}

class SwiffyRespDetail {
	private String status;
	private String output;
	private String version;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}