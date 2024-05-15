package io.github.lanicc.mrpc.remote;

import io.github.lanicc.mrpc.remote.proto.Protocol;
import io.github.lanicc.mrpc.remote.proto.Request;
import io.github.lanicc.mrpc.stream.StreamObserver;
import io.github.lanicc.mrpc.stream.StreamResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.PlatformDependent;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2022/7/11.
 *
 * @author lan
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ClientHandler extends SimpleChannelInboundHandler<Protocol> {

    private final Map<Long, CompletableFuture> completableFutureMap;
    private final Map<Long, StreamObserver> streamObserverMap;

    private final Semaphore throttle;

    private final int maxWaitSize;

    public ClientHandler(int maxWaitSize) {
        this.maxWaitSize = maxWaitSize;
        this.completableFutureMap = PlatformDependent.newConcurrentHashMap(maxWaitSize);
        this.throttle = new Semaphore(maxWaitSize);
        this.streamObserverMap = new ConcurrentHashMap<>();
    }

    public <T> CompletableFuture<T> future(long requestId) throws InterruptedException {
        boolean b = throttle.tryAcquire(1, TimeUnit.SECONDS);
        if (!b) {
            throw new RuntimeException("too many waiting requests");
        }
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        completableFuture.whenComplete((t, throwable) -> remove(requestId));
        CompletableFuture<T> future = completableFutureMap.putIfAbsent(requestId, completableFuture);
        return Objects.isNull(future) ? completableFuture : future;
    }

    public void stream(Request request) {
        streamObserverMap.put(request.getRequestId(), request.getStreamObserver());
    }

    public void remove(long id) {
        throttle.release();
        completableFutureMap.remove(id);
    }

    public int getProcessingRequestCount() {
        return maxWaitSize - throttle.availablePermits();
    }

    //处理从服务器返回的消息。
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Protocol protocol) throws Exception {
        //是StreamResponseMessage类型，则调用processStream方法进行处理。
        if (protocol instanceof StreamResponseMessage) {
            processStream((StreamResponseMessage) protocol);
            return;
        }

        long requestId = protocol.getRequestId();
        //根据requestId从completableFutureMap中获取对应的CompletableFuture对象。
        CompletableFuture future = completableFutureMap.remove(requestId);
        //接收到的消息作为结果完成该CompletableFuture对象。
        future.complete(protocol);
        throttle.release();
    }

    private void processStream(StreamResponseMessage streamResponseMessage) {
        //根据streamResponseMessage中的requestId从streamObserverMap中获取对应的StreamObserver对象。
        StreamObserver observer = streamObserverMap.get(streamResponseMessage.getRequestId());
        if (!streamResponseMessage.isComplete()) {
            //将消息数据传递给StreamObserver对象的onNext()方法进行处理。
            observer.onNext(streamResponseMessage.getData());
        } else {
            //streamObserverMap中移除对应的requestId，
            // 然后调用observer.onCompleted()方法通知StreamObserver对象流式处理已完成。
            streamObserverMap.remove(streamResponseMessage.getRequestId());
            observer.onCompleted();
        }
    }
}
