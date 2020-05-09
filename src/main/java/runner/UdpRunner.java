package runner;


import handler.UdpClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import util.Mapper;
import util.UdpData;

import java.net.InetSocketAddress;
import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 模拟P2P客户端
 * @author
 *
 */
public class UdpRunner extends Thread{

    private int port;

    public UdpRunner(int port){
        this.port = port;
    }

    public  void run() {
        Bootstrap b = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new UdpClientHandler());
            final ChannelFuture f = b.bind(0).sync();
            Timer timer = new Timer();
            System.out.println("timer run");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    UdpData udpData = new UdpData("server", LocalProxyRunner.config.getName(), LocalProxyRunner.config.getIpv6LocalAddress(), ((InetSocketAddress) f.channel().localAddress()).getPort());
                    String jsonString = Mapper.getJsonString(udpData);
                    f.channel().writeAndFlush(new DatagramPacket(
                            Unpooled.copiedBuffer(jsonString.getBytes()), new InetSocketAddress(LocalProxyRunner.config.getServerAddress(), LocalProxyRunner.config.getUdpport())));
//                    System.out.println("timer run");
                }
            }, 2000,5000);
            f.channel().closeFuture().await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            group.shutdownGracefully();
        }
    }
}