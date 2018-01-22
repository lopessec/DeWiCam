package org.usslab.decam.Util;

import android.util.Log;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.security.spec.ECField;

/**
 * Created by pip on 2017/1/29.
 */

public class FileOperation {
    public static final String TAG="FileOperation";
    public static final String Packet_POST_FIX=".pcap";
    public static final String Packet_PRE_FIX ="tmp";
    public static final int PACKET_FILE_DELETE_CODE=23;
    public static final int LIST_FILE_DIR=24;
    private static StringBuilder cmdoutput=new StringBuilder();


    public static boolean deleteTmpPcapFiles(String path){
        StringBuilder shellcmd=new StringBuilder();
        shellcmd.append("rm ").append(path).append('/')
                .append(Packet_PRE_FIX).append("*").append(Packet_POST_FIX);
        Log.d(TAG,shellcmd.toString());
        Command deleteFileshellCmd=new Command(PACKET_FILE_DELETE_CODE,shellcmd.toString());
        try{
            RootTools.getShell(true).add(deleteFileshellCmd);
            //use normal shell;
        }catch (Exception e){
            Log.e(TAG,"error:"+shellcmd.toString(),e);
        }

        return true;
    }

}
