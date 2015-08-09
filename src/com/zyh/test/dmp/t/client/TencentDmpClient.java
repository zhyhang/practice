package com.zyh.test.dmp.t.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 
 * @author zhyhang
 *
 */
public final class TencentDmpClient {

	private static final String HOST = System.getProperty("host", "125.39.213.107");
	private static final int PORT = Integer.parseInt(System.getProperty("port", "1688"));

	public static void main(String[] args) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).handler(new TencentDmpClientInitializer());
			// Make a new connection.
			Channel ch = b.connect(HOST, PORT).sync().channel();
			// Get the handler instance to retrieve the answer.
			TencentDmpClientHandler handler = (TencentDmpClientHandler) ch.pipeline().last();
			// Bussiness logic
			handler.writeTest();
		} finally {
			group.shutdownGracefully();
		}
	}
}
