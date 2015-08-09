package com.zyh.test.dmp.t.codec;

import java.time.LocalDateTime;
import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;
import com.zyh.test.dmp.t.client.TencentDmpClientHandler;
import com.zyh.test.dmp.t.model.TencentDmpResponse;
import com.zyh.test.dmp.t.model.TencentDmpRrHead;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import tencent.ExDMPRequestMsg.ExDMPResponse;

/**
 * 
 * @author zhyhang
 *
 */
public class TencentDmpResponseDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		System.out.printf("begin decode:%s\n", LocalDateTime.now().format(TencentDmpClientHandler.FORMATTER_NORMAL));
		// check head length
		if (in.readableBytes() < TencentDmpRrHead.HEAD_LENGTH) {
			return;
		}
		in.markReaderIndex();
		TencentDmpRrHead head = new TencentDmpRrHead();
		head.setVersion(in.readShort());
		head.setCommand(in.readShort());
		head.setBizId(in.readInt());
		head.setDspId(in.readInt());
		head.setSignature(in.readInt());
		head.setReserved(in.readInt());
		head.setResult(in.readShort());
		head.setLength(in.readShort());
		// Wait until the whole data is available.
		int bodyLength = head.getLength() - TencentDmpRrHead.HEAD_LENGTH;
		if (in.readableBytes() < bodyLength) {
			in.resetReaderIndex();
			return;
		}
		// Convert the received data into response body.
		ExDMPResponse body = null;
		if (bodyLength > 0) {
			byte[] decoded = new byte[bodyLength];
			in.readBytes(decoded);
			ExDMPResponse.Builder bodyBuilder = ExDMPResponse.newBuilder();
			try {
				body = bodyBuilder.mergeFrom(decoded).build();
			} catch (InvalidProtocolBufferException e) {
				throw new CorruptedFrameException("Invalid Dmp Response: " + e.getMessage());
			}
		}
		TencentDmpResponse response = new TencentDmpResponse();
		response.setHead(head);
		response.setBody(body);
		out.add(response);
		System.out.printf("end decode:%s\n", LocalDateTime.now().format(TencentDmpClientHandler.FORMATTER_NORMAL));
	}
}
