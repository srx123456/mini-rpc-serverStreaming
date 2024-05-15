package io.github.lanicc;

import io.github.lanicc.mrpc.Config;
import io.github.lanicc.mrpc.ServerBootstrap;
import io.github.lanicc.mrpc.ServerConfig;
import io.github.lanicc.mrpc.serialization.fastjson.FastJsonSerializer;
import io.github.lanicc.mrpc.test.api.LocalRemoteMapperAPI;
import io.github.lanicc.mrpc.test.api.UserApi;

import java.net.InetSocketAddress;
import java.util.Collections;

/**
 * Created on ${DATE}.
 * 服务器端流式RPC
 * 服务器端代码。
 * 它创建了一个服务提供者，用于提供用户相关的API服务。
 * 通过配置Config和ServerConfig对象，将UserApi接口的实现类UserApiImpl暴露为服务，并启动服务。
 * @author lan
 */
public class Provider {
    public static void main(String[] args) {
        //创建一个Config对象，用于配置RPC服务。
        Config config =
                new Config()
                        .setRefClasses(Collections.singletonList(LocalRemoteMapperAPI.class))
                        // 使用FastJson作为序列化器
                        .setSerializer(new FastJsonSerializer())
                        // 设置服务器的Socket地址
                        .setServerSocketAddr(new InetSocketAddress("127.0.0.1", 8091));
        // 复制一个ServerConfig对象，用于配置服务端。
        ServerConfig serverConfig = ServerConfig.copy(config);
        // 配置服务端，将UserApi接口的实现类UserApiImpl注册到服务端。
        // 决定当前的rpc系统为哪个对象服务。
        serverConfig.exportService(LocalRemoteMapperAPI.class, new LocalRemoteMapperAPIImpl());

        ServerBootstrap serverBootstrap = new ServerBootstrap(serverConfig);
        // 调用doInit函数，然后实现NettyServer，最后调用ServerHandler方法。
        serverBootstrap.init();
        serverBootstrap.start();

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> serverBootstrap.stop());

    }

//    public static void main(String[] args) {
//        //创建一个Config对象，用于配置RPC服务。
//        Config config =
//                new Config()
//                        .setRefClasses(Collections.singletonList(UserApi.class))
//                        // 使用FastJson作为序列化器
//                        .setSerializer(new FastJsonSerializer())
//                        // 设置服务器的Socket地址
//                        .setServerSocketAddr(new InetSocketAddress("127.0.0.1", 8091));
//        // 复制一个ServerConfig对象，用于配置服务端。
//        ServerConfig serverConfig = ServerConfig.copy(config);
//        // 配置服务端，将UserApi接口的实现类UserApiImpl注册到服务端。
//        // 决定当前的rpc系统为哪个对象服务。
//        serverConfig.exportService(UserApi.class, new UserApiImpl());
//
//        ServerBootstrap serverBootstrap = new ServerBootstrap(serverConfig);
//        serverBootstrap.init();
//        serverBootstrap.start();
//
//        Thread.setDefaultUncaughtExceptionHandler((t, e) -> serverBootstrap.stop());
//
//    }
}
