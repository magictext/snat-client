package runner;

import com.fasterxml.jackson.core.JsonProcessingException;
import config.ClientConfig;
import config.ClientConfigFactory;
import exception.RangePortException;
import io.netty.channel.Channel;
import util.Data;
import util.Mapper;
import util.Type;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingDeque;

public class LocalProxyRunner {

	public static Channel toServerChannel;

	public  static String configFileSrc;

	public static void main(String[] args) throws InterruptedException, RangePortException, JsonProcessingException {
		if(args.length==0){

		}else {
			configFileSrc=args[0];
		}
		ClientConfig clientConfig = ClientConfigFactory.getInstance();
		LocalProxyToServer localProxyToServerRunnable = new LocalProxyToServer(new InetSocketAddress(clientConfig.getServerAddress(), clientConfig.getPort()));
		Thread toServerThread =new Thread(localProxyToServerRunnable,"localProxyToServerRunnable");
		toServerThread.start();
		toServerChannel = localProxyToServerRunnable.toServerChannel;
		while (true){
			synchronized ("haha"){
				if(toServerChannel==null) {
					"haha".wait(1000);
					toServerChannel = localProxyToServerRunnable.toServerChannel;
				}
				else break;
			}
		}
		BlockingDeque deque = localProxyToServerRunnable.getDeque();
		deque.add(new Data().setType(Type.configSending).setB(Mapper.getJsonByte(clientConfig)));

    }
}
