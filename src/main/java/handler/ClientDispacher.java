package handler;

import util.Data;
import config.ClientConfigFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import map.ClientChannelMap;
import org.apache.log4j.Logger;
import runner.LocalProxyToLocal;

//此类客户端使用 用来分析服务端传来的数据
public class ClientDispacher extends SimpleChannelInboundHandler<Data> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Data msg) throws Exception {
        switch (msg.type){
            case 200:
                Channel channel = ClientChannelMap.map.get(msg.session);
                if (channel == null) {
                    new Thread(new LocalProxyToLocal((Integer) ClientConfigFactory.getMap().getKey(msg.getPort()),msg.session)).start();
                    while (true){
                        synchronized ("local"){
                            if (ClientChannelMap.map.get(msg.session) == null) {
                                "local".wait();
                            }else {
                                break;
                            }
                        }
                    }
                }
                Logger.getLogger(this.getClass()).debug(new String(msg.getB()));
                ClientChannelMap.map.get(msg.session).writeAndFlush(msg.getB());
                break;
                default :
                    Logger.getLogger(this.getClass()).warn(msg);
        }
    }
}
