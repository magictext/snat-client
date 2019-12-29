package map;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public class ClientChannelMap {
    //存放sessionid和channel对应关系的map
    public static Map<Integer, Channel> map=new HashMap<>();
}
