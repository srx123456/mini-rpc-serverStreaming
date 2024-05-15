package io.github.lanicc.mrpc;

import io.github.lanicc.mrpc.remote.NettyClient;
import net.sf.cglib.proxy.Enhancer;

import java.util.Objects;

/**
 * Created on 2022/7/9.
 *
 * @author lan
 */
public class ProxyFactory {

    private final NettyClient nettyClient;

    public ProxyFactory(NettyClient nettyClient) {
        this.nettyClient = Objects.requireNonNull(nettyClient);
    }

    @SuppressWarnings("unchecked")
    //创建一个动态代理对象
    public <T> T newProxy(Class<T> inf) {
        // Enhancer 对象，用于创建动态代理对象。
        Enhancer enhancer = new Enhancer();
        // 将传入的接口类 inf 设置为动态代理对象的接口。
        enhancer.setInterfaces(new Class[]{inf});
        // 将 Invoker 对象设置为动态代理对象的回调方法。Invoker 是一个自定义的类，用于处理代理方法的调用。
        enhancer.setCallback(new Invoker(inf, nettyClient));
        // 创建动态代理对象，并将其强制转换为泛型类型 T，然后返回。
        return (T) enhancer.create();
    }
}
