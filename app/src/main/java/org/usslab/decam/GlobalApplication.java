package org.usslab.decam;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.stericson.RootShell.RootShell;

import org.usslab.decam.Base.ActivityCollector;
import org.usslab.decam.Util.BlackMacLists;

import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by pip on 2017/1/27.
 */

public class GlobalApplication extends Application {
    //create In system's and app's Global variables;

    private static Context context;
    private static DatagramSocket datagramSocket;
    private static int port;
    private static BlackMacLists blackMacLists;

    @Override
    public void onCreate(){
        super.onCreate();

        Logger.init("PLog");
        RootShell.debugMode=false;
        blackMacLists=new BlackMacLists(getResources().getString(R.string.black_mac_list_db_filename),
                getAssets());
        context=getApplicationContext();

        bindPort(5555);


    }
    public static void setDatagramSocket(DatagramSocket s){
        datagramSocket=s;
    }

    public static Context getContext(){
        return context;
    }
    public static DatagramSocket getDatagramSocket(){
        return datagramSocket;
    }
    public static boolean bindPort(int port){
        GlobalApplication.port=port;
        if (datagramSocket==null) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        GlobalApplication.setDatagramSocket(new DatagramSocket(GlobalApplication.port,
                                InetAddress.getLocalHost())
                        );
                    } catch (Exception e) {
                        Logger.e(e, "DataGram Error");
                    }
                }
            }).start();
        }
        return true;

    }




}
