package config;

import exception.RangePortException;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import util.Mapper;
import util.RangePort;

import java.io.File;
import java.util.Map;

//客户端用来获得配置的类
public class ClientConfigFactory {

        private static DualHashBidiMap map=new DualHashBidiMap();

        public static DualHashBidiMap getMap(){
            return map;
        }

        private ClientConfigFactory(){}

}
