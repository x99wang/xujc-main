package pri.wx.jwcrawler.model;

import pri.wx.jwcrawler.utils.PropertyUtil;

/**
 * 读取http.property文件类
 *
 * @author wx
 */

public class MyHttpConfig {

    public static final String CURRENT_USER_ID = "CURRENT_USER_ID";

    public static String getEncoding(){
        return PropertyUtil.getProperty("encoding");
    }

    public static int getHttpConnectTimeout() {
        return Integer.parseInt(PropertyUtil.getProperty("connectTimeout"));
    }

    public static int getHttpConnectRequestTimeout() {
        return Integer.parseInt(PropertyUtil.getProperty("connectRequestTimeout"));
    }

    public static int getHttpSocketTimeout() {
        return Integer.parseInt(PropertyUtil.getProperty("socketTimeout"));
    }

    public static int getHttpMaxPoolSize() {
        return Integer.parseInt(PropertyUtil.getProperty("maxPoolSize"));
    }

    public static long getHttpIdelTimeout() {
        return Integer.parseInt(PropertyUtil.getProperty("idelTimeout"));
    }

    public static long getHttpMonitorInterval() {
        return Integer.parseInt(PropertyUtil.getProperty("monitorInterval"));
    }

    public static String getContentTypeFormUrl() {
        return PropertyUtil.getProperty("contentTypeFormUrl");
    }

    public static String getAgentKey() {
        return PropertyUtil.getProperty("userAgentKey");
    }

    public static String getAgentValue() {
        return PropertyUtil.getProperty("userAgentValue");
    }

    public static String getAuthKey() {
        return PropertyUtil.getProperty("authorizationKey");
    }

}
