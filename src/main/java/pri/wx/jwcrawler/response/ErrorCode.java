package pri.wx.jwcrawler.response;

/**
 * 错误信息枚举类
 *
 * @author wx
 * @date 2018/7/25
 */
public enum ErrorCode implements ErrorCodeImp {
    NET_ERROR("网络请求错误"),
    PWD_WRONG("账号或密码错误"),
    IMGCODE_WRONG("后台识别验证码错误"),
    MAKE_KEY_WRONG("申请apikey错误"),
    INFO_ERROR("获取个人信息错误"),
    APIKEY_WRONG("获取apikey错误"),
    API_TERM_WRONG("api获取学期列表错误"),
    API_INFO_WRONG("api获取学号信息错误"),
    API_COURSE_WRONG("api获取课表错误"),
    API_EXAM_WRONG("api获取考试安排错误"),
    API_SCORE_WRONG("api获取成绩信息错误"),
    API_ZHCP_WRONG("api获取综合测评错误"),
    
    DB_TERM_WRONG("数据库获取学期列表错误"),
    DB_INFO_WRONG("数据库获取学号信息错误"),
    DB_COURSE_WRONG("数据库获取课表错误"),
    DB_EXAM_WRONG("数据库获取考试安排错误"),
    DB_SCORE_WRONG("数据库获取成绩信息错误"),
    DB_ZHCP_WRONG("数据库获取综合测评错误"),


    ATT_ERROR("获取考勤信息错误"),
    TBK_ERROR("获取调补课信息错误");



    private String value;

    ErrorCode(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
