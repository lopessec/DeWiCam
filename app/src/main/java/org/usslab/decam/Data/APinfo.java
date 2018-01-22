package org.usslab.decam.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pip on 2017/2/11.
 */
public class APinfo {
    public static final String UNKNOW_AP_SSID="UnknowAP";
    public static final String USSLAB_SSID="USSLAB";
    public static final String FAKE_APSSID="TP-LINK_Lab_AP";
    //HashMap
    private static Map<String,String> macToNameMap=new HashMap<>();
    private static Map<String,String> nameToMacMap=new HashMap<>();


    public static String fromMacfindAPName(String n){
        //know Mac,find ssid name;
        String name=macToNameMap.get(n);
        if (name.equals(USSLAB_SSID))
            return FAKE_APSSID;
        else {
            return name;
        }


    }
    public static String fromNamefindAPMac(String m){
        //know ap's name, find his Mac;
        return nameToMacMap.get(m);

    }
    public static void insertMacNamePair(String mac,String name){
        if (macToNameMap.containsKey(mac)){
            //prevent duplicate added;
            return;
        }
        macToNameMap.put(mac,name);
        nameToMacMap.put(name,mac);

    }

    public static void clearCache(){

        macToNameMap.clear();
        nameToMacMap.clear();

    }

}
