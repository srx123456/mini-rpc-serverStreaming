package io.github.lanicc.mrpc.remote;

import io.github.lanicc.mrpc.Config;
import io.github.lanicc.mrpc.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.ExecutionException;

/**
 * Created on 2022/7/11.
 *
 * @author lan
 */
public class NettyServer {
    private final ChannelFuture channelFuture;

    private final EventLoopGroup bossGroup;

    private final EventLoopGroup workerGroup;


    public NettyServer(ServerConfig config) {
        //设置一个线程池，用于处理网络事件。
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        //创建一个ServerBootstrap对象，用于配置服务器端网络参数。
        channelFuture =
                new ServerBootstrap()
                        .group(bossGroup, workerGroup)
                        //设置服务器端使用的SocketChannel类型为NioServerSocketChannel。
                        .channel(NioServerSocketChannel.class)
                        //设置日志处理器，用于记录网络事件。
                        .handler(new LoggingHandler(LogLevel.DEBUG))
                        //设置服务器端SocketChannel的处理器，用于处理客户端连接和消息发送接收。
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel channel) throws Exception {
                                channel.pipeline()
                                        .addLast(new LengthFieldBasedFrameDecoder(1024 * 1024 * 1024, 0, 4, 0, 4))
                                        .addLast(new LengthFieldPrepender(4))
                                        .addLast(new ProtoCodec(config.getSerializer()))
                                        // 添加服务器端处理器，用于处理客户端连接和消息发送接收。
                                        .addLast(new ServerHandler(config));
                            }
                        })
                        .bind(config.getServerSocketAddr());
    }

    public void start() {
        try {
            //等待连接完成，获取连接结果。
            channelFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
