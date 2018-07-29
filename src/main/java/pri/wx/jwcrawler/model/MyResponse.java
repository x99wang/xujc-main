package pri.wx.jwcrawler.model;

import org.apache.http.cookie.Cookie;

import java.util.List;

/**
 * 自定义响应类
 *
 * @author wx
 */
public class MyResponse {

    private int code;
    private String body;
    private List<Cookie> cookies;

    public int getCode() {
        return code;
    }

    public MyResponse setCode(int code) {
        this.code = code;
        return this;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public MyResponse setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
        return this;
    }

    public String getBody() {
        return body;
    }

    public MyResponse setBody(String body) {
        this.body = body;
        return this;
    }

    public static MyResponse FAILURE() {
        return FAILURE(-1);
    }

    private static MyResponse FAILURE(int code) {
        return FAILURE(code, "Unknown error");
    }

    private static MyResponse FAILURE(int code, String body) {
        return new MyResponse()
                .setCode(code)
                .setBody(body);
    }

    public static MyResponse PWD_WRONG() {
        return FAILURE(10001,"登陆失败：账号或密码错误");
    }

    public static MyResponse IMGCODE_WRONG() {
        return FAILURE(10002,"登陆失败：后台识别验证码错误");
    }

    public static MyResponse MAKE_KEY_WRONG() {
        return FAILURE(10003,"获取apikey失败：申请apikey错误");
    }

    public static MyResponse APIKEY_WRONG() {
        return FAILURE(10004,"获取apikey失败：获取apikey错误");
    }

}
