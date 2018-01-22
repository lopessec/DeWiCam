package org.usslab.decam.Background;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.orhanobut.logger.Logger;
import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import org.usslab.decam.Base.BaseService;
import org.usslab.decam.Util.Logg;
import java.util.HashMap;


public class MonitorModeController {


    private MODE status=MODE.NORMAL;
    private MonitorModeConfig config=new MonitorModeConfig();
    //original author use BroadCast to control,but we just open/close.
    //use binder instead;

    public boolean mstartMonitorMode(){

        config.startMonitorMode();
        return true;
    }
    public boolean mstopMonitorMode(){

        config.stopMonitorMode();
        return true;

    }
    public boolean reverseMonitorMode(){
        if(MODE.NORMAL==status){
            return mstartMonitorMode();
        }else {
            return mstopMonitorMode();
        }

    }
    public void msetWlanChannel(String value){
        this.config.setWlanChannel(value);
    }
    public String mgetWlanChannel(){
        return this.config.getWlanChannel();
    }

    public MODE checkMonitoMode(){
        //MonitorModeConfig.this
        if(config.checkMode()){
            status=MODE.MONITOR;
        }else {
            status=MODE.NORMAL;
        }
        return status;
    }
}
enum MODE{
    NORMAL,MONITOR,UNKNOWN,PROMISCUOUS
}

class MonitorModeConfig {

    public static final int COMMAND_CHECK_MONITOR = 30;

    public static final String TAG = "MonitorMode";



    private String target_channel = "11";
    private boolean warningFlag = true;
    private boolean checkedMode=false;
    protected boolean checkMode() {

        final Command command = new Command(COMMAND_CHECK_MONITOR, /*"nexutil -m",*/ "nexutil -n") {
            //generate new root shell cmd;
            @Override
            public void commandOutput(int id, String line) {
                if (id == COMMAND_CHECK_MONITOR) {
                    if (line.contains("monitor: 0")) {
                        checkedMode=false;
                    }else {
                        checkedMode=true;
                    }
                }
                super.commandOutput(id, line);
            }
        };

        try {
            RootTools.getShell(true).add(command);
        } catch (Exception e) {
            Logg.e(TAG, "CheckMonitorMode", e);
        }
        return checkedMode;
    }



    protected void startMonitorMode() {
        if (this.warningFlag){
            Logg.w(TAG,"Remember to setup Wlan channel");
        }

        final Command command_set_channel = new Command(0, "nexutil -i -s 30 -v " + this.target_channel);

        try {
            RootTools.getShell(true).add(command_set_channel);
        } catch(Exception e) {
            Logg.e(TAG,"setWlanChannel",e);
        }
        //------------------------------------------------------------------------------------------

        final Command command = new Command(COMMAND_CHECK_MONITOR, /*"nexutil -m",*/ "nexutil -n") {
            //generate new root shell cmd;
            @Override
            public void commandOutput(int id, String line) {
                if(id == COMMAND_CHECK_MONITOR) {
                    if(line.contains("monitor: 0")) {
                        try {
                            RootTools.getShell(true).add(new StartMonitorModeCommand());

                        } catch(Exception e) {

                            Logg.e(TAG,"startMonitorMode",e);
                        }
                    }
                }

                super.commandOutput(id, line);
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RootTools.getShell(true).add(command);
                } catch(Exception e) {Logg.e(TAG,"startMonitorMode",e);}
            }
        }).start();
        Logg.d(TAG,"startMonitorMode Done~");
    }

    protected void stopMonitorMode() {

        final Command command = new Command(COMMAND_CHECK_MONITOR, /*"nexutil -m",*/ "nexutil -n") {
            @Override
            public void commandOutput(int id, String line) {
                if(id == COMMAND_CHECK_MONITOR) {
                    if(line.contains("monitor: 1")) {
                        try {
                            RootTools.getShell(true).add(new StopMonitorModeCommand());

                        } catch(Exception e) {Logg.e(TAG,"stopMonitorMode",e);}
                    }
                }

                super.commandOutput(id, line);
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RootTools.getShell(true).add(command);
                    //move into subthreads
                } catch(Exception e) {Logg.e(TAG,"stopMonitorMode",e);}
            }
        }).start();
        Logg.d(TAG,"stopMonitorMode Done~");
    }


    public void setWlanChannel(String channel) {
        //only effect channel setting when it comes to startMonitormode cmd;
        this.target_channel=channel;
        this.warningFlag=false;
    }
    public String getWlanChannel(){
        return this.target_channel;
    }

    private class StartMonitorModeCommand extends Command {
        public static final int COMMAND_START_MONITOR = 31;

        public StartMonitorModeCommand() {
            super(COMMAND_START_MONITOR, "nexutil -s 52 -c1 -m true",
                    "rawproxy -i wlan0 -p " + android.os.Process.myPid() + " &");
                    //"rawproxyreverse -i wlan0 -p " + android.os.Process.myPid() + " &");
        }
    }

    private class StopMonitorModeCommand extends Command {
        public static final int COMMAND_STOP_MONITOR = 32;

        public StopMonitorModeCommand() {
            super(COMMAND_STOP_MONITOR, "nexutil -c0 -m 0", "pkill rawproxy" );//  , "pkill rawproxyreverse");
        }
    }

}
