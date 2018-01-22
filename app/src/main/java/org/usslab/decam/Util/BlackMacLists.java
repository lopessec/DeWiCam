package org.usslab.decam.Util;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;

import org.usslab.decam.Base.BaseActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by pip on 2017/3/27.
 */

public class BlackMacLists{
    public static final String TAG="BlackMacLists";
    public static final String DEFAULT_DIC_VALUE="";

    public static String blackMacListFileName;
    public static Map<String,String> blackMacDict;


    public BlackMacLists(String filename, AssetManager manager){
        blackMacListFileName=filename;
        blackMacDict=new Hashtable<>();
        try {
            InputStream is = manager.open(blackMacListFileName);
            loadTextFromSDcard(is);
            is.close();
        } catch (Exception e) {
            Logg.e(TAG,"init BlackList error",e);
        }
    }

    public static boolean isBlackMac(String macAddr){
        if (blackMacListFileName==null){
            //ignore case;
            Logg.w(TAG,"BlackList is not initialized");
            return false;
        }
        //regular key:
        String keyFormationMacAddr=macAddr.substring(0,8);

        if (blackMacDict.containsKey(keyFormationMacAddr))
            return true;
        else
            return false;
    }


    /**
     * 按行读取txt
     *
     * @param is
     *
     * @throws Exception
     */
    private static void loadTextFromSDcard(InputStream is) throws Exception {
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String strAline,strPure;
        while ((strAline = bufferedReader.readLine()) != null) {
            strPure=strAline.trim().toUpperCase();
            if(strPure.length()<=6){
                continue;
            }
            blackMacDict.put(strPure,DEFAULT_DIC_VALUE);
        }

    }
}
