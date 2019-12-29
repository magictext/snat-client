package handler;

import util.ChannelHashcode;
import util.Data;
import util.Type;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import map.ClientChannelMap;
import org.apache.log4j.Logger;

@AllArgsConstructor
public class CReadbytesAndSend extends SimpleChannelInboundHandler {

    private Channel channel;

    private int session;


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Logger.getLogger(this.getClass()).debug("start a connect to  "+ctx.channel().localAddress());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ClientChannelMap.map.remove(session);
        ctx.close().sync();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf mes = null;
        if (msg instanceof ByteBuf) {
            mes = (ByteBuf) msg;
            byte b[] = new byte[mes.readableBytes()];
            mes.readBytes(b);
            Logger.getLogger(this.getClass()).debug(new String(b));
            channel.writeAndFlush(new Data().setType(Type.date).setSession(ChannelHashcode.getChannelHashcode(ctx)).setSession(session).setB(b));
        }
    }
}
