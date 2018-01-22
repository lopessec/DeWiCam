package org.usslab.decam.UI;

import org.usslab.decam.Algo.Core;
import org.usslab.decam.Data.APinfo;
import org.usslab.decam.Data.ResultAPCams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by pip on 2017/2/13.
 */

public class ResAlgoToAPAdapter {
    //convert Algo's result to APadapter's lists;
    private List<ResultAPCams> resultAPCamsList;

    public ResAlgoToAPAdapter(){
        resultAPCamsList=new ArrayList<>();
    }
    public ResAlgoToAPAdapter(List<ResultAPCams> allocatedList){
        resultAPCamsList=allocatedList;

    }

    public List<ResultAPCams> convertAlgoToAdater(Core.ResultStru resFromAlgo){
        resultAPCamsList.clear();//reset

        Map<String,List<Core.ResultCamInfo>> mapperFromAlgo=resFromAlgo.ap_Cams_infomation;
        int ap_number=0;
        for(String ap_bssid:mapperFromAlgo.keySet()){
            ResultAPCams aAPandHisCams=new ResultAPCams();
            aAPandHisCams.APid=ap_number;
            aAPandHisCams.bssid=ap_bssid;
            aAPandHisCams.detectedCams=mapperFromAlgo.get(ap_bssid);

            if (APinfo.fromMacfindAPName(ap_bssid)!=null){//put his ssid;
                aAPandHisCams.charname=APinfo.fromMacfindAPName(ap_bssid);
            }else {
                aAPandHisCams.charname=APinfo.UNKNOW_AP_SSID;
            }
            resultAPCamsList.add(aAPandHisCams);

            ap_number++;

        }
        return resultAPCamsList;

    }

    public List<ResultAPCams> getResultAPCamsList(){
        return resultAPCamsList;
    }
}
