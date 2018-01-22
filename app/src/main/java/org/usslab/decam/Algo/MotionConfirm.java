package org.usslab.decam.Algo;

import org.usslab.decam.Data.PacketInfo;

import java.util.List;

/**
 * Created by pip on 2017/4/29.
 */

public class MotionConfirm {
    // functions could without instanced to use;
    public static final int[] TIME_GAP={10,10,10};

    /*
    * Accept a confirmed camera's mac address and sum up to TIME_GAP's period packetInfo,
    * Return this mac's is in_home boolean value;
    * */
    public static boolean confirmAMacsPacketInfo(String mac, List<PacketInfo> packetInfos){
        return false;
    }
}

//
//interface MotionConfirmInterface{
//    boolean confirmAMacsPacketInfo(String mac, PacketInfo packetInfos[]);
//}

