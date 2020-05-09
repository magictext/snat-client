package handler;

import map.UdpMapper;
import util.Data;
import config.ClientConfigFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import map.ClientChannelMap;
import org.apache.log4j.Logger;
import runner.LocalProxyToLocal;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

//此类客户端使用 用来分析服务端传来的数据
public class ClientDispacher extends SimpleChannelInboundHandler<Data> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Data msg) throws Exception {
        switch (msg.type){
            case 200:
                Channel channel= ClientChannelMap.map.get(msg.session);
                if (channel == null) {
                    InetSocketAddress key = (InetSocketAddress) ClientConfigFactory.getMap().getKey(msg.port);
                    Thread thread = new Thread(new LocalProxyToLocal(key, msg.session));
                    thread.start();
                    thread.join();
                }
                Logger.getLogger(this.getClass()).debug(new String(msg.getB()));
                ClientChannelMap.map.get(msg.session).writeAndFlush(msg.getB());
                break;

            case 201:
                DatagramSocket datagramSocket = UdpMapper.map.get(msg.session);
                if (datagramSocket == null) {
                    InetSocketAddress key = (InetSocketAddress) ClientConfigFactory.getMap().getKey(msg.port);
                    datagramSocket = new DatagramSocket();
                    datagramSocket.connect(key);
                    UdpMapper.map.put(msg.session, datagramSocket);
                }
                Logger.getLogger(this.getClass()).debug(new String(msg.getB()));
                DatagramPacket packet=new DatagramPacket(msg.getB(), msg.b.length);
                datagramSocket.send(packet);
                break;
                default :
                    Logger.getLogger(this.getClass()).warn(msg);

        }
    }
}
