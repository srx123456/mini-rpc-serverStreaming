package io.github.lanicc.mrpc.serialization.fastjson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.github.lanicc.mrpc.serialization.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 2022/7/11.
 *
 * @author lan
 */
public class FastJsonSerializer implements Serializer {
    @Override
    public void write(Object o, OutputStream out) {
        try {
            // 调用FastJson的writeJSONString方法将对象o序列化为JSON字符串（序列化时写入类名信息。）
            JSON.writeJSONString(out, o, SerializerFeature.WriteClassName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T read(Class<T> clazz, InputStream in) {
        try {
            //支持自动类型识别。
            return JSON.parseObject(in, clazz, Feature.SupportAutoType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
