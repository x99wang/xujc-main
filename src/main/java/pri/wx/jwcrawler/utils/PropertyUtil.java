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
    private static final String NAME_PROPER = "src/http.properties";
    private static final Logger logger = LoggerFactory.getLogger(PropertyUtil.class);
    private static Properties props;
    static{
        loadProps();
    }

    synchronized static private void loadProps(){
        logger.info("开始加载properties文件内容.......");
        props = new Properties();
        InputStream in = null;
        try {
            //<!--第一种，通过类加载器进行获取properties文件流-->
            // in = PropertyUtil.class.getClassLoader().getResourceAsStream(NAME_PROPER);
            //<!--第二种，通过类进行获取properties文件流-->
            //in = PropertyUtil.class.getResourceAsStream("/jdbc.properties");

            // 获取文件流（方法1或2均可）
            InputStream inputStream = new BufferedInputStream(new FileInputStream(new File("src/main/resources/"+NAME_PROPER))); //方法1
//            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("jdbc.properties"); //方法2
            props.load(new InputStreamReader(inputStream, DEFAULT_ENCODING));
        } catch (FileNotFoundException e) {
            logger.error(NAME_PROPER + "文件未找到");
        } catch (IOException e) {
            logger.error("出现IOException");
        } finally {
            try {
                if(null != in) {
                    in.close();
                }
            } catch (IOException e) {
                logger.error(NAME_PROPER+"文件流关闭出现异常");
            }
        }
        logger.info("加载properties文件内容完成...........");
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