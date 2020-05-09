package runner;

import com.fasterxml.jackson.core.JsonProcessingException;

import config.ClientConfig;
import config.ClientConfigFactory;
import config.ConfigEntity;
import exception.RangePortException;
import io.netty.channel.Channel;
import util.IpUtil;
import util.Mapper;
import util.RangePort;
import util.Type;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.BlockingDeque;

public class LocalProxyRunner {

	public static Channel toServerChannel;

	public  static ClientConfig config;

	public static void main(String[] args) throws InterruptedException, RangePortException, JsonProcessingException {
		if(args.length==0){
			config = Mapper.parseObject(new File("./target/classes/client.json"), ClientConfig.class);
		}else {
			String configFileSrc=args[0];
            String configPath = System.getProperty("user.dir") + File.separatorChar +configFileSrc;
            System.out.println(configPath);
            File file = new File(configPath);
            if (file.exists()) {
                config = Mapper.parseObject(file, ClientConfig.class);
            }else {
                System.out.println("file not exist");
                System.exit(0);
            }
		}
		config.setIpv4LocalAddress(IpUtil.getLocalIp(4));
		config.setIpv6LocalAddress(IpUtil.getLocalIp(6));
		LocalProxyToServer localProxyToServerRunnable = new LocalProxyToServer(config);
		Thread toServerThread =new Thread(localProxyToServerRunnable,"localProxyToServerRunnable");
		toServerThread.start();
		//需要修改
		configureMap();
		new UdpRunner(config.getUdpport()).start();
//		while (true){
//			synchronized ("haha"){
//				if(toServerChannel==null) {
//					"haha".wait(1000);
//					toServerChannel = localProxyToServerRunnable.toServerChannel;
//				}
//				else break;
//			}
//		}
//		BlockingDeque deque = localProxyToServerRunnable.getDeque();
//		deque.add(new Data().setType(Type.configSending).setB(Mapper.getJsonByte(clientConfig)));

    }

	private static void configureMap() throws RangePortException {
		for (ConfigEntity configEntity : config.getList()) {
			ClientConfigFactory.getMap().putAll(RangePort.getRangePort(configEntity.getLocalServer(), configEntity.getPort(), configEntity.getRemotePort()));
		}
	}
}
