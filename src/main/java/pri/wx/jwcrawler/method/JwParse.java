package pri.wx.jwcrawler.method;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import pri.wx.jwcrawler.enums.JsonParms;

import java.util.HashMap;
import java.util.Map;

class JwParse {

    /**
     * 解析个人信息页
     * 失败结果返回空的map
     * @param inf_str 网页内容
     * @return Stu Entity
     */
    static String getStuInfo(String inf_str) {

        Map<String,String> stu = new HashMap<>();

        Document doc = Jsoup.parse(inf_str);
        Element inf = doc.body().getElementById("wrap").
                getElementById("main").getElementById("m_ext").
                getElementById("main2").getElementById("content").
                select("table").first().
                select("tbody").first();

        for (Element e : inf.select("tr")) {
            for (Element f : e.select("th")) {
                String key = f.text();
                String value = f.nextElementSibling().text();
                if ("学号".equals(key)) {
                    stu.put(JsonParms.XJ_ID.value(),value);
                } else if ("姓名".equals(key)) {
                    stu.put(JsonParms.XJ_XM.value(),value);
                } else if ("性别".equals(key)) {
                    stu.put(JsonParms.XJ_XB.value(),value);
                } else if ("年级".equals(key)) {
                    stu.put(JsonParms.XJ_NJ.value(),value);
                } else if ("专业".equals(key)) {
                    stu.put(JsonParms.ZY_MC.value(),value);
                } else if ("宿舍".equals(key)) {
                    stu.put(JsonParms.SSYQ.value(),value);
                }
                /*else if ("姓名拼音".equals(key)) {
                } else if ("学历".equals(key)) {
                } else if ("学制".equals(key)) {
                } else if ("入学年份".equals(key)) {
                } else if ("毕业年份".equals(key)) {
                } else if ("专业方向".equals(key)) {
                } else if ("行政班级".equals(key)) {
                } else if ("身份证号".equals(key)) {
                } else if ("中学名称".equals(key)) {
                } else if ("生源地".equals(key)) {
                } else if ("出生日期".equals(key)) {
                } else if ("民族".equals(key)) {
                } else if ("政治面貌".equals(key)) {
                } else if ("移动电话".equals(key)) {
                } else if ("高考生源地".equals(key)) {
                } else if ("通讯地址".equals(key)) {
                } else if ("邮编".equals(key)) {
                } else if ("家庭电话".equals(key)) {
                } else if ("收件人".equals(key)) {
                } else if ("父亲姓名".equals(key)) {
                } else if ("父亲生日".equals(key)) {
                } else if ("父亲单位".equals(key)) {
                } else if ("父亲职务".equals(key)) {
                } else if ("父亲办公电话".equals(key)) {
                } else if ("父亲移动电话".equals(key)) {
                } else if ("母亲姓名".equals(key)) {
                } else if ("母亲生日".equals(key)) {
                } else if ("母亲单位".equals(key)) {
                } else if ("母亲职务".equals(key)) {
                } else if ("母亲办公电话".equals(key)) {
                } else if ("母亲移动电话".equals(key)) {
                } else if ("籍贯".equals(key)) {
                } else if ("身高(cm)".equals(key)) {
                } else if ("体重(kg)".equals(key)) {
                } else if ("银行卡号".equals(key)) {
                } else if ("学籍异动".equals(key)) {
                }*/
            }
        }

        return new JSONObject(stu).toString();
    }

    /**
     * 抓取apikey
     * 失败结果返回 null
     * @param key_str apikey的获取页面
     * @return apikey
     */
    static String getApikey(String key_str) {
        String apikey = null;
        Document doc = Jsoup.parse(key_str);
        Element inf = doc.body().getElementById("wrap").
                getElementById("main").getElementById("m_ext").
                getElementById("main2").getElementById("content").
                select("table").first().
                select("tbody").first();

        for (Element e : inf.select("tr")) {
            Element f = e.select("td").first().
                    select("table").first();
            if (f != null) {
                //if(f.select("tbody").first()!=null);
                Element ff = f.select("tbody").first().
                        select("tr").first().
                        select("th").first();

                if (null != ff && "API Key".equals(ff.text())) {
                    apikey = ff.nextElementSibling().text();
                    break;
                }
            }
        }
        return apikey;
    }

}

