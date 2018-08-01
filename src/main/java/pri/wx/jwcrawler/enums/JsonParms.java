package pri.wx.jwcrawler.enums;

/**
 * 为适应嘉庚部分api的参数名称所设计的枚举类
 *
 * @author wx
 */
public enum JsonParms {
    XJ_ID("xj_id"),
    XJ_XM("xj_xm"),
    XJ_XB("xj_xb"),
    XJ_NJ("xj_nj"),
    ZY_MC("zy_mc"),
    SSYQ("ssyq"),
    TEL("tel"),
    WECHAT_OPENID("wechat_openid"),
    QQ_OPENID("qq_openid"),
    WEIBO_ID("weibo_id"),
    OTHER("other"),
    XJ_PW("xj_pw"),
    PE_PW("pe_pw"),
    APIKEY("apikey"),
    KCB_ID("kcb_id"),
    SKSD_MC("sksd_mc"),
    SKSD_XQ("sksd_xq"),
    SKSD_ZC("sksd_zc"),
    SKSD_JC_S("sksd_jc_s"),
    SKSD_JC_E("sksd_jc_e"),
    SKSD_QZZ_S("sksd_qzz_s"),
    SKSD_QZZ_E("sksd_qzz_e"),
    CR_MC("cr_mc"),
    TM_ID("tm_id"),
    XSMD_XKFS("xsmd_xkfs"),
    KCB_MC("kcb_mc"),
    KCB_RS("kcb_rs"),
    KCB_QZZ("kcb_qzz"),
    KCB_RKJS_DESC("kcb_rkjs_desc"),
    KCB_SKSD_DESC("kcb_sksd_desc"),
    KCB_BZ("kcb_bz"),
    KC_MC("kc_mc"),
    KC_XF("kc_xf"),
    KSAP_KSRQ("ksap_ksrq"),
    KSAP_KSSD("ksap_kssd"),
    KSAP_KSSJ("ksap_kssj"),
    KSAP_KHFS("ksap_khfs"),
    KSAP_XQ("ksap_xq"),
    ZCJ("zcj"),
    ZCJ_DJ("zcj_dj"),
    XKFS("xkfs"),
    KSQK_QZ("ksqk_qz"),
    KSQK_QM("ksqk_qm"),
    KCB_CLASS_ID ("kcb_class_id"),
    EXAM_DATE("exam_date"),
    EXAM_ROOM("exam_room"),
    ZHCP_MC("zhcp_mc"),
    BJ_MC("bj_mc"),
    BJ_U("bj_u"),
    ZHCP_SZXS("zhcp_szxs"),
    ZHCP_SZDF("zhcp_szdf"),
    ZHCP_CJDF("zhcp_cjdf"),
    ZHCP_BJGMS("zhcp_bjgms"),
    ZHCP_XFBL("zhcp_xfbl"),
    ZHCP_CPDF ("zhcp_cpdf"),
    ZHCP_PM("zhcp_pm"),
    KCB_SKSD("kcb_sksd"),
    TM_MC("tm_mc"),

    KCB_RKJS("kcb_rkjs"), WEEK("week"), DAY_WEEK("day_week"), DATE("date"), TYPE("type"), HOUR("hour");

    private String value;

    JsonParms(String name) {
        this.value = name;
    }

    public String value() {
        return value;
    }

}
