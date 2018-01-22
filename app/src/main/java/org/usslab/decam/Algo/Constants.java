package org.usslab.decam.Algo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Interpolator;

import org.usslab.decam.GlobalApplication;
import org.usslab.decam.R;

/**
 * Created by pip on 2017/2/12.
 */
public class Constants {
    public static int currentFlowMaxPacketlen=1000;


    public static int
            maxLengthPacketsRatioL=5,
            maxLengthPacketsRatioR=95;
    public static int[] volumeRatio={10,15,3};
    public static int[] aflowDurationsVariance={100,0,500};

    private static Context context;
    private static SharedPreferences sharedPreferencesObj;

    public static void setValueFromSettings(){
        String tmp;
        int tmpi[];
        if (context==null ){
            context= GlobalApplication.getContext();
        }
        if (sharedPreferencesObj==null){
            sharedPreferencesObj=context.getSharedPreferences(
                    context.getString(R.string.pref_setting_file_name),Context.MODE_PRIVATE);

        }
        //load data from pref_config;
        tmp = sharedPreferencesObj.getString(context.getString(R.string.pref_key_algo_seq_len), "300");
        Core.FASTMODE_CONDUCT_PACKET_INFO_LIST_LEN = Integer.valueOf(tmp);

        //TODO:consider a more effective way to load config;and solve the invalid input when careless
        tmp = sharedPreferencesObj.getString(context.getString(R.string.pref_key_algo_1), "");
        if (!tmp.equals("")) {
            tmpi = extractValue(tmp);
            if (tmpi.length < 2)
                return;
            maxLengthPacketsRatioL = tmpi[0];
            maxLengthPacketsRatioR = tmpi[1];
        }
        tmp = sharedPreferencesObj.getString(context.getString(R.string.pref_key_algo_2), "");
        if (!tmp.equals("")) {
            tmpi = extractValue(tmp);
            if (tmpi.length < 3)
                return;
            volumeRatio = tmpi;
        }
        tmp = sharedPreferencesObj.getString(context.getString(R.string.pref_key_algo_3), "");

        if (!tmp.equals("")) {
            tmpi = extractValue(tmp);
            if (tmpi.length < 3)
                return;
            aflowDurationsVariance = tmpi;
        }


    }

    private static int[] extractValue(String strx) {
        //split string with " ";
        String[] strv=strx.split(" ");
        int[] res=new int[strv.length];
        for (int i=0;i<strv.length;i++) {
            res[i]=Integer.valueOf(strv[i]);
        }
        return res;
    }

}
