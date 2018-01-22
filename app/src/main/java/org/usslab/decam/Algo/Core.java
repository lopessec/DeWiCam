package org.usslab.decam.Algo;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import org.usslab.decam.Data.PacketInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.stream.Collectors;//until SDK to JDK1.8

import org.apache.commons.math3.stat.StatUtils;


/**
 * Created by pip on 2017/2/10.
 */
public class Core {

    public static int FASTMODE_CONDUCT_PACKET_INFO_LIST_LEN = 100;

    private List<PacketInfo> packinfolists;
    private Map<String, List<PacketInfo>> flowsContainer = new HashMap<>();
    private ResultStru fastModeRes;
    private Map<String,FlowLDVStru> flowsResContainer=new HashMap<>();


    private Gson agson = new Gson();

    public Core(List<PacketInfo> listx){
        this.packinfolists=listx;
    }
    public Core(){
        this.packinfolists=null;
    }


    public void updateList(List<PacketInfo> listx){
        this.packinfolists=listx;
    }
    public ResultStru fastMode(){
        return fastMode(new ResultStru());
    }

    public ResultStru fastMode(ResultStru resultPosition){

        fastModeRes=resultPosition;
        //fastModeRes.ap_Cams_infomation.clear();

        gatherSameFlow();
        plfeature();
        //now result is in flowsResContainer;

        for(String eachFlowLDVresKey:this.flowsResContainer.keySet()){
            FlowLDVStru aFlowResult=this.flowsResContainer.get(eachFlowLDVresKey);
            if(aFlowResult.isCamera){

                String currnetAP=aFlowResult.bssid;

                if(!fastModeRes.ap_Cams_infomation.containsKey(currnetAP)){
                    fastModeRes.ap_Cams_infomation.put(currnetAP,new ArrayList<ResultCamInfo>());
                }
                //check if already contain this camera;
                List<ResultCamInfo> aApsKnownCameras=fastModeRes.ap_Cams_infomation.get(currnetAP);

                //TODO:update Data structure use hashmap to locate cameras   Map<String camsMac,ResultCamInfo>
                boolean currentCamsMacFound=false;
                ResultCamInfo foundCam=null;
                for (ResultCamInfo aCaminfo:aApsKnownCameras){
                    if (aCaminfo.macaddr.equals(aFlowResult.thisgroupsCamInfo.macaddr)){
                        currentCamsMacFound=true;
                        foundCam=aCaminfo;
                        break;
                    }
                }


                if(!currentCamsMacFound){
                    aApsKnownCameras.add(aFlowResult.thisgroupsCamInfo);
                }else {
                    //int knowCamIndex=aApsKnownCameras.indexOf(foundCam);
                    aApsKnownCameras.remove(foundCam);
                    aApsKnownCameras.add(aFlowResult.thisgroupsCamInfo);
                    //if found cam in known list;

                }




                fastModeRes.countCameraNumbers+=1;
            }
        }




        return fastModeRes;
    }

    public void plfeature(){
        //max len packets' ratio
        //generate each flow's feature;
        flowsResContainer.clear();

        int flowLength;
        int aflowmaxPacketLengthcount;


        double maxLengthPacketsRatio;
        int currentFlowMaxPacketlen=0;

        for(String current3Mac:this.flowsContainer.keySet()){
            //for each flow;
            int audioCount=0;
            List<PacketInfo> aflowlist=this.flowsContainer.get(current3Mac);
            flowLength=aflowlist.size();

            //extract packets' each length;
            //List<Integer> packetsLengthList=aflowlist.stream().map(PacketInfo::getsLength).collect(Collectors.toList());
            //packetsLengthArray=new int[flowLength];
            aflowmaxPacketLengthcount=0;
            currentFlowMaxPacketlen=0;
            for(int i=0,apcketLength;i<flowLength;i++){
                apcketLength=aflowlist.get(i).getLength();
                if( apcketLength > currentFlowMaxPacketlen ){
                    currentFlowMaxPacketlen = apcketLength;
                    aflowmaxPacketLengthcount = 1;
                }else if (currentFlowMaxPacketlen == apcketLength){
                    aflowmaxPacketLengthcount ++;
                }
                if (apcketLength<600 && apcketLength>200){
                    //count packet may be a audio;
                    audioCount++;
                }

            }

            //aflowmaxLength=StatUtils.max(packetsLengthArray);

            maxLengthPacketsRatio=aflowmaxPacketLengthcount*100.0/flowLength;
            //-----------------------------------------------------------------------
            //-----------------------------------------------------------------------



            double[] flowsDuration=new double[flowLength];
            for (int i=0;i<flowLength;i++){
                flowsDuration[i]=aflowlist.get(i).getDurationT();
            }
            double aflowDurationsVariance=StatUtils.variance(flowsDuration);



            //-----------------------------------------------------------------------
            //-----------------------------------------------------------------------

            double volumeRatio=flowLength*100.0/packinfolists.size();

            //-----------------------------------------------------------------------
            flowsResContainer.put(current3Mac,
                    new FlowLDVStru(
                            maxLengthPacketsRatio,
                            aflowDurationsVariance,
                            volumeRatio)
            );




            //finish flow feature generate;
            MidLDVInfo midLDVInfo=new MidLDVInfo(currentFlowMaxPacketlen, aflowDurationsVariance,
                    flowLength, volumeRatio,
                    maxLengthPacketsRatio,current3Mac,
                    aflowmaxPacketLengthcount);

            Logger.json(agson.toJson(midLDVInfo));




            if (currentFlowMaxPacketlen  >Constants.currentFlowMaxPacketlen &&
                    maxLengthPacketsRatio>Constants.maxLengthPacketsRatioL  &&
                    maxLengthPacketsRatio<Constants.maxLengthPacketsRatioR  &&
                    ((volumeRatio>Constants.volumeRatio[0]                  &&
                        (aflowDurationsVariance>Constants.aflowDurationsVariance[0]||
                                (aflowDurationsVariance!=Constants.aflowDurationsVariance[1] && volumeRatio > Constants.volumeRatio[1])
                        )
                    ) || (volumeRatio>Constants.volumeRatio[2] && aflowDurationsVariance>Constants.aflowDurationsVariance[2]))
                )
            {

                flowsResContainer.get(current3Mac).isCamera=true;
                flowsResContainer.get(current3Mac).thisgroupsCamInfo=new ResultCamInfo();


                //camera Audio:
                double audioPercent=audioCount*100.0/flowLength;
                double reversePercent=(100-maxLengthPacketsRatio-audioPercent);

                if((audioPercent/reversePercent>0.2 && audioPercent>10) ||
                        audioPercent/reversePercent>0.5
                        ){
                    //
                    flowsResContainer.get(current3Mac).thisgroupsCamInfo.cameraHasAudio=true;

                }


                //camera Resolve;
                if (maxLengthPacketsRatio < 35){
                    //current flow apply to a Resolution;
                    flowsResContainer.get(current3Mac).thisgroupsCamInfo.resolution=CAM_RES.LOW;

                }else if (maxLengthPacketsRatio <60){
                    flowsResContainer.get(current3Mac).thisgroupsCamInfo.resolution=CAM_RES.MID;

                }else {
                    flowsResContainer.get(current3Mac).thisgroupsCamInfo.resolution=CAM_RES.HIGH;
                }


                flowsResContainer.get(current3Mac).bssid=aflowlist.get(0).getMacDes();
                flowsResContainer.get(current3Mac).thisgroupsCamInfo.macaddr=aflowlist.get(0).getMacSrc();


            }


        }

    }


    public Map<String,List<PacketInfo>> gatherSameFlow(){
        this.flowsContainer.clear();//reset hash-map;
        for (PacketInfo apacket:this.packinfolists){
            if (apacket==null){
                //in case of any uncareful Null data passed in;
                continue;
            }
            //each packet has his 3 macAddress,3mac is the same,
            //then put them into <key=3macAddr,value=list.append(thispacket)> togather.
            String current3MacAddr=apacket.getMacAll();
            if (!flowsContainer.containsKey(current3MacAddr)){//create a new key-value pair and put it in;
                flowsContainer.put(current3MacAddr,new ArrayList<PacketInfo>());

            }
            flowsContainer.get(current3MacAddr).add(apacket);
        }
        //gather featured packets OVER!
        return this.flowsContainer;

    }


    public class FlowLDVStru{
        public double maxLengthPacketsRatio,durationsVariance,currentFlowInAllPacketinfosRatio;

        public String bssid;//target ssid;
        public boolean isCamera=false;
        public ResultCamInfo thisgroupsCamInfo;


        public FlowLDVStru(double maxLengthPacketsRatio, double durationsVariance, double currentFlowInAllPacketinfosRatio) {
            this.maxLengthPacketsRatio = maxLengthPacketsRatio;
            this.durationsVariance = durationsVariance;
            this.currentFlowInAllPacketinfosRatio = currentFlowInAllPacketinfosRatio;
        }

    }
    public static class ResultStru{
        public int countCameraNumbers;//this time ,passed in packetlist;
        //each ap' cameras list;
        public Map<String,List<ResultCamInfo>> ap_Cams_infomation;//AP:his sons of camera;
        public ResultStru(){

            countCameraNumbers=0;
            ap_Cams_infomation=new HashMap<>();
        }

    }
    public static class ResultCamInfo{
        public int camID;
        public String macaddr;
        public CAM_RES resolution;
        public boolean cameraIsHomed=false;
        public boolean cameraHasAudio=false;
        public ResultCamInfo(){

        }

    }
    public static ResultCamInfo getAResultCamInfoInstance(){
        return new ResultCamInfo();
    }

    protected static class MidLDVInfo{
        int currentFlowMaxPacketlen,flowLength,aflowmaxPacketLengthcount;
        double maxLengthPacketsRatio,volumeRatio,aflowDurationsVariance;
        String mac3addr;

        public MidLDVInfo(int currentFlowMaxPacketlen, double aflowDurationsVariance, int flowLength, double volumeRatio, double maxLengthPacketsRatio,String mac3addr,int aflowmaxPacketLengthcount) {
            this.currentFlowMaxPacketlen = currentFlowMaxPacketlen;
            this.aflowDurationsVariance = aflowDurationsVariance;
            this.mac3addr=mac3addr;
            this.flowLength = flowLength;
            this.volumeRatio = volumeRatio;
            this.maxLengthPacketsRatio = maxLengthPacketsRatio;
            this.aflowmaxPacketLengthcount=aflowmaxPacketLengthcount;
        }
    }

    public enum CAM_RES{
        LOW,MID,HIGH
    }
}
