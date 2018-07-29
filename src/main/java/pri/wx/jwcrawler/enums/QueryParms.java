package pri.wx.jwcrawler.enums;

import java.util.HashMap;
import java.util.Map;

public enum QueryParms {

    LOGIN("Login", "login"),
    STU_INF("Default", "inf"),
    API_KEY("Client", "index"),
    MAKE_KEY("Client", "apply"),
    NULL(null, null),

    ;



    private String keyOfC;
    private String valueOfC;

    private String keyOfA;
    private String valueOfA;

    QueryParms(String v_c, String v_a) {
        keyOfC = "c";
        valueOfC = v_c;
        keyOfA = "a";
        valueOfA = v_a;
    }

    public Map<String, String> query() {
        Map<String, String> query = new HashMap<>();
        query.put(keyOfC, valueOfC);
        query.put(keyOfA, valueOfA);
        return query;
    }

    public static Map<String, String> apiQuery(String apikey) {
        Map<String, String> query = new HashMap<>();
        query.put(JsonParms.APIKEY.value(), apikey);
        return query;
    }

    public static Map<String, String> apiTermQuery(String apikey,String tmId) {
        Map<String, String> query = apiQuery(apikey);
        query.put(JsonParms.TM_ID.value(), tmId);
        return query;
    }

}
