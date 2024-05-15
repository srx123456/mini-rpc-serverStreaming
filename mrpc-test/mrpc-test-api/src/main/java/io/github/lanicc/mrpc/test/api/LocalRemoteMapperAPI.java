package io.github.lanicc.mrpc.test.api;

import io.github.lanicc.mrpc.stream.StreamObserver;

import java.util.List;

/**
 * @author suoruixiang
 * @date 2024-05-13
 */
public interface LocalRemoteMapperAPI {
    void add(LocalRemoteMapper localRemoteMapper);

    List<LocalRemoteMapper> list();

    void iterate(StreamObserver<LocalRemoteMapper> mapperStreamObserver);
}
