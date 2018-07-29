package pri.wx.jwcrawler.enums;

/**
 * 教务系统部分表单提交信息枚举类
 *
 * @author wx
 */
public enum FormParms {
    PARM_USER("username"),
    PARM_PWD("password"),
    PARM_LABEL("user_lb"),
    PARM_LABEL_VALUE("学生"),
    PARM_IMGCODE("imgcode"),
    COOKIE_KEY_IMGCODE("jwimgcode");

    private String str;

    FormParms(String s) {
        str = s;
    }

    public String value() {
        return str;
    }
}
