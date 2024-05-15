package io.github.lanicc.mrpc;

import io.github.lanicc.mrpc.remote.NettyClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created on 2022/7/9.
 *
 * @author lan
 */
public class ClientBootstrap extends AbstractBootstrap {

    private Map<Class<?>, Object> references;

    private NettyClient nettyClient;

    private ProxyFactory proxyFactory;

    public ClientBootstrap(Config config) {
        super(config);
    }

    @Override
    public void doInit() {
        //初始化客户端
        //调用ClientHandler
        this.nettyClient = new NettyClient(config);
        //初始化代理工厂
        this.proxyFactory = new ProxyFactory(this.nettyClient);
        //初始化引用 Class<?>, Object
        initReference();
    }
    @Override
    public void doStart() {
        nettyClient.start();
    }

    @Override
    public void doStop() {
        nettyClient.shutdown();
    }

    private void initReference() {
        //存储引用的类和对应的代理对象。
        Map<Class<?>, Object> referencesTmp = new HashMap<>();

        //通过 config.getRefClasses() 方法获取配置中的引用类列表 refClasses。
        List<Class<?>> refClasses = config.getRefClasses();
        for (Class<?> refClass : refClasses) {
            Object proxy = proxyFactory.newProxy(refClass);
            referencesTmp.put(refClass, proxy);
        }

        this.references = Collections.unmodifiableMap(referencesTmp);
    }

    @SuppressWarnings("unchecked")
    public <T> T getReference(Class<T> clazz) {
        checkRunning();
        //获取引用的对象 Map<Class<?>, Object>
        Object o = references.get(clazz);
        if (Objects.isNull(o)) {
            throw new NullPointerException("no such service: " + clazz);
        }
        return (T) o;
    }


}
