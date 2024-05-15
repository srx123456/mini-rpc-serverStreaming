package io.github.lanicc;

import io.github.lanicc.mrpc.stream.StreamObserver;
import io.github.lanicc.mrpc.test.api.User;
import io.github.lanicc.mrpc.test.api.UserApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2022/7/11.
 *
 * @author lan
 */
public class UserApiImpl implements UserApi {

    static Logger logger = LoggerFactory.getLogger(UserApiImpl.class);

    final Map<String, User> users = new ConcurrentHashMap<>(4);

    @Override
    public void add(User user) {
        logger.info("add user: {}", user);
        users.put(user.getId(), user);
    }

    @Override
    public User findById(String id) {
        logger.info("find user: {}", id);
        return users.get(id);
    }

    @Override
    public List<User> list() {
        logger.info("list all user: {}", users);
        return new ArrayList<>(users.values());
    }

    // 迭代所有用户，并将每个用户通过userStreamObserver发送给客户端。
    @Override
    public void iterate(StreamObserver<User> userStreamObserver) {
        logger.info("iterate all user: {}", userStreamObserver.getClass());

        try {
            for (User user : users.values()) {
                // 调用userStreamObserver.onNext(user)方法，将当前用户发送给客户端。
                userStreamObserver.onNext(user);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            // 调用userStreamObserver.onCompleted()方法，通知客户端迭代完成。
            userStreamObserver.onCompleted();
        }
    }
}
