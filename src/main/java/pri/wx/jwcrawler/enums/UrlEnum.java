package pri.wx.jwcrawler.enums;

/**
 * 嘉庚学院教务系统部分网页/接口静态地址
 *
 * @author wx
 */

public enum UrlEnum {
    //无验证码登陆
    TRY_LOGIN("http://xyfw.xujc.com/login/index.php"),
    //教务系统登陆
    JW_LOGIN("http://jw.xujc.com/index.php"),
    //教务登陆验证码
    CODE_IMAGE("http://jw.xujc.com/imgcode.php"),
    //教务系统内部主页
    JW_HOME("http://jw.xujc.com/student/index.php"),
    //学期列表接口
    JW_TERM("http://jw.xujc.com/api/kb.php"),
    //学号信息接口
    JW_INFOMATION("http://jw.xujc.com/api/me.php"),
    //综合测评接口
    JW_ZHCP("http://jw.xujc.com/api/zhcp.php"),
    //课程课表接口
    JW_COURSE("http://jw.xujc.com/api/kb.php"),
    //成绩查询接口
    JW_SCORE("http://jw.xujc.com/api/score.php"),
    //考试安排接口
    JW_EXAM("http://jw.xujc.com/api/ksap.php");

    private String url;

    UrlEnum(String url) {
        this.url = url;
    }

    public String url() {
        return this.url;
    }

}
