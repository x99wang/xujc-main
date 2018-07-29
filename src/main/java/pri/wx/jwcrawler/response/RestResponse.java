package pri.wx.jwcrawler.response;


import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.UUID;

/**
 * 返回给上层的自定义响应类
 *
 * @param <T> 响应体中的实体类型{@code List , Map}
 * @author wx
 */
public class RestResponse<T> implements Serializable {

    //响应ID
    private String id = UUID.randomUUID().toString();

    //响应代码
    private String code = "SUCCESS";

    //响应描述
    private String message;

    //响应体
    private T result = null;

    //错误体
    private ErrorResult error = null;

    public RestResponse(T t) {
        this("操作成功", t);
    }

    public RestResponse(String msg, T t) {
        this.message = msg;
        this.result = t;
    }

    //构造函数
    public RestResponse(ErrorCodeImp errorCode) {
        this(errorCode, null);
    }

    public RestResponse(ErrorCodeImp errorCode, ErrorResult error) {
        super();
        this.code = errorCode.name();
        this.message = errorCode.value();
        this.error = error;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public ErrorResult getError() {
        return error;
    }

    public void setError(ErrorResult error) {
        this.error = error;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                if (field.get(this) == null) {
                    json.put(field.getName(), (Object) null);
                } else {
                    if (field.get(this) instanceof JSONArray
                            || field.get(this) instanceof JSONObject
                            || field.get(this) instanceof String) {
                        json.put(field.getName(), field.get(this));
                    } else {
                        json.put(field.getName(), new JSONObject(
                                new Gson().toJson(field.get(this))));
                    }
                }
            } catch(IllegalAccessException e){
            e.printStackTrace();
        }
    }
    return json.toString();
}
}