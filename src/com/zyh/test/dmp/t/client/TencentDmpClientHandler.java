package com.zyh.test.dmp.t.client;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;

import com.zyh.test.dmp.t.model.TencentDmpRequest;
import com.zyh.test.dmp.t.model.TencentDmpResponse;
import com.zyh.test.dmp.t.model.TencentDmpRrHead;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import tencent.ExDMPRequestMsg.ExDMPRequest;

/**
 * 
 * @author zhyhang
 *
 */
public class TencentDmpClientHandler extends SimpleChannelInboundHandler<TencentDmpResponse> {

	public static final DateTimeFormatter FORMATTER_NORMAL = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private Channel channel;

	private CountDownLatch latch = new CountDownLatch(1);

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) {
		channel = ctx.channel();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TencentDmpResponse msg) throws Exception {
		System.out.printf("%s\nResponse:%s-------------------------------------------------\n",
				LocalDateTime.now().format(FORMATTER_NORMAL), null == msg ? null : msg.toString());
		latch.countDown();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	public void writeTest() {
		TencentDmpRequest request = new TencentDmpRequest();
		request.setHead(new TencentDmpRrHead());
		ExDMPRequest.Builder bodyBuilder = ExDMPRequest.newBuilder();
		bodyBuilder.setUserId("F87AFc85abyv");
		bodyBuilder.addGroupId("1");
		bodyBuilder.addGroupId("2");
		bodyBuilder.addGroupId("3");
		bodyBuilder.addGroupId("102");
		request.setBody(bodyBuilder.build());
		System.out.printf("%s\nRequest:%s-------------------------------------------------\n",
				LocalDateTime.now().format(FORMATTER_NORMAL), request.toString());
		channel.writeAndFlush(request);
		try {
			latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
