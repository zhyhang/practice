package com.zyh.test.j2ee.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.http.util.ByteArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 互动审核测试服务器模拟类
 * @author zhyhang
 *
 */
public class AdinAuditMockServlet extends HttpServlet {

	private static final long serialVersionUID = -8257348288800666445L;
	
	private transient Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		InputStream instream = req.getInputStream();
		if (instream != null) {
			 ByteArrayBuffer buffer = new ByteArrayBuffer(1024);
		        byte[] tmp = new byte[1024];
		        int l;
		        while ((l = instream.read(tmp)) != -1) {
		            buffer.append(tmp, 0, l);
		        }
		        String reqString=new String(buffer.toByteArray(),"UTF-8");
		        reqString=reqString.replace("request=[", "");
		        reqString=reqString.substring(0, reqString.length() - 1);
		        JSONObject jsonReq = JSON.parseObject(reqString, JSONObject.class);
		        logger.error("A request coming:");
		        logger.error(reqString);
		        String jsonBd=jsonReq.getString("binaryData");
		        logger.error(jsonBd);
		        //生成文件
		        byte[] decodeBdata=Base64.decodeBase64(jsonBd);
		        File homeDir=SystemUtils.getUserHome();
		        String imgFilename=homeDir.getCanonicalPath()+"/temp/adin"+System.currentTimeMillis()+".jpg";
		        logger.error("生成文件：{}",imgFilename);
		        FileOutputStream fos=new FileOutputStream(homeDir.getAbsoluteFile()+"/temp/adin"+System.currentTimeMillis()+".jpg");
		        IOUtils.write(decodeBdata, fos);
		        fos.close();
		}
	}

}
