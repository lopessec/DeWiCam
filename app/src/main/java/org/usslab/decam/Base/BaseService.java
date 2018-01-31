package org.usslab.decam.Base;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.lang.StackTraceElement;
import android.content.Context;
import android.os.Vibrator;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.usslab.decam.GlobalApplication;
import org.usslab.decam.Util.Logg;


public class BaseService extends Service {
    public static String TAG="BaseService";
    Vibrator vibrator = null;
    public BaseService() {

    }
    @Override
    public void onCreate(){
        super.onCreate();
        String info="onCreate";
        Logger.t(this.getClass().getSimpleName()).d(info);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        String info="onDestroy";
        Logger.t(this.getClass().getSimpleName()).d(info);

    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        String info="onStartCommand";
        Logger.t(this.getClass().getSimpleName()).d(info);
        return super.onStartCommand(intent,flags,startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        String info="onBind";
        Logger.t(this.getClass().getSimpleName()).d(info);
        throw new UnsupportedOperationException("Not implemented AbstractMethod");
    }

    protected void mkToast(final String msg){

        Runnable toastFromserve=new Runnable() {
            @Override
            public void run() {
                Toast.makeText(GlobalApplication.getContext(),msg,Toast.LENGTH_SHORT).show();
            }
        };
        if (ActivityCollector.activities.size()>0)
            ActivityCollector.activities.get(0).runOnUiThread(toastFromserve);

    }

    protected void doVibrate() {
        if (ActivityCollector.activities.size()>0) {
            vibrator = (Vibrator) ActivityCollector.activities.get(0).getSystemService(VIBRATOR_SERVICE);
        }
        if (vibrator != null) {
            vibrator.vibrate(1*1000);
        }
    }

}
