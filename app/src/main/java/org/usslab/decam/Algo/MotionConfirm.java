package org.usslab.decam.Algo;

import org.usslab.decam.Data.PacketInfo;
import org.usslab.decam.Util.Logg;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pip on 2017/4/29.
 */

public class MotionConfirm {
    // functions could without instanced to use;
    public static final int[] TIME_GAP = {5, 15, 5};

    public static int DIFFERENCE = 80;

    public static long computeBitrate(String mac, List<PacketInfo> packetInfos, int startPointer, int endPointer) {
        // compute bitrate
        if (packetInfos.size() == 0) {
            return -1;
        }
        List<Long> bitRates = new ArrayList<>();
        int camPacketsLength = 0;
        int j = 0;
        long startStamp = 0;
        long endStamp = 0;

        long totalChange = 0;
        for (int i = startPointer; i < endPointer; i++) {
            PacketInfo packet = packetInfos.get(i);
            if (packet.getMacSrc().equals(mac)) {
                if (j == 0) {
                    startStamp = packet.getReceivedTime();
                }
                endStamp = packet.getReceivedTime();
                camPacketsLength += packet.getLength();
                j++;
                if (j == 5) {
                    long littleTimeGap = endStamp-startStamp;
                    long bitRate = (long) (camPacketsLength*8.0*1000 / littleTimeGap);
                    if (bitRates.size() > 1) {
                        totalChange += Math.abs(bitRate-bitRates.get(bitRates.size()-1));
                    }

                    Logg.d("littleBitRate", camPacketsLength+" "+littleTimeGap+" "+bitRate+"");
                    bitRates.add(bitRate);
                    j = 0;
                    camPacketsLength = 0;
                }
            }
        }
        if (j > 1) {
            long littleTimeGap = endStamp-startStamp;
            long bitRate = (long) (camPacketsLength*8.0*1000 / littleTimeGap);
            totalChange += Math.abs(bitRate-bitRates.get(bitRates.size()-1));
            bitRates.add(bitRate);
        }
        long totalBitRate = 0;
        for (long bitRate: bitRates) {
            totalBitRate += bitRate;
        }
        Logg.d("bitRateChange", totalChange/(bitRates.size()-1)+"");
        long averageBitRate = totalBitRate / bitRates.size();
        return averageBitRate;
    }

    /*
    * Accept a confirmed camera's mac address and sum up to TIME_GAP's period packetInfo,
    * Return this mac's is in_home boolean value;
    * */
    public static boolean confirmAMacsPacketInfo(String mac,
                                                 List<PacketInfo>packetInfos,
                                                 int firstPointer,
                                                 int secondPointer,
                                                 int thirdPointer) {
        long stillBitRate = computeBitrate(mac, packetInfos, 0, firstPointer);
        long moveBitRate = computeBitrate(mac, packetInfos, firstPointer, secondPointer);
        long stillAgainBitRate = computeBitrate(mac, packetInfos, secondPointer, thirdPointer);

        Logg.d("difference", stillBitRate + " " + moveBitRate + " " + stillAgainBitRate);
        Logg.d("difference", moveBitRate-stillBitRate + " " + (moveBitRate-stillAgainBitRate));
        if (moveBitRate-stillBitRate>DIFFERENCE && moveBitRate-stillAgainBitRate >DIFFERENCE) {
            return true;
        }
        return false;
    }
}

//
//interface MotionConfirmInterface{
//    boolean confirmAMacsPacketInfo(String mac, PacketInfo packetInfos[]);
//}

