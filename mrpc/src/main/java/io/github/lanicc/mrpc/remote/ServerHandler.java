package io.github.lanicc.mrpc.remote;

import com.alibaba.fastjson.JSON;
import io.github.lanicc.mrpc.ServerConfig;
import io.github.lanicc.mrpc.ServiceInvoker;
import io.github.lanicc.mrpc.remote.proto.Protocol;
import io.github.lanicc.mrpc.remote.proto.Request;
import io.github.lanicc.mrpc.remote.proto.Response;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Created on 2022/7/11.
 *
 * @author lan
 */
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<Protocol> {

    static Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private final ServiceInvoker invoker;

    public ServerHandler(ServerConfig config) {
        this.invoker = new ServiceInvoker(config);
    }

    //SimpleChannelInboundHandler是Netty提供的一个抽象类，用于处理入站消息。
    // 它提供了一个模板方法channelRead0()，用于处理接收到的消息。
    //在ServerHandler类中，重写了channelRead0()方法，用于处理接收到的Protocol类型的消息。
    // 当有消息到达时，Netty会自动调用channelRead0()方法，并将接收到的消息作为参数传递给该方法。
    //所以，实际上是Netty框架在适当的时机调用了channelRead0()方法
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Protocol protocol) throws Exception {
        // DEBUG io.github.lanicc.mrpc.remote.ServerHandler - Request{clazz=interface io.github.lanicc.mrpc.test.api.LocalRemoteMapperAPI
        // , method='add', stream=false, requestId=2, data=[{"local":"192.168.2.1:4744","remote":"10.10.10.123:701"}]}
        logger.debug(protocol.getData().toString());
        //System.out.println(Objects.nonNull(JSON.parseArray(protocol.getData().toString()).getJSONObject(0)));
        if(Objects.nonNull(JSON.parseArray(protocol.getData().toString()).getJSONObject(0))){
            System.out.println(protocol.getRequestId() + " =====>>>>>> local " + JSON.parseArray(protocol.getData().toString()).getJSONObject(0).getString("local"));
            System.out.println(protocol.getRequestId() + " =====>>>>>> remote " + JSON.parseArray(protocol.getData().toString()).getJSONObject(0).getString("remote"));
        }

        // 请求类型的协议，则调用processRequest方法进行处理
        if (protocol instanceof Request) {
            processRequest((Request) protocol, ctx);
        } else {
            logger.warn("receive unexpected message: {}", protocol);
        }
    }

    //处理客户端发送的请求。
    private void processRequest(Request request, ChannelHandlerContext ctx) {
        try {
            //调用服务方法，并将请求对象和通道对象作为参数传递。
            Object result = invoker.invoke(request, ctx.channel());
            //设置其 requestId 为请求的 requestId，将返回结果 result 设置为 Response 的数据。
            Response response = new Response();
            response.setRequestId(request.getRequestId());
            response.setData(result);
            // 将响应对象写入通道并刷新，以便客户端收到响应结果。
            ctx.channel().writeAndFlush(response);
        } catch (Exception e) {
            logger.error("invoke service error", e);
        }
    }

}
