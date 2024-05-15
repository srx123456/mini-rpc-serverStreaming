package io.github.lanicc.mrpc.serialization.fastjson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created on 2022/7/11.
 * 使用FastJson序列化和反序列化对象。
 * @author lan
 */
class FastJsonSerializerTest {

    @Test
    void write() throws IOException {
        FastJsonSerializer serializer = new FastJsonSerializer();
        //分配ByteBuf。
        ByteBufAllocator allocator = new UnpooledByteBufAllocator(true);
        ByteBuf buffer = allocator.buffer(10240);
        //创建ByteBufOutputStream对象，将ByteBuf作为输出流的目标。
        ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        User u = new User("1 ", 12);
        //使用FastJsonSerializer将User对象u写入ByteBufOutputStream中。
        serializer.write(u, out);
        out.flush();
        out.close();
        //创建ByteBufInputStream对象，将之前写入的ByteBuf作为输入流的源。
        ByteBufInputStream in = new ByteBufInputStream(buffer);
        //使用FastJsonSerializer从ByteBufInputStream中读取User对象。
        User user = serializer.read(User.class, in);
        Assertions.assertEquals(u.id, user.id);
        Assertions.assertEquals(u.age, user.age);
        //使用FastJson将一个包含一个U对象、一个Object对象和一个User对象的列表转换为JSON字符串，并打印输出。
        /*
        [
            {
                "@type":"io.github.lanicc.mrpc.serialization.fastjson.FastJsonSerializerTest$U",
                "age":0
            },
            {},
            {
                "@type":"io.github.lanicc.mrpc.serialization.fastjson.FastJsonSerializerTest$User",
                "age":0
            }
        ]
         */
        System.out.println(JSON.toJSONString(Arrays.asList(new U(), new Object(), new User()), SerializerFeature.WriteClassName, SerializerFeature.PrettyFormat));

        String string = JSON.toJSONString(u);


    }

    static class U extends User {

    }
    static class User {

        private String id;

        private int age;

        public User() {
        }

        public User(String id, int age) {
            this.id = id;
            this.age = age;
        }

        public String getId() {
            return id;
        }

        public User setId(String id) {
            this.id = id;
            return this;
        }

        public int getAge() {
            return age;
        }

        public User setAge(int age) {
            this.age = age;
            return this;
        }
    }

}
