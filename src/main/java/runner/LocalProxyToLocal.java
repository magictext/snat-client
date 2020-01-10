package runner;

import handler.CReadbytesAndSend;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.ssl.SslHandler;
import lombok.Data;
import map.ClientChannelMap;
import org.apache.log4j.Logger;
import util.SslClientFactory;

import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;

@Data
public class LocalProxyToLocal implements Runnable {

    private int session;

    InetSocketAddress localAddress;

    boolean ssl=false;

    static EventLoopGroup workerGroup = new NioEventLoopGroup();

    public LocalProxyToLocal(InetSocketAddress i, int session) {
        localAddress=i;
        this.session=session;
    }

    public LocalProxyToLocal(InetSocketAddress i, int session,boolean ssl) {
        localAddress=i;
        this.session=session;
        this.ssl=ssl;
    }

    public void run() {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                            .addLast(new ByteArrayEncoder())
                            .addLast(new CReadbytesAndSend(LocalProxyRunner.toServerChannel,session));
                    if (ssl){
                        ch.pipeline().addFirst(new SslHandler(SslClientFactory.prepareEngine(localAddress.getHostName(),localAddress.getPort() )));
                    }
                }
            });
            //Start the client.
            ChannelFuture f = null; // (5)
            try {
                    f = b.connect(localAddress).sync();
                    ClientChannelMap.map.put(session, f.channel());
                    System.out.println("===================I have finished");
            } catch (InterruptedException e) {
                Logger.getLogger(this.getClass()).error("本地服务器连接失败");
                e.printStackTrace();
                System.exit(0);
            }
        }
}