package org.usslab.decam.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by pip on 2017/2/15.
 */

public class CAMinfo {
    //a cams first show time;
    //and his last seen time;

    public static final String UNKNOW_TIME="0xff";
    private static Map<String ,CameraInfoDetail> camsDetails=new HashMap<>();

    private static long starttime;
    private static boolean allIsConducting;

    public static class CameraInfoDetail{
        public long camfirstshowtime;
        public long camlastseentime;
        public boolean ishomed=false;
        public boolean conductIsHomed=false;
    }

    public static long findACamInfo(String mac){
        //default camera's info init function;
        if (camsDetails.containsKey(mac)){
            return camsDetails.get(mac).camfirstshowtime;
        }else {
            long currentTime = System.currentTimeMillis();
            CameraInfoDetail cameraInfoDetail=new CameraInfoDetail();
            cameraInfoDetail.camfirstshowtime=currentTime;
            cameraInfoDetail.camlastseentime=currentTime;
            camsDetails.put(mac,cameraInfoDetail);
            return currentTime;
        }

    }
    public static void setLastSeen(String mac){
        long currentTime = System.currentTimeMillis();
        camsDetails.get(mac).camlastseentime=currentTime;
    }
    public static long getLastSeen(String mac){
        if (camsDetails.containsKey(mac))
            return camsDetails.get(mac).camlastseentime;
        else {
            return System.currentTimeMillis();
        }
    }
    public static boolean getConductingIsHomed(){
        return allIsConducting;
    }
    public static void setConductingIsHomed(boolean v){
        allIsConducting=v;
    }

    public static boolean getIsHomed(String mac){
        if (camsDetails.containsKey(mac)){
            return camsDetails.get(mac).ishomed;
        }else
            return false;
    }
    public static void setIsHomed(String mac,boolean v){
        if (camsDetails.containsKey(mac))
            camsDetails.get(mac).ishomed=v;
    }
    public static Set<String> getCameraList(){
        return camsDetails.keySet();
    }

    public static void cleanInfo(){
        camsDetails.clear();
        allIsConducting=false;
        //setStarttime(System.currentTimeMillis());
    }

    public static boolean isACamera(String v){
        return camsDetails.containsKey(v);
    }
    public static long getStarttime(){
        return starttime;
    }
    public static void setStarttime(long x){
        starttime=x;
    }
}
