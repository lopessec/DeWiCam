package org.usslab;

import org.junit.Test;
import org.usslab.decam.Data.Packet;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void test_stringBuilder() throws Exception {
        StringBuilder shellcmd=new StringBuilder("/sdcard-");
        shellcmd.append('/');
        System.out.println(shellcmd.toString());
        shellcmd.append("*.pcap");
        System.out.println(shellcmd);

        shellcmd.delete(1,shellcmd.length());
        shellcmd.delete(1,shellcmd.length());

        System.out.println(shellcmd);
    }


    @Test
    public void test_packetsInfoBuild() throws Exception{
        byte[] header=new byte[16];
        byte[] data=new byte[Data.rawData.length];
        for(int i=0;i<Data.rawheader.length;i++){
            header[i]=(byte)Data.rawheader[i];
        }
        for(int i=0;i<Data.rawData.length;i++){
            data[i]=(byte)Data.rawData[i];
        }




        Packet aPacket=new Packet(data,header,0);
        System.out.println(aPacket.getFullLength());
        System.out.println(aPacket.getPacketAllMac());

    }
}