package io.github.lanicc.mrpc.serialization;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 2022/7/9.
 *
 * @author lan
 */
public interface Serializer {

    //将对象序列化后写入输出流中。
    void write(Object o, OutputStream out);

    //从输入流中读取对象，Class<T> clazz 是要反序列化的目标类型
    <T> T read(Class<T> clazz, InputStream in);

}
