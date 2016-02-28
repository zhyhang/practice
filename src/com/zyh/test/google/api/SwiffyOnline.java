package com.zyh.test.google.api;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.methods.CloseableHttpResponse;
import org.apache.hc.client5.http.methods.HttpGet;
import org.apache.hc.client5.http.methods.HttpPost;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * call google swiffy online upload to convert flash swf file to html5.
 * 
 * @author zhyhang
 */
public class SwiffyOnline implements Flash2Html{

	private static Logger logger = LoggerFactory.getLogger(SwiffyOnline.class);

	/**
	 * Online Swiffy upload constants
	 */
	private static final String URL_HTTPS_SWIFFY_UPLOAD = "https://google-swiffy.appspot.com/upload";
	private static final Header[] HEADERS_SWIFFY_UPLOAD = new Header[] {
			new BasicHeader("Host", "google-swiffy.appspot.com"),
			new BasicHeader("Accept-Encoding", "gzip,deflate,sdch"),
			new BasicHeader("User-Agent",
					"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:44.0) Gecko/20100101 Firefox/44.0"),
			// important don't remove
			new BasicHeader("Origin", "https://google-developers.appspot.com"),
			new BasicHeader("Referer", "https://google-developers.appspot.com/swiffy/convert/")

	};
	private static final String FORM_SWF_NAME_SWIFFY_UPLOAD="swfFile";

	public final static void main(String[] args) throws Exception {
		String respHtml = new SwiffyOnline().convert("d:/temp/57.swf");//70f74b3c4327345a0d33b65e2a1f0155.swf");// 57.swf");
		System.out.println(respHtml);
	}

	public String convert(String fileName) {
		HttpEntity reqEntity = null;
		try{
			reqEntity = MultipartEntityBuilder.create().addPart(FORM_SWF_NAME_SWIFFY_UPLOAD, new FileBody(new File(fileName))).build();
		}catch(Exception e){
			logger.error("cannot read the file  {}", fileName);
			return ERROR_PREFIX + "cannot read the file " + fileName;
		}
		HttpPost httppost = new HttpPost(URL_HTTPS_SWIFFY_UPLOAD);
		httppost.setEntity(reqEntity);
		httppost.setHeaders(HEADERS_SWIFFY_UPLOAD);

		logger.info("executing swiffy upload, file {}, {}", fileName, httppost.getRequestLine());
		try (CloseableHttpResponse response = HTTPS_CLIENT.execute(httppost)) {
				HttpEntity entity = response.getEntity();
				logger.info("swiffy upload response {}", response.getStatusLine());
				String respJson = EntityUtils.toString(entity);
				logger.info("response json: {}", respJson);
				SwiffyOnlineResponse respSwiffy = JSON.parseObject(respJson, SwiffyOnlineResponse.class);
				if (respSwiffy!=null && respSwiffy.getOutputFileDir()!=null && respSwiffy.getOutputFile()!=null) {
					EntityUtils.consume(entity);
					return parseHtmlOnline(respSwiffy);
				} else {
					EntityUtils.consume(entity);
					return ERROR_PREFIX + "call swiffy upload error";
				}
		} catch (Exception e) {
			logger.error("call swiffy upload error", e);
			return ERROR_PREFIX + "call swiffy upload error";
		}

	}
	
	private String parseHtmlOnline(SwiffyOnlineResponse uploadResp){
		String htmlUrl=uploadResp.getOutputFileDir().trim();
		htmlUrl=htmlUrl.endsWith("/")?htmlUrl.concat(uploadResp.getOutputFile().trim()):htmlUrl.concat("/").concat(uploadResp.getOutputFile().trim());
		HttpGet httpget=new HttpGet(htmlUrl);
		logger.info("executing get swiffy upload converted file {}", htmlUrl);
		try (CloseableHttpResponse response = HTTPS_CLIENT.execute(httpget)) {
				HttpEntity entity = response.getEntity();
				logger.info("get swiffy upload converted file response {}", response.getStatusLine());
				String respHtml = EntityUtils.toString(entity,StandardCharsets.UTF_8);
				if (respHtml!=null) {
					EntityUtils.consume(entity);
					return processHtml(respHtml);
				} else {
					EntityUtils.consume(entity);
					return ERROR_PREFIX + "get swiffy upload converted file error";
				}
		} catch (Exception e) {
			logger.error("get swiffy upload converted file error", e);
			return ERROR_PREFIX + "get swiffy upload converted file error";
		}
	}
	
}

class SwiffyOnlineResponse {
	private String outputFileDir;
	private String outputFile;
	private String creativeKey;
	private int width;
	private int height;
	private double swfSizeInKb;
	private double totalOutputSizeGzippedInKb;
	private SwiffyOnlineRespMsg[] messages;

	public String getOutputFileDir() {
		return outputFileDir;
	}

	public void setOutputFileDir(String outputFileDir) {
		this.outputFileDir = outputFileDir;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public String getCreativeKey() {
		return creativeKey;
	}

	public void setCreativeKey(String creativeKey) {
		this.creativeKey = creativeKey;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public double getSwfSizeInKb() {
		return swfSizeInKb;
	}

	public void setSwfSizeInKb(double swfSizeInKb) {
		this.swfSizeInKb = swfSizeInKb;
	}

	public double getTotalOutputSizeGzippedInKb() {
		return totalOutputSizeGzippedInKb;
	}

	public void setTotalOutputSizeGzippedInKb(double totalOutputSizeGzippedInKb) {
		this.totalOutputSizeGzippedInKb = totalOutputSizeGzippedInKb;
	}

	public SwiffyOnlineRespMsg[] getMessages() {
		return messages;
	}

	public void setMessages(SwiffyOnlineRespMsg[] messages) {
		this.messages = messages;
	}

}

class SwiffyOnlineRespMsg {
	private String message;
	private int numberOfOccurences;
	private String messageType;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getNumberOfOccurences() {
		return numberOfOccurences;
	}

	public void setNumberOfOccurences(int numberOfOccurences) {
		this.numberOfOccurences = numberOfOccurences;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

}
