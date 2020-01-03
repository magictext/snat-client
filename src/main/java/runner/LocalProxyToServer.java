package runner;

import config.ClientConfig;
import handler.ClientDispacher;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import util.Data;
import handler.ClientMassageDecoder;
import handler.ClientMessageEncode;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.apache.log4j.Logger;
import util.Mapper;
import util.Type;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class LocalProxyToServer implements Runnable{

    ClientConfig clientConfig;

    public  Channel toServerChannel = null;

    public LocalProxyToServer(ClientConfig clientConfig) {
        this.clientConfig=clientConfig;
    }

    public  BlockingDeque deque =new LinkedBlockingDeque();

    public Channel getToServerChannel() {
        return toServerChannel;
    }

    public void setToServerChannel(Channel toServerChannel) {
        this.toServerChannel = toServerChannel;
    }

    public BlockingDeque getDeque() {
        return deque;
    }

    public void setDeque(BlockingDeque deque) {
        this.deque = deque;
    }

    public void run() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                            .addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2))
                            .addLast(new LengthFieldPrepender(2))
                            .addLast(new ClientMassageDecoder())
                            .addLast(new ClientMessageEncode())
                            .addLast(new ClientDispacher())
                    ;
                }
            });
            //Start the client.
            final ChannelFuture f = b.connect(clientConfig.getServerAddress(),clientConfig.getPort());
            f.addListener(new FutureListener<Void>() {
                @Override
                public void operationComplete(Future<Void> future) throws Exception {
                    if (!f.isSuccess()) {
                        Logger.getLogger(this.getClass()).error("服务器连接失败......................");
                        System.exit(0);
                    }else {
                        Channel channel = f.channel();
                        LocalProxyRunner.toServerChannel=channel;
                        channel.writeAndFlush(new Data().setType(1).setB(Mapper.getJsonByte(clientConfig)));
                        Logger.getLogger(this.getClass()).debug("I have write a config");
                    }
                }
            });

//            try {
//                synchronized ("haha"){
//                    f = b.connect(serverAddress).sync();
//                    toServerChannel = f.channel();
//                    "haha".notifyAll();
//                    System.out.println("===================I have finished");
//                }
//            } catch (InterruptedException e) {
//                System.err.println("服务器连接失败......................");
//                e.printStackTrace();
//                System.exit(0);
//            }
//            while (true){
//                Data take = (Data)deque.take();
//                toServerChannel.writeAndFlush(take);
//                Logger.getLogger(this.getClass()).debug("write a message");
//            }
            //Wait until the connection is closed.
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            toServerChannel.closeFuture();
//            workerGroup.shutdownGracefully();
//        }

    }

}
