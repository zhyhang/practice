/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.zyh.test.dmp.t.client;

import com.zyh.test.dmp.t.codec.TencentDmpReqestEncoder;
import com.zyh.test.dmp.t.codec.TencentDmpResponseDecoder;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * 
 * @author zhyhang
 *
 */
public class TencentDmpClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // Add the number codec first,
        pipeline.addLast(new TencentDmpResponseDecoder());
        pipeline.addLast(new TencentDmpReqestEncoder());

        // and then business logic.
        pipeline.addLast(new TencentDmpClientHandler());
    }
}
