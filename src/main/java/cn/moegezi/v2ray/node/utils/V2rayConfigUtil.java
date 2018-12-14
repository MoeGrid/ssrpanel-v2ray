package cn.moegezi.v2ray.node.utils;

import cn.moegezi.v2ray.node.model.InboundModel;
import cn.moegezi.v2ray.node.model.V2rayConfig;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class V2rayConfigUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
    private static V2rayConfig config = null;

    static {
        File file = new File("config.json");
        try {
            FileInputStream stream = new FileInputStream(file);
            config = JSON.parseObject(stream, V2rayConfig.class);
        } catch (IOException e) {
            logger.error("V2Ray配置文件错误", e);
        }
    }

    public static InboundModel getInboundByTag(String tag) {
        for (InboundModel i : config.getInbounds()) {
            if (i.getTag().equals(tag)) {
                return i;
            }
        }
        return null;
    }

}
