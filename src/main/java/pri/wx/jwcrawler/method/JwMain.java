package pri.wx.jwcrawler.method;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.http.cookie.Cookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pri.wx.jwcrawler.enums.FormParms;
import pri.wx.jwcrawler.enums.JsonParms;
import pri.wx.jwcrawler.enums.QueryParms;
import pri.wx.jwcrawler.enums.UrlEnum;
import pri.wx.jwcrawler.model.MyResponse;
import pri.wx.jwcrawler.response.ErrorCode;
import pri.wx.jwcrawler.response.RestResponse;
import pri.wx.jwcrawler.utils.HttpConnectionPoolUtil;
import pri.wx.jwcrawler.utils.HttpUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pri.wx.jwcrawler.enums.QueryParms.*;
import static pri.wx.jwcrawler.enums.UrlEnum.*;
import static pri.wx.jwcrawler.response.ErrorCode.*;

/**
 * 教务操作类<br>
 * 其中返回的错误信息定义在
 * @see ErrorCode
 *
 * <br>
 * 方法：
 * <br>1、根据<b>学号</b>、<b>密码</b>登陆并且获取apikey
 * @see #login(String, String)
 * <br>2、个人信息页面获取学号、姓名、年级、专业、宿舍
 * @see #oneInfo()
 * <br>3、个人学期列表
 * @see #apiTerms(String)
 * <br>4、课程列表
 * @see #apiCourses(String, String)
 * <br>5、课程上课信息列表
 * @see #apiClasses(String, String)
 * <br>6、当前考试安排
 * @see #apiTerms(String)
 * <br>7、成绩信息
 * @see #apiScores(String, String)
 * <br>8、综合测评
 * @see #apiZhcp(String)
 *
 * @author wx
 * @version 1.0
 */
public class JwMain {

    /** 默认登陆重试次数 {@value} */
    private final static int RETRY_LOGIN = 10;
    private final static int RETRY_MAKE_KEY = 3;

    private static Logger logger = LoggerFactory.getLogger(JwMain.class);

    private static Gson gson = new GsonBuilder()
            .serializeNulls().create();

    private static Type mType = new TypeToken<Map<String, String>>() {
    }.getType();

    private static List<Cookie> cookies;

    /**
     * 模拟教务系统登陆
     * @param sno 教务系统账号
     * @param pwd 教务系统密码
     * @return 用户账户实体类
     * 学号、密码、apikey
     */
    public static RestResponse<Map<String, String>> login(String sno, String pwd) {
        long startTime = System.currentTimeMillis();
        cookies = new ArrayList<>();

        //Form参数
        Map<String, String> parms = new HashMap<>();
        parms.put(FormParms.PARM_USER.value(), sno);
        parms.put(FormParms.PARM_PWD.value(), pwd);
        parms.put(FormParms.PARM_LABEL.value(), FormParms.PARM_LABEL_VALUE.value());

        RestResponse<MyResponse> re = HttpUtil.doPost(TRY_LOGIN, parms, LOGIN);

        MyResponse verify = re.getResult();

        if (verify == null) {
            return new RestResponse<>(ErrorCode.valueOf(re.getCode()));
        }

        if (verify.getCode() == 200) {
            logger.error("Username is wrong. current username and password:\n" + sno + '\n' + pwd);
            return new RestResponse<>(PWD_WRONG);
        }
        int retry = 0;//登陆重试次数（重新解析验证码）
        MyResponse server;
        do {
            retry++;
            logger.info("Image code. current time:" + retry);
            parms.put(FormParms.PARM_IMGCODE.value(), imgCode());
            HttpConnectionPoolUtil.resetCookie(cookies);
            re = HttpUtil.doPost(JW_LOGIN, parms, LOGIN);
            server = re.getResult();
            if (server == null) {
                return new RestResponse<>(ErrorCode.valueOf(re.getCode()));
            }
        } while (server.getCode() != 302 && retry < RETRY_LOGIN);
        if (retry == 10) {
            logger.error("Parse code error");
            return new RestResponse<>(IMGCODE_WRONG);
        }

        //跳过问卷调查
        HttpUtil.doGet(JW_HOME, STU_INF);
        HttpUtil.doGet(JW_HOME, STU_INF);

        String apikey = apiKey().getResult();
        if (null == apikey) {
            logger.error("Get apikey error");
            return new RestResponse<>(APIKEY_WRONG);
        }

        Map<String, String> account = new HashMap<>();
        account.put(JsonParms.XJ_ID.value(), sno);
        account.put(JsonParms.XJ_PW.value(), pwd);
        account.put(JsonParms.APIKEY.value(), apikey);

        long endTime = System.currentTimeMillis();
        return new RestResponse<>("处理时间" + (endTime - startTime) + "ms", account);
    }

    /**
     * Get and parse Image Code
     *
     * @return String of ImgCode
     */
    private static String imgCode() {
        RestResponse<MyResponse> mresponse = HttpUtil.doGet(UrlEnum.CODE_IMAGE);


        MyResponse response = mresponse.getResult();
        if (response == null) {
            return null;
        }
        for (Cookie cookie : response.getCookies()) {
            if (cookie.getName().equals(FormParms.COOKIE_KEY_IMGCODE.value())) {
                cookies.add(cookie);
                break;
            }
        }
        return response.getBody();
    }

    /**
     * Get one's information
     * about name,grade,dorm,stuNo
     * @return info
     */
    public static RestResponse<JSONObject> oneInfo() {
        long startTime = System.currentTimeMillis();

        RestResponse<MyResponse> mresponse = HttpUtil.doGet(JW_HOME, STU_INF);

        MyResponse inf = mresponse.getResult();
        if (inf == null) {
            return new RestResponse<>(ErrorCode.valueOf(mresponse.getCode()));
        }
        String infStr = JwParse.getStuInfo(inf.getBody());
        if (null != infStr && !infStr.isEmpty()) {
            JSONObject res = new JSONObject(infStr);
            long endTime = System.currentTimeMillis();
            return new RestResponse<>("处理时间" + (endTime - startTime) + "ms", res);
        }
        return new RestResponse<>(ErrorCode.INFO_ERROR);
    }

    /**
     * Get one's attendance record
     * 考勤明细
     * @return list of attendances
     */
    public static RestResponse<JSONArray> oneAtt() {
        long startTime = System.currentTimeMillis();

        RestResponse<MyResponse> mresponse = HttpUtil.doGet(JW_HOME,STU_ATT);

        MyResponse att = mresponse.getResult();
        if (att == null) {
            return new RestResponse<>(ErrorCode.valueOf(mresponse.getCode()));
        }
        String attStr = JwParse.getAttendanceRecord(att.getBody());
        if (null != attStr && !attStr.isEmpty()) {
            JSONArray res = new JSONArray(attStr);
            long endTime = System.currentTimeMillis();
            return new RestResponse<>("处理时间" + (endTime - startTime) + "ms", res);
        }
        return new RestResponse<>(ErrorCode.ATT_ERROR);
    }

    /**
     * 调停补课
     * @return list of attendances
     */
    public static RestResponse<JSONArray> oneTbk() {
        long startTime = System.currentTimeMillis();

        RestResponse<MyResponse> mresponse = HttpUtil.doGet(JW_HOME,STU_TBK);

        MyResponse att = mresponse.getResult();
        if (att == null) {
            return new RestResponse<>(ErrorCode.valueOf(mresponse.getCode()));
        }
        String attStr = JwParse.getRemediation(att.getBody());
        if (null != attStr && !attStr.isEmpty()) {
            JSONArray res = new JSONArray(attStr);
            long endTime = System.currentTimeMillis();
            return new RestResponse<>("处理时间" + (endTime - startTime) + "ms", res);
        }
        return new RestResponse<>(ErrorCode.TBK_ERROR);
    }

    /**
     * Get api_key
     *
     * @return String of key
     */
    private static RestResponse<String> apiKey() {
        RestResponse<MyResponse> response = HttpUtil.doGet(JW_HOME, API_KEY);
        MyResponse apikey = response.getResult();
        String key = JwParse.getApikey(apikey.getBody());
        if (null != key) {
            return new RestResponse<>(key);
        }
        int retry = 0;//申请key重试次数（重新申请key）
        do {
            retry++;
            logger.info("Make apikey. current time:" + retry);
            HttpConnectionPoolUtil.resetCookie(cookies);
            response = HttpUtil.doGet(JW_HOME, MAKE_KEY);
            apikey = response.getResult();
        } while (apikey.getCode() != 302 && retry < RETRY_MAKE_KEY);
        if (retry == 10) {
            logger.error("Make apikey error");
            return new RestResponse<>(MAKE_KEY_WRONG);
        }
        response = HttpUtil.doGet(JW_HOME, API_KEY);
        apikey = response.getResult();
        key = JwParse.getApikey(apikey.getBody());
        if (apikey.getCode() != 200 || null == key) {
            logger.error("Parse apikey error");
            return new RestResponse<>(APIKEY_WRONG);
        }
        return new RestResponse<>(key);
    }

    /**
     * Get list of student's term
     *
     * @param apikey one's apikey
     * @return list of term
     */
    public static RestResponse<JSONArray> apiTerms(String apikey) {
        long startTime = System.currentTimeMillis();
        RestResponse<MyResponse> mresponse = HttpUtil.doGet(UrlEnum.JW_TERM, QueryParms.apiQuery(apikey));

        if (null == mresponse.getResult())
            return new RestResponse<>(ErrorCode.valueOf(mresponse.getCode()), mresponse.getError());
        MyResponse response = mresponse.getResult();
        if (200 != response.getCode()) {
            return new RestResponse<>(ErrorCode.API_TERM_WRONG);
        }

        /* format of response must be right */

        Map<String, String> map = gson.fromJson(response.getBody(), mType);

        JSONArray terms = new JSONArray();
        for (String key : map.keySet()) {
            Map<String, String> t = new HashMap<>();
            t.put(JsonParms.TM_ID.value(), key);
            t.put(JsonParms.TM_MC.value(), map.get(key));
            terms.put(t);
        }
        long endTime = System.currentTimeMillis();
        return new RestResponse<>("处理时间" + (endTime - startTime) + "ms", terms);
    }

    /**
     * 根据apikey返回个人信息.
     *
     * @param apikey one's apikey
     * @return 学号、姓名、年级、专业
     */
    public static RestResponse<Map<String, String>> apiInfo(String apikey) {
        long startTime = System.currentTimeMillis();
        RestResponse<MyResponse> mresponse = HttpUtil.doGet(UrlEnum.JW_INFOMATION, QueryParms.apiQuery(apikey));

        if (null == mresponse.getResult())
            return new RestResponse<>(ErrorCode.valueOf(mresponse.getCode()), mresponse.getError());
        MyResponse response = mresponse.getResult();
        if (200 != response.getCode()) {
            logger.error("Request api of info error");
            return new RestResponse<>(ErrorCode.API_INFO_WRONG);
        }

        /* format of response must be right */
        Map<String, String> map = gson.fromJson(response.getBody(), mType);

        if (!map.containsKey(JsonParms.XJ_ID.value())) {
            logger.error("Request api of info error");
            return new RestResponse<>(ErrorCode.API_INFO_WRONG);
        }
        long endTime = System.currentTimeMillis();
        return new RestResponse<>("处理时间" + (endTime - startTime) + "ms", map);
    }

    /**
     * Get list of student's course
     *
     * @param apikey one's apikey
     * @param tmId   term id
     * @return 该学期的所选课程
     */
    public static RestResponse<JSONArray> apiCourses(String apikey, String tmId) {
        long startTime = System.currentTimeMillis();
        RestResponse<MyResponse> mresponse = HttpUtil.doGet(UrlEnum.JW_COURSE, QueryParms.apiTermQuery(apikey, tmId));

        if (null == mresponse.getResult())
            return new RestResponse<>(ErrorCode.valueOf(mresponse.getCode()), mresponse.getError());
        MyResponse response = mresponse.getResult();
        if (200 != response.getCode()) {
            return new RestResponse<>(ErrorCode.API_COURSE_WRONG);
        }

        /* format of response must be right */
        String body = response.getBody()
                .replace("\n", "")
                .replace("\r", "");

        logger.debug("Courses body:" + body);
        JSONArray maps;
        try {
            maps = new JSONArray(body);
        } catch (JSONException e) {
            maps = new JSONArray();
        }

        for (int i = 0; i < maps.length(); i++) {
            maps.getJSONObject(i).remove(JsonParms.KCB_SKSD.value());
        }
        long endTime = System.currentTimeMillis();
        return new RestResponse<>("处理时间" + (endTime - startTime) + "ms", maps);
    }

    /**
     * Get list of student's classes
     *
     * @param apikey one's apikey
     * @param tmId   term id
     * @return dbClasses of term
     */
    public static RestResponse<JSONArray> apiClasses(String apikey, String tmId) {
        long startTime = System.currentTimeMillis();
        RestResponse<MyResponse> mresponse = HttpUtil.doGet(UrlEnum.JW_COURSE, QueryParms.apiTermQuery(apikey, tmId));

        if (null == mresponse.getResult())
            return new RestResponse<>(ErrorCode.valueOf(mresponse.getCode()), mresponse.getError());
        MyResponse response = mresponse.getResult();
        if (200 != response.getCode()) {
            return new RestResponse<>(ErrorCode.API_COURSE_WRONG);
        }

        /* format of response must be right */
        String body = response.getBody()
                .replace("\n", "")
                .replace("\r", "");

        logger.debug("Classes body:" + body);
        JSONArray maps;
        JSONArray classes;
        try {
            maps = new JSONArray(body);
            classes = new JSONArray();
        } catch (JSONException e) {
            maps = new JSONArray();
            classes = new JSONArray();
        }
        for (int i = 0; i < maps.length(); i++) {
            JSONArray c;
            try {
                JSONObject course = maps.getJSONObject(i);
                Object obj = course.get(JsonParms.KCB_SKSD.value());
                if (obj instanceof JSONArray) {
                    c = (JSONArray) obj;
                } else {
                    c = new JSONArray();
                }
            } catch (JSONException e) {
                c = new JSONArray();
                System.err.println(e.getMessage()
                        + '\n' + maps.getJSONObject(i));
            }
            if (c.length() > 0) {
                for (int j = 0; j < c.length(); j++)
                    classes.put(c.getJSONObject(j).put("kc_mc",maps.getJSONObject(i).getString("kc_mc")));
            }
        }
        long endTime = System.currentTimeMillis();

        return new RestResponse<>("处理时间" + (endTime - startTime) + "ms", classes);
    }

    /**
     * Get list of student's exam
     *
     * @param apikey one's apikey
     * @return list of dbExam
     */
    public static RestResponse<JSONArray> apiExams(String apikey) {

        long startTime = System.currentTimeMillis();
        RestResponse<MyResponse> mresponse = HttpUtil.doGet(UrlEnum.JW_EXAM, QueryParms.apiQuery(apikey));

        if (null == mresponse.getResult())
            return new RestResponse<>(ErrorCode.valueOf(mresponse.getCode()), mresponse.getError());
        MyResponse response = mresponse.getResult();
        if (200 != response.getCode()) {
            return new RestResponse<>(ErrorCode.API_EXAM_WRONG);
        }

        /* format of response must be right */
        String body = response.getBody()
                .replace("\n", "")
                .replace("\r", "");

        logger.debug("Courses body:" + body);
        JSONObject map;
        try {
            map = new JSONObject(body);
        } catch (JSONException e) {
            map = new JSONObject();
        }
        JSONArray exams = new JSONArray();
        for (String key : map.keySet()) {
            JSONObject exam = map.getJSONObject(key);
            if (null != exam)
                exams.put(exam);
        }
        long endTime = System.currentTimeMillis();
        return new RestResponse<>("处理时间" + (endTime - startTime) + "ms", exams);
    }

    /**
     * Get list of student's score
     *
     * @param apikey one's apikey
     * @param tmId   term id
     * @return list of dbScore
     */
    public static RestResponse<JSONArray> apiScores(String apikey, String tmId) {

        long startTime = System.currentTimeMillis();
        RestResponse<MyResponse> mresponse = HttpUtil.doGet(UrlEnum.JW_SCORE, QueryParms.apiTermQuery(apikey, tmId));

        if (null == mresponse.getResult())
            return new RestResponse<>(ErrorCode.valueOf(mresponse.getCode()), mresponse.getError());
        MyResponse response = mresponse.getResult();
        if (200 != response.getCode()) {
            return new RestResponse<>(ErrorCode.API_SCORE_WRONG);
        }

        /* format of response must be right */
        String body = response.getBody()
                .replace("\n", "")
                .replace("\r", "");

        logger.debug("Scores body:" + body);
        JSONArray maps;
        try {
            maps = new JSONArray(body);
        } catch (JSONException e) {
            maps = new JSONArray();
        }
        for (int i = 0; i < maps.length(); i++) {
            JSONObject m = maps.getJSONObject(i);
            m.put(JsonParms.XJ_ID.value(), apikey.split("-")[0].toUpperCase());
            m.put(JsonParms.TM_ID.value(), tmId);
        }
        long endTime = System.currentTimeMillis();
        return new RestResponse<>("处理时间" + (endTime - startTime) + "ms", maps);

    }

    /**
     * Get list of student's evaluation
     *
     * @param apikey one's apikey
     * @return list of dbEvaluation
     */
    public static RestResponse<JSONArray> apiZhcp(String apikey) {

        long startTime = System.currentTimeMillis();

        RestResponse<MyResponse> mresponse = HttpUtil.doGet(UrlEnum.JW_ZHCP, QueryParms.apiQuery(apikey));
        if (null == mresponse.getResult())
            return new RestResponse<>(ErrorCode.valueOf(mresponse.getCode()), mresponse.getError());
        MyResponse response = mresponse.getResult();
        if (200 != response.getCode()) {
            return new RestResponse<>(ErrorCode.API_ZHCP_WRONG);
        }

        /* format of response must be right */
        String body = response.getBody()
                .replace("\n", "")
                .replace("\r", "");

        logger.debug("Evaluation body:" + body);
        JSONArray maps;
        try {
            maps = new JSONArray(body);
        } catch (JSONException e) {
            maps = new JSONArray();
        }
        for (int i = 0; i < maps.length(); i++) {
            JSONObject m = maps.getJSONObject(i);
            m.put(JsonParms.XJ_ID.value(), apikey.split("-")[0].toUpperCase());
            String tmId = m.getString(JsonParms.ZHCP_MC.value());
            /* 手动拼凑学期号 */
            tmId = "20" + tmId.split("-")[0] + tmId.split("-")[1].charAt(3);
            m.put(JsonParms.TM_ID.value(), tmId);
        }
        long endTime = System.currentTimeMillis();
        return new RestResponse<>("处理时间" + (endTime - startTime) + "ms", maps);

    }


}
