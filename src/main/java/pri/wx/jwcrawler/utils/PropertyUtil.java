package pri.wx.jwcrawler.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * Desc:properties文件获取工具类
 * @author hafiz.zhang
 */
public class PropertyUtil {
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String NAME_PROPER = "http.properties";
    private static final Logger logger = LoggerFactory.getLogger(PropertyUtil.class);
    private static Properties props;
    static{
        loadProps();
    }

    synchronized static private void loadProps(){
        logger.info("开始设置properties文件内容.......");
        props = new Properties();
        props.setProperty("connectTimeout", "5000");
        props.setProperty("connectRequestTimeout", "5000");
        props.setProperty("socketTimeout", "10000");
        props.setProperty("maxPoolSize", "10");
        props.setProperty("idelTimeout", "1000");
        props.setProperty("monitorInterval", "3000");
        props.setProperty("contentTypeFormUrl", "application/x-www-form-urlencoded");
        props.setProperty("userAgentKey", "User-Agent");
        props.setProperty("userAgentValue", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36");
        props.setProperty("authorizationKey", "Authorization");
        props.setProperty("encoding", "gb2312");
        logger.info("设置properties文件完成");
        logger.info("properties文件内容：" + props);
    }

    public static String getProperty(String key){
        if(null == props) {
            loadProps();
        }
        return props.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        if(null == props) {
            loadProps();
        }
        return props.getProperty(key, defaultValue);
    }
}