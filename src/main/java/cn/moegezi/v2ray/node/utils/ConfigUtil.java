package cn.moegezi.v2ray.node.utils;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
    private static Configuration config = null;

    static {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                        .configure(params.properties()
                                .setFileName("config.properties"));
        try {
            config = builder.getConfiguration();
        } catch (ConfigurationException e) {
            logger.error("配置文件错误", e);
        }
    }

    public static String getString(String key) {
        return config.getString(key, "");
    }

    public static String[] getStringArray(String key) {
        return config.getStringArray(key);
    }

    public static Integer getInteger(String key) {
        return config.getInteger(key, 0);
    }

    public static Double getDouble(String key) {
        return config.getDouble(key, 0D);
    }

    public static Long getLong(String key) {
        return config.getLong(key, 0L);
    }

    public static void setProperty(String key, Object value) {
        config.setProperty(key, value);
    }

}