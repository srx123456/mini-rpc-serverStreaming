package io.github.lanicc;

import io.github.lanicc.mrpc.stream.StreamObserver;
import io.github.lanicc.mrpc.test.api.LocalRemoteMapper;
import io.github.lanicc.mrpc.test.api.LocalRemoteMapperAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author suoruixiang
 * @date 2024-05-13
 */
public class LocalRemoteMapperAPIImpl implements LocalRemoteMapperAPI {
    static Logger logger = LoggerFactory.getLogger(LocalRemoteMapperAPIImpl.class);
    final List<LocalRemoteMapper> localRemoteMappers = new CopyOnWriteArrayList<>();

    @Override
    public void add(LocalRemoteMapper localRemoteMapper) {
        logger.info("add localRemoteMapper： {}", localRemoteMapper);
        localRemoteMappers.add(localRemoteMapper);
    }

    @Override
    public List<LocalRemoteMapper> list() {
        logger.info("list localRemoteMapper: {}",localRemoteMappers);
        return new ArrayList<>(localRemoteMappers);
    }

    @Override
    public void iterate(StreamObserver<LocalRemoteMapper> mapperStreamObserver){
        logger.info("iterate all localRemoteMappers : {} ", mapperStreamObserver.getClass());

        try {
            for (LocalRemoteMapper mapper : localRemoteMappers) {
                // 调用userStreamObserver.onNext(mapper)方法，将当前用户发送给客户端。
                mapperStreamObserver.onNext(mapper);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            // 调用userStreamObserver.onCompleted()方法，通知客户端迭代完成。
            mapperStreamObserver.onCompleted();
        }
    }
}
