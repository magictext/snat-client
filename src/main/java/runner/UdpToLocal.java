package runner;

import handler.CReadbytesAndSend;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import map.ClientChannelMap;
import map.UdpMapper;
import util.Type;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class UdpToLocal extends Thread{

    private int session;

    InetSocketAddress localAddress;

    static EventLoopGroup workerGroup = new NioEventLoopGroup();

    public UdpToLocal(InetSocketAddress i, int session) {
        localAddress=i;
        this.session=session;

    }
    @Override
    public void run() {
        //通过NioDatagramChannel创建Channel，并设置Socket参数支持广播
        //UDP相对于TCP不需要在客户端和服务端建立实际的连接，因此不需要为连接（ChannelPipeline）设置handler
//        Bootstrap b=new Bootstrap();
//        b.group(workerGroup)
//                .channel(NioDatagramChannel.class)
//                .option(ChannelOption.SO_BROADCAST, true)
//                .handler(new ChannelInitializer<SocketChannel>() { // (4)
//                    @Override
//                    public void initChannel(SocketChannel ch) throws Exception {
//                        ch.pipeline()
//                                .addLast(new ByteArrayEncoder())
//                                .addLast(new CReadbytesAndSend(LocalProxyRunner.toServerChannel,session,Type.udpinfo)); //处理器
//                    }
//                });
//        Channel channel = b.bind(0).channel();
//        ClientChannelMap.map.put(localAddress.getPort(), channel);
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.connect(localAddress);
            UdpMapper.map.put(session, datagramSocket);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
