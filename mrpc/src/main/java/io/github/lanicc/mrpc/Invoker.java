package io.github.lanicc.mrpc;

import io.github.lanicc.mrpc.remote.NettyClient;
import io.github.lanicc.mrpc.remote.proto.Request;
import io.github.lanicc.mrpc.remote.proto.Response;
import io.github.lanicc.mrpc.stream.StreamObserver;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2022/7/9.
 *
 * @author lan
 */
public class Invoker implements MethodInterceptor {

    private final Class<?> clazz;

    private final NettyClient nettyClient;

    public Invoker(Class<?> clazz, NettyClient nettyClient) {
        this.clazz = clazz;
        this.nettyClient = nettyClient;
    }

    //一个方法拦截器的实现，用于拦截指定类的方法调用
    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        if (Objects.equals(method.getDeclaringClass(), clazz)) {
            Request request = initRequest(new Request(), method, args);
            //如果请求对象不是流式调用，则通过网络客户端发送异步请求，并等待3秒钟获取响应结果，然后返回响应数据。
            if (!request.isStream()) {
                CompletableFuture<Response> future = nettyClient.requestAsync(request);
                return future.get(3, TimeUnit.SECONDS).getData();
            }
            nettyClient.streamRequest(request);
            return null;
        }

        throw new UnsupportedOperationException(method.getName());
    }

    //初始化一个请求对象
    private Request initRequest(Request request, Method method, Object[] args) {
        request.setMethod(method.getName());
        request.setClazz(clazz);
        if (Objects.nonNull(args) && args.length > 0) {
            // 仅支持第一个参数为StreamObserver的流式调用
            if (args[0] instanceof StreamObserver) {
                request.setStreamObserver((StreamObserver<?>) args[0]);
                request.setStream(true);
            }
            // 请求对象是流式调用
            if (request.isStream()) {
                Object[] args1 = new Object[args.length];
                System.arraycopy(args, 0, args1, 0, args.length);
                args1[0] = null;
                request.setData(args1);
            // 如果请求对象不是流式调用，则设置请求数据为args
            } else {
                request.setData(args);
            }
        }

        return request;
    }
}
