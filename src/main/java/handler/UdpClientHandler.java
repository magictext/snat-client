package handler;


import java.io.File;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import extend.UDPPacketHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.apache.log4j.Logger;

public class UdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {


    UDPPacketHandler handler = null;

    public UdpClientHandler() throws MalformedURLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        File file = new File(System.getProperty("user.dir") +File.separatorChar+ "/classes/");
        System.out.println(file.getPath());
        Map<File,String> resFile = new HashMap();
        getChildren(file, resFile,"");
        Logger.getLogger("loadClass").debug(resFile);
        URL[] arr = {file.toURL()};
        URLClassLoader urlClassLoader = new URLClassLoader(arr);
        for (Map.Entry<File, String> entry : resFile.entrySet()) {
            Class<?> aClass = urlClassLoader.loadClass(entry.getValue() + "." + entry.getKey().getName().split(".class")[0]);
            Class<?>[] interfaces = aClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                if (interfaces[i] == UDPPacketHandler.class){
                    handler = (UDPPacketHandler) aClass.newInstance();
                }
                break;
            }
            if (handler!=null) break;
        }
        System.out.println(handler);
    }

    private void getChildren(File file, Map resFile, String path){
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                String name = files[i].getName();
                if (name.endsWith(".class")) {
                    resFile.put(files[i],path);
                }
            }
            if (files[i].isDirectory()) {
                if (path.equals("")){
                    getChildren(files[i],resFile ,path+files[i].getName());
                }else {
                    getChildren(files[i],resFile ,path+"."+files[i].getName());
                }

            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet)
            throws Exception {
        //服务器推送对方IP和PORT
        ByteBuf buf = (ByteBuf) packet.copy().content();
        InetSocketAddress sender = packet.sender();
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String str = new String(req, "UTF-8");
        System.out.println(str);
        String[] list = str.split(" ");
        //如果是A 则发送
        if (list[0].equals("A")) {
            String ip = list[1];
            String port = list[2];
            ctx.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer("打洞信息".getBytes()), new InetSocketAddress(ip, Integer.parseInt(port))));
//            Thread.sleep(1000);
//            ctx.writeAndFlush(new DatagramPacket(
//                    Unpooled.copiedBuffer("P2P info..".getBytes()), new InetSocketAddress(ip, Integer.parseInt(port))));
        }else {
            ctx.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer(handler.handler(req)),sender));
            System.out.println("接收到的信息:" + str);
        }

    }

//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("客户端向服务器发送自己的IP和PORT");
//        ctx.writeAndFlush(new DatagramPacket(
//                Unpooled.copiedBuffer("L".getBytes()), new InetSocketAddress("183.1.1.1", 7402)));
//        super.channelActive(ctx);
//    }
}