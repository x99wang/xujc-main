package pri.wx.jwcrawler.utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import pri.wx.jwcrawler.enums.ImageCode;
import pri.wx.jwcrawler.enums.QueryParms;
import pri.wx.jwcrawler.enums.UrlEnum;
import pri.wx.jwcrawler.model.MyHttpConfig;
import pri.wx.jwcrawler.model.MyResponse;
import pri.wx.jwcrawler.response.ErrorCode;
import pri.wx.jwcrawler.response.RestResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * 网络请求类
 *
 * @author wx
 * @version 1.0
 */

public class HttpUtil {

    private static final int CONNECT_TIMEOUT = MyHttpConfig.getHttpConnectTimeout();// 设置连接建立的超时时间为10s
    private static final int CONNECT_REQUEST_TIMEOUT = MyHttpConfig.getHttpConnectRequestTimeout();// 设置连接建立的超时时间为10s
    private static final int SOCKET_TIMEOUT = MyHttpConfig.getHttpSocketTimeout();
    private static final String CONTENT_TYPE_FORM_URL = MyHttpConfig.getContentTypeFormUrl();
    private static final String USER_AGENT_KEY = MyHttpConfig.getAgentKey();
    private static final String USER_AGENT_VALUE = MyHttpConfig.getAgentValue();
    private static final String ENCODING = MyHttpConfig.getEncoding();

    /**
     * 对http请求进行基本设置
     *
     * @param httpRequestBase http请求
     */
    private static void setRequestConfig(HttpRequestBase httpRequestBase) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECT_REQUEST_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();
        httpRequestBase.setConfig(requestConfig);
        httpRequestBase.setHeader(USER_AGENT_KEY, USER_AGENT_VALUE);
    }

    /**
     * 设置post请求的参数
     *
     * @param httpPost 请求
     * @param params 参数
     */
    private static void setPostParams(HttpPost httpPost, Map<String, String> params) {
        List<NameValuePair> nvps = new ArrayList<>();
        Set<String> keys = params.keySet();
        for (String key : keys) {
            nvps.add(new BasicNameValuePair(key, params.get(key)));
        }
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nvps, ENCODING);
            entity.setContentType(CONTENT_TYPE_FORM_URL);
            httpPost.setEntity(entity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置URI中的query
     * 顺序放前面
     * @param request 请求
     * @param url url地址
     * @param query 查询内容
     */
    private static void setQuery(HttpRequestBase request, String url, Map<String, String> query) {
        URI uri = URI.create(url);
        try {
            List<NameValuePair> nvps = new ArrayList<>();
            Set<String> keys = query.keySet();
            for (String key : keys) {
                nvps.add(new BasicNameValuePair(key, query.get(key)));
            }
            uri = new URIBuilder(url)
                    .setParameters(nvps)
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        request.setURI(uri);
    }

    private static CloseableHttpResponse post(String url, Map<String, String> params, Map<String, String> query) {
        HttpPost httpPost = new HttpPost(url);
        if(null != query)
            setQuery(httpPost,url, query);
        setRequestConfig(httpPost);
        if(null != params)
        setPostParams(httpPost, params);
        CloseableHttpResponse response = null;
        try {
            response = HttpConnectionPoolUtil.getHttpClient(url)
                    .execute(httpPost, HttpClientContext.create());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private static CloseableHttpResponse get(String url, Map<String, String> query) {
        HttpGet httpGet = new HttpGet(url);
        if(null != query)
            setQuery(httpGet,url,query);
        setRequestConfig(httpGet);
        CloseableHttpResponse response = null;
        try {
            response = HttpConnectionPoolUtil.getHttpClient(url).execute(httpGet, HttpClientContext.create());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static RestResponse<MyResponse> doPost(UrlEnum url, Map<String, String> params) {
        return doPost(url, params, QueryParms.NULL);
    }

    public static RestResponse<MyResponse> doPost(UrlEnum url, Map<String, String> params, QueryParms query) {
        RestResponse<MyResponse> r;
        MyResponse myResponse = new MyResponse();
        CloseableHttpResponse response = null;
        InputStream in = null;
        try {
            response = post(url.url(), params, query.query());
            HttpEntity entity = response.getEntity();
            myResponse.setCode(response.getStatusLine().getStatusCode())
                    .setCookies(HttpConnectionPoolUtil.getCookieStore().getCookies());
            if (entity != null) {
                in = entity.getContent();
                myResponse.setBody(IOUtils.toString(in, ENCODING));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new RestResponse<>(ErrorCode.NET_ERROR);
        } finally {
            try {
                if (in != null) in.close();
                if (response != null) response.close();

            } catch (IOException e) {
                e.printStackTrace();
                return new RestResponse<>(ErrorCode.NET_ERROR);
            }
        }
        r = new RestResponse<>(myResponse);
        return r;
    }

    public static RestResponse<MyResponse> doGet(UrlEnum url) {
        return doGet(url,QueryParms.NULL);
    }

    public static RestResponse<MyResponse> doGet(UrlEnum url, QueryParms query) {
        return doGet(url, query.query());
    }

    public static RestResponse<MyResponse> doGet(UrlEnum url, Map<String,String> query) {
        RestResponse<MyResponse> r;
        MyResponse myResponse = new MyResponse();
        CloseableHttpResponse response = null;
        InputStream in = null;
        try {
            response = get(url.url(), query);
            HttpEntity entity = response.getEntity();
            myResponse.setCode(response.getStatusLine().getStatusCode())
                    .setCookies(HttpConnectionPoolUtil.getCookieStore().getCookies());
            if (entity != null) {
                //验证码特殊操作
                if (url == UrlEnum.CODE_IMAGE) {
                    byte b[] = EntityUtils.toByteArray(entity);
                    ImageCode.INSTANCE.setImage(b);
                    String code = ImageCode.INSTANCE.getImageCode();
                    myResponse.setBody(code);
                } else {
                    in = entity.getContent();
                    myResponse.setBody(IOUtils.toString(in, ENCODING));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new RestResponse<>(ErrorCode.NET_ERROR);
        } finally {
            try {
                if (in != null) in.close();
                if (response != null) response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        r = new RestResponse<>(myResponse);
        return r;
    }

}
