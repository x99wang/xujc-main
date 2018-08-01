package pri.wx.jwcrawler.utils;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pri.wx.jwcrawler.model.MyHttpConfig;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Http连接池
 * @author PigPIgAutumn
 * <a href="https://www.jianshu.com/p/c852cbcf3d68">出处</a>
 */
public class HttpConnectionPoolUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpConnectionPoolUtil.class);

    private static final int MAX_CONN = MyHttpConfig.getHttpMaxPoolSize(); // 最大连接数
    private static final int Max_PRE_ROUTE = MyHttpConfig.getHttpMaxPoolSize();
    private static final int MAX_ROUTE = MyHttpConfig.getHttpMaxPoolSize();

    private static CloseableHttpClient httpClient; // 发送请求的客户端单例
    private static PoolingHttpClientConnectionManager manager; //连接池管理类
    private static ScheduledExecutorService monitorExecutor;
    private static BasicCookieStore cookieStore;

    private final static Object syncLock = new Object(); // 相当于线程锁,用于线程安全

    static CloseableHttpClient getHttpClient(String url) {
        String hostName = url.split("/")[2];
        System.out.println(hostName);
        int port = 80;
        if (hostName.contains(":")) {
            String[] args = hostName.split(":");
            hostName = args[0];
            port = Integer.parseInt(args[1]);
        }

        if (httpClient == null) {
            //多线程下多个线程同时调用getHttpClient容易导致重复创建httpClient对象的问题,所以加上了同步锁
            synchronized (syncLock) {
                if (httpClient == null) {
                    cookieStore = new BasicCookieStore();
                    httpClient = createHttpClient(hostName, port);

                    //开启监控线程,对异常和空闲线程进行关闭
                    monitorExecutor = Executors.newScheduledThreadPool(1);
                    monitorExecutor.scheduleAtFixedRate(() -> {
                                //关闭异常连接
                                manager.closeExpiredConnections();
                                //关闭5s空闲的连接
                                manager.closeIdleConnections(MyHttpConfig.getHttpIdelTimeout(), TimeUnit.MILLISECONDS);
                                logger.info("close expired and idle for over 5s connection");

                            },
                            MyHttpConfig.getHttpMonitorInterval(),
                            MyHttpConfig.getHttpMonitorInterval(),
                            TimeUnit.MILLISECONDS);
                }
            }
        }
        return httpClient;
    }

    /**
     * 根据host和port构建httpclient实例
     *
     * @param host 要访问的域名
     * @param port 要访问的端口
     * @return httpclient实例
     */
    private static CloseableHttpClient createHttpClient(String host, int port) {
        ConnectionSocketFactory plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainSocketFactory)
                .register("https", sslSocketFactory)
                .build();

        manager = new PoolingHttpClientConnectionManager(registry);
        //设置连接参数
        manager.setMaxTotal(MAX_CONN); // 最大连接数
        manager.setDefaultMaxPerRoute(Max_PRE_ROUTE); // 路由最大连接数

        HttpHost httpHost = new HttpHost(host, port);
        manager.setMaxPerRoute(new HttpRoute(httpHost), MAX_ROUTE);

        //请求失败时,进行请求重试
        HttpRequestRetryHandler handler = (e, i, httpContext) -> {
            if (i > 3) {
                //重试超过3次,放弃请求
                logger.error("retry has more than 3 time, give up request");
                return false;
            }
            if (e instanceof NoHttpResponseException) {
                //服务器没有响应,可能是服务器断开了连接,应该重试
                logger.error("receive no response from server, retry");
                return true;
            }
            if (e instanceof SSLHandshakeException) {
                // SSL握手异常
                logger.error("SSL hand shake exception");
                return false;
            }
            if (e instanceof InterruptedIOException) {
                //超时
                logger.error("InterruptedIOException");
                return false;
            }
            if (e instanceof UnknownHostException) {
                // 服务器不可达
                logger.error("server host unknown");
                return false;
            }
            if (e instanceof SSLException) {
                logger.error("SSLException");
                return false;
            }

            HttpClientContext context = HttpClientContext.adapt(httpContext);
            HttpRequest request = context.getRequest();
            return !(request instanceof HttpEntityEnclosingRequest);
        };

        CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(manager)
                .setRetryHandler(handler)
                .setDefaultCookieStore(cookieStore)
                //使用Fiddler工具时配置
                //.setProxy(new HttpHost("127.0.0.1", 8888))
                .build();

        return client;
    }

    static org.apache.http.client.CookieStore getCookieStore() {
        return null != httpClient ? cookieStore : null;
    }

    public static void addCookie(Cookie cookie) {
        cookieStore.addCookie(cookie);
    }

    public static void resetCookie(List<Cookie> cookies) {
        cookieStore.clear();
        for (Cookie c : cookies)
            cookieStore.addCookie(c);
    }


    /**
     * 关闭连接池
     */
    public static void closeConnectionPool() {
        try {
            httpClient.close();
            manager.close();
            monitorExecutor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}