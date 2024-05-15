package io.github.lanicc;

import io.github.lanicc.mrpc.ClientBootstrap;
import io.github.lanicc.mrpc.Config;
import io.github.lanicc.mrpc.serialization.fastjson.FastJsonSerializer;
import io.github.lanicc.mrpc.stream.StreamObserver;
import io.github.lanicc.mrpc.test.api.LocalRemoteMapper;
import io.github.lanicc.mrpc.test.api.LocalRemoteMapperAPI;
import io.github.lanicc.mrpc.test.api.User;
import io.github.lanicc.mrpc.test.api.UserApi;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created on ${DATE}.
 *
 * @author lan
 */
public class Consumer {
    public static void main(String[] args) throws InterruptedException {
        // 创建一个Config对象，用于配置RPC服务。
        Config config =
                new Config()
                        // 设置RPC服务的引用类，这里只设置了一个UserApi类。
                        .setRefClasses(Collections.singletonList(LocalRemoteMapperAPI.class))
                        // 设置RPC服务的序列化器，这里使用FastJsonSerializer作为序列化器。
                        .setSerializer(new FastJsonSerializer())
                        // 设置RPC服务的服务器地址，这里设置为"127.0.0.1:8091"。
                        .setServerSocketAddr(new InetSocketAddress("127.0.0.1", 8091));


        ClientBootstrap clientBootstrap = new ClientBootstrap(config);
        //调用mrpc里的函数
        clientBootstrap.init();
        //启动客户端
        clientBootstrap.start();
        // rpc的实际业务。
        // 该代理对象mapperAPI实现了LocalRemoteMapperAPI接口，并且可以通过网络调用远程的RPC服务。
        // 通过这个代理对象，你可以像调用本地方法一样调用LocalRemoteMapperAPI接口中定义的方法，
        // 实际上这些方法的执行是通过网络请求远程的RPC服务来完成的。代理对象会将你的方法调用转化为网络请求，并将结果返回给你。
        LocalRemoteMapperAPI mapperAPI = clientBootstrap.getReference(LocalRemoteMapperAPI.class);

        int userNum = 0;

        List<LocalRemoteMapper> mappers = new ArrayList<>();
        mappers.add(new LocalRemoteMapper("192.168.2."+new Random().nextInt(256)+":"+new Random().nextInt(10000), "10."+new Random().nextInt(255)+".10.123:" + new Random().nextInt(1000)));
        mappers.add(new LocalRemoteMapper("192.168.2."+new Random().nextInt(256)+":"+new Random().nextInt(10000), "10."+new Random().nextInt(255)+".10.123:" + new Random().nextInt(1000)));
        mappers.add(new LocalRemoteMapper("192.168.2."+new Random().nextInt(256)+":"+new Random().nextInt(10000), "10."+new Random().nextInt(255)+".10.123:" + new Random().nextInt(1000)));
        mappers.add(new LocalRemoteMapper("192.168.2."+new Random().nextInt(256)+":"+new Random().nextInt(10000), "10."+new Random().nextInt(255)+".10.123:" + new Random().nextInt(1000)));

        // 向UserApi接口发送add方法的请求，添加用户
        for (LocalRemoteMapper map: mappers) {
//            mapperAPI.add(new LocalRemoteMapper("192.168.2.1:" + new Random().nextInt(10000), "10.10.10.123:" + new Random().nextInt(1000)));
            mapperAPI.add(map);
            userNum++;
        }
        CountDownLatch stopWatch = new CountDownLatch(userNum);

        // 向UserApi接口发送iterate方法的请求，并传入一个StreamObserver对象作为回调。
        mapperAPI.iterate(new StreamObserver<LocalRemoteMapper>() {
            @Override
            public void onNext(LocalRemoteMapper mapper) {
                System.out.println("onNext: " + mapper);
                stopWatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("onCompleted");
            }
        });

        System.out.println("stream request called");
        // 等待所有请求完成
        // 保证所有的发送线程优先于主线程执行
        stopWatch.await();
        System.out.println("stream request completed");
        // 关闭客户端，释放资源。
        clientBootstrap.stop();
        // 设置默认的未捕获异常处理程序，当线程发生未捕获异常时，调用clientBootstrap的stop方法。
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> clientBootstrap.stop());
    }

//    public static void main(String[] args) throws InterruptedException {
//        // 创建一个Config对象，用于配置RPC服务。
//        Config config =
//                new Config()
//                        // 设置RPC服务的引用类，这里只设置了一个UserApi类。
//                        .setRefClasses(Collections.singletonList(UserApi.class))
//                        // 设置RPC服务的序列化器，这里使用FastJsonSerializer作为序列化器。
//                        .setSerializer(new FastJsonSerializer())
//                        // 设置RPC服务的服务器地址，这里设置为"127.0.0.1:8091"。
//                        .setServerSocketAddr(new InetSocketAddress("127.0.0.1", 8091));
//
//
//        ClientBootstrap clientBootstrap = new ClientBootstrap(config);
//        clientBootstrap.init();
//        //启动客户端
//        clientBootstrap.start();
//        // rpc的实际业务。
//        //获取UserApi的引用
//        UserApi userApi = clientBootstrap.getReference(UserApi.class);
//
//        int userNum = 10;
//
//        CountDownLatch stopWatch = new CountDownLatch(userNum);
//
//        // 向UserApi接口发送add方法的请求，添加10个用户
//        for (int i = 0; i < userNum; i++) {
//            userApi.add(new User(i + "", "lanicc_" + i, new Date()));
//        }
//        //User user = userApi.findById("1");
//        //System.out.println(user);
//        //List<User> users = userApi.list();
//        //System.out.println(users);
//
//        // 向UserApi接口发送iterate方法的请求，并传入一个StreamObserver对象作为回调。
//        userApi.iterate(new StreamObserver<User>() {
//            @Override
//            public void onNext(User user) {
//                System.out.println("onNext: " + user);
//                stopWatch.countDown();
//            }
//
//            @Override
//            public void onCompleted() {
//                System.out.println("onCompleted");
//            }
//        });
//
//        System.out.println("stream request called");
//
//        stopWatch.await();
//        // 关闭客户端，释放资源。
//        clientBootstrap.stop();
//        // 设置默认的未捕获异常处理程序，当线程发生未捕获异常时，调用clientBootstrap的stop方法。
//        Thread.setDefaultUncaughtExceptionHandler((t, e) -> clientBootstrap.stop());
//    }
}
