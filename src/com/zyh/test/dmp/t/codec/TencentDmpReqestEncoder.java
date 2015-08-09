/**
 * 
 */
package com.zyh.test.dmp.t.codec;

import java.time.LocalDateTime;

import com.zyh.test.dmp.t.client.TencentDmpClientHandler;
import com.zyh.test.dmp.t.model.TencentDmpRequest;
import com.zyh.test.dmp.t.model.TencentDmpRrHead;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import tencent.ExDMPRequestMsg.ExDMPRequest;

/**
 * @author zhyhang
 *
 */
public class TencentDmpReqestEncoder extends MessageToByteEncoder<TencentDmpRequest> {

	@Override
	protected void encode(ChannelHandlerContext ctx, TencentDmpRequest msg, ByteBuf out) throws Exception {
		if (msg == null || msg.getBody() == null || msg.getHead() == null) {
			return;
		}
		System.out.printf("begin encode:%s\n", LocalDateTime.now().format(TencentDmpClientHandler.FORMATTER_NORMAL));
		TencentDmpRrHead head = msg.getHead();
		ExDMPRequest body = msg.getBody();
		byte[] bodyData = body.toByteArray();
		head.setLength((short) (TencentDmpRrHead.HEAD_LENGTH + bodyData.length));
		out.writeShort(head.getVersion());
		out.writeShort(head.getCommand());
		out.writeInt(head.getBizId());
		out.writeInt(head.getDspId());
		out.writeInt(head.getSignature());
		out.writeInt(head.getReserved());
		out.writeShort(head.getResult());
		out.writeShort(head.getLength());
		System.out.printf("end encode:%s\n", LocalDateTime.now().format(TencentDmpClientHandler.FORMATTER_NORMAL));
		out.writeBytes(bodyData);
	}

}
