package org.usslab.decam.Data;

import org.usslab.decam.Algo.Core;

import java.security.PublicKey;
import java.util.List;


/**
 * Created by pip on 2017/2/11.
 */

public class ResultAPCams {
    public int APid;
    public String charname;
    public String bssid;

    public List<Core.ResultCamInfo> detectedCams;

    public String getCharname(){
        return charname;
    }
    public int getAPId(){
        return APid;
    }
    public String getBssid(){
        return bssid;
    }
    public List<Core.ResultCamInfo> getDetectedCams(){
        return detectedCams;
    }




}
