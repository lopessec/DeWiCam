package org.usslab.decam.Data;

/**
 * Created by pip on 2017/2/9.
 */

//the data structure of packet for the needs of analysis infomation
//before using everything,please check isNull();


public class PacketInfo {
    public static final char sp='|';


    public long receivedTime;

    public int count;
    public int length;      //full length of packets,though sometimes the receiver may drop some last bytes,
                            // eg:only save the first 1024byte,drop the left;
                            // position:in pcap header(Packet._rawheader),the last 4 bytes,12~15;

    public int channel;   //channel infomation in RadioTapHeader,
                            //position:(Packet._rawdata),2 bytes, 0x12~0x13;


    public byte signalStrength;//signal info of this packets,   0xcd==-51 db
                                //position:(Packet._rawdata), 1 byte, 0x16;signed char in C;

    //(Packet._rawdata)
    public byte type;//frame control feilds,byte index:30,;
    public byte flags;//only need last 2 bit,byte index:31;
    public int durationT;// 2 bytes,index:32~33;
    public String macDes,macSrc,macTrans;//,macAll;//each of mac address: 6 bytes;
    //  index      34~39, 40~45,  46~51

    private static StringBuilder macAllstr=new StringBuilder();



    public long getReceivedTime(){
        return receivedTime;
    }

    public int getCount(){
        return count;
    }
    public static int getsCount(PacketInfo i){
        return i.getCount();
    }
    public int getLength(){
        return length;
    }
    public static int getsLength(PacketInfo i){
        return i.getLength();
    }
    public int getChannel(){
        return channel;
    }
    public static int getsChannel(PacketInfo i){
        return i.getChannel();
    }
    public byte getSignalStrength(){
        return signalStrength;
    }
    public static byte getsSignalStrength(PacketInfo i){
        return i.getSignalStrength();
    }
    public byte getType(){
        return type;
    }
    public static byte getsType(PacketInfo i){
        return i.getType();

    }
    public byte getFlags(){
        return flags;
    }
    public static byte getsFlags(PacketInfo i){
        return i.getFlags();
    }
    public int getDurationT(){
        return durationT;
    }
    public String getMacDes(){
        return macDes;
    }
    public String getMacSrc(){
        return macSrc;
    }
    public static int getsDurationT(PacketInfo i){
        return i.getDurationT();
    }
    public String getMacAll(){
        //
        //Source Mac|Transmitter Mac|Destination Mac
        //S         |T              |D
        //


        //=new StringBuilder();
        macAllstr.delete(0,macAllstr.length());//clear;


        macAllstr.append(macSrc).append(sp)
                .append(macTrans).append(sp)
                .append(macDes);
        return macAllstr.toString();
    }




    public static String getsMacAll(PacketInfo i){
        return i.getMacAll();
    }



    //it's just what we needs by now;
}
/*
enum Channel{
    C1,C2,C3,C4,C5,C6,C7,C8,C9,C10,C11
}
*/


//Pcap Data Structure reference:http://www.cnblogs.com/caoguoping100/p/3658792.html