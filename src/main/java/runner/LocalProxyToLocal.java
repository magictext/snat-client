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
import lombok.Data;
import map.ClientChannelMap;
import org.apache.log4j.Logger;

@Data
public class LocalProxyToLocal implements Runnable {

    private int port;

    private int session;

    static EventLoopGroup workerGroup = new NioEventLoopGroup();

    public LocalProxyToLocal(int i,int session) {
        port=i;
        this.session=session;

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
                }
            });
            //Start the client.
            ChannelFuture f = null; // (5)
            try {
                synchronized ("local"){
                    f = b.connect("47.100.56.65",port).sync();
                    ClientChannelMap.map.put(session, f.channel());
                    "local".notifyAll();
                    System.out.println("===================I have finished");
                }
            } catch (InterruptedException e) {
                Logger.getLogger(this.getClass()).error("本地服务器连接失败");
                e.printStackTrace();
                System.exit(0);
            }
        }
}