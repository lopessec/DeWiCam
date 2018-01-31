package org.usslab.decam.Background;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;

import org.usslab.decam.Algo.Constants;
import org.usslab.decam.Algo.Core;
import org.usslab.decam.Algo.MotionConfirm;
import org.usslab.decam.Base.BaseService;
import org.usslab.decam.Data.APinfo;
import org.usslab.decam.Data.CAMinfo;
import org.usslab.decam.Data.Packet;
import org.usslab.decam.Data.PacketInfo;
import org.usslab.decam.Data.ResultAPCams;
import org.usslab.decam.R;
import org.usslab.decam.UI.APAdapter;
import org.usslab.decam.UI.ResAlgoToAPAdapter;
import org.usslab.decam.UI.StartMainOpActivity;
import org.usslab.decam.Util.BlackMacLists;
import org.usslab.decam.Util.Logg;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CaptureService extends BaseService {
    public static final String TAG = "CapSer";
    public static final int CAPTURE_NOTIFICATION_ID = 66;
    public static final String CAPTURE_NOTIFICATION_TITLE = "NexmonCapture";
    public static final String CAPTURE_NOTIFICATION_CONTENT = "Status:running";
    private static MonitorModeController controller;
    private SocketTask socketTask;
    private String filename;
    //private List<Packet> packetList;
    private List<PacketInfo> packetInfoList, fastModepacketInfolist, motionConfirmPckInfoList;
    private PacketInfo packetInfo;
    private APinfo aPinfo;
    private Integer packetCount;
    //private ViewCallListener reclistview,startview;
    private CaptureBinder mBinder;
    private boolean runningFlag, needMotionConfirmFlag;

    //Algo part
    private Core algoCore;
    private Core.ResultStru algoRes;
    private ResAlgoToAPAdapter dataConverter;
    private Thread motionConfirmCollector;
    //private Runnable motionConfirmCollector_runable;


    private List<ResultAPCams> dataUsedInAdatpre;

    private long startTime, stopTime;
    private APAdapter apAdapter;
    private RecyclerView aPCamsListView;

    private CaptureListener listener = new CaptureListener() {
        @Override
        public void onStart(String currentFilename) {
            Constants.setValueFromSettings();
            packetInfoList.clear();
            fastModepacketInfolist.clear();
            motionConfirmPckInfoList.clear();
            algoRes.ap_Cams_infomation.clear();
            dataUsedInAdatpre.clear();

            packetCount = 0;
            needMotionConfirmFlag = true;
            CaptureService.this.filename = currentFilename;
            //startTime=System.currentTimeMillis();

            APinfo.clearCache();
            CAMinfo.cleanInfo();
            CAMinfo.setStarttime(System.currentTimeMillis());

            algoRes.countCameraNumbers = 0;
            apAdapter.notifyDataSetChanged();
            if (true) {
                //fast mode
                algoCore.updateList(fastModepacketInfolist);
            } else {
                //advance mode
                //Deprecated!!!
                throw new UnsupportedOperationException("advance mode not implemented");
            }


            Logg.i(TAG, "Capture Start");
            startForeground(
                    CAPTURE_NOTIFICATION_ID,
                    getNotification(CAPTURE_NOTIFICATION_TITLE, CAPTURE_NOTIFICATION_CONTENT)
            );
            //UI init;

        }

        @Override
        public void onStop() {
            //UI become static;
            stopTime = System.currentTimeMillis();
            Logg.d(TAG, "Capture Stopped");
            //stopForeground(true);

        }

        @Override
        public void onFail() {
            //make an alert;
            Logg.d(TAG, "Capture Failed,check Logging");
            //stopForeground(true);
        }

        @Override
        public void onProgress(Packet aPacket) {

            //async problem when stopFlag set,but last packet do not finish.Cause Null pointer.
            //always happend in multithreads programming.
            packetCount = aPacket.getsequenceCount();
            int length = aPacket.getFullLength();
            if (aPacket.isBeacon()) {
                try {
                    String bssid = aPacket.getBSSIDfromBeacon();
                    String ssid = aPacket.getSSIDfromBeacon();
                    APinfo.insertMacNamePair(bssid, ssid);
                } catch (Exception e) {
                    Logg.e(TAG, "Ignored:Beacon Error", e);
                }
            } else if (aPacket.isValidQosPacket()) {
                packetInfo = aPacket.buildQosPacketInfo();
                if (packetInfo == null) {
                    return;
                }
                String apacketSrcMac = packetInfo.macSrc;
                if (BlackMacLists.isBlackMac(apacketSrcMac)) {
                    //this apacket's MAC found in blacklists;
                    return;
                }


                packetInfoList.add(packetInfo);
                fastModepacketInfolist.add(packetInfo);
                if (needMotionConfirmFlag &&
                        motionConfirmCollector != null &&
                        motionConfirmCollector.isAlive())
                    motionConfirmPckInfoList.add(packetInfo);

                if (fastModepacketInfolist.size() == Core.FASTMODE_CONDUCT_PACKET_INFO_LIST_LEN) {
                    Logg.i(TAG, "Fastlist counts fill $,run algo"
                            .replace("$", Integer.toString(Core.FASTMODE_CONDUCT_PACKET_INFO_LIST_LEN))
                    );
                    algoCore.fastMode(algoRes);//call up update algorithm in this case;
                    fastModepacketInfolist.clear();
                    //put them into new threads;
                    //update UI.result
                    dataConverter.convertAlgoToAdater(algoRes);
                    apAdapter.notifyDataSetChanged();

                    if (algoRes.ap_Cams_infomation.size() > 0) {
                        //represent has 1 camera at least
                        //call up a thread collect packetInfo for N seconds, then pass to Algo of motion;
                        //finally update CAMInfo and UI;
                        if (needMotionConfirmFlag) {
                            if (motionConfirmCollector == null || !motionConfirmCollector.isAlive()) {
                                motionConfirmCollector = new Thread(new MotionConfirmCollectorRunnable(),
                                        "motionConfirmCollector");
                                motionConfirmCollector.start();
                            }
                        }
                    }

                }
            }

            //call Update RecyclerView's list;
            //Log.i("Packet",packetList.size()+"" );
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //requests STORAGE permission;

        aPinfo = new APinfo();
        packetCount = 0;
        controller = new MonitorModeController();
        packetInfoList = new ArrayList<>();
        fastModepacketInfolist = new ArrayList<>();
        motionConfirmPckInfoList = new ArrayList<>();

        algoCore = new Core();
        algoRes = new Core.ResultStru();

        dataUsedInAdatpre = new ArrayList<>();
        dataConverter = new ResAlgoToAPAdapter(dataUsedInAdatpre);
        apAdapter = new APAdapter(dataUsedInAdatpre);

        mBinder = new CaptureBinder();
        runningFlag = false;
        needMotionConfirmFlag = false;
    }

    public class CaptureBinder extends Binder {

        public void startCapture() {

            if (socketTask == null) {
                runningFlag = (!runningFlag);

                socketTask = new SocketTask(listener);
                socketTask.execute();

                controller.mstartMonitorMode();
                //view=mainview;
                //引用类型（reference type）指向一个对象，不是原始值，指向对象的变量是引用变量
            }
        }

        public void stopCapture() {
            if (socketTask != null) {
                runningFlag = (!runningFlag);
                controller.mstopMonitorMode();
                socketTask.stopSocket();
                socketTask = null;//release objects;
                stopForeground(true);

            }
        }

        public String getCurrentFileName() {
            return filename;
        }

        public void setChannel(String value) {
            controller.msetWlanChannel(value);
        }

        public String getChannel() {
            return controller.mgetWlanChannel();
        }

        public boolean getRunningFlag() {
            return runningFlag;
        }

        public List<PacketInfo> getPacketlist() {
            return CaptureService.this.packetInfoList;
        }

        public Integer getPacketCount() {
            return CaptureService.this.packetCount;
        }

        public void bindAPCamsListView(RecyclerView listx) {
            aPCamsListView = listx;
            aPCamsListView.setAdapter(apAdapter);
        }

    }

    private class MotionConfirmCollectorRunnable implements Runnable {

        @Override
        public void run() {
            motionConfirmPckInfoList.clear();
            //keep current cameras mac list;
            Set<String> currentCamsMacList = CAMinfo.getCameraList();
            CAMinfo.setConductingIsHomed(true);
            if (currentCamsMacList.size() == 0) {
                Logg.w(TAG, "currentCamsMacList.size==0");
                return;
            }
            int firstPointer = 0;
            int secondPointer = 0;
            int thirdPointer = 0;
            try {
                doVibrate();
                mkToast("Stay still for $ seconds~".replace("$", MotionConfirm.TIME_GAP[0]+""));
                Thread.sleep(MotionConfirm.TIME_GAP[0] * 1000);
                firstPointer = motionConfirmPckInfoList.size();

                doVibrate();
                Thread.sleep(1000);
                mkToast("Move around the room for $ seconds!".replace("$", MotionConfirm.TIME_GAP[1]+""));
                Thread.sleep(MotionConfirm.TIME_GAP[1] * 1000);
                secondPointer = motionConfirmPckInfoList.size();

                doVibrate();
                Thread.sleep(1000);
                mkToast("Stay still for $ seconds~".replace("$", MotionConfirm.TIME_GAP[2]+""));
                Thread.sleep(MotionConfirm.TIME_GAP[2] * 1000);
                thirdPointer = motionConfirmPckInfoList.size();
            } catch (Exception e) {
                Logg.e(TAG, "Error in MotionConfirm", e);
            }
            for (String acamsMac : currentCamsMacList) {
                boolean currentACamMacsIsHomed = MotionConfirm
                        .confirmAMacsPacketInfo(acamsMac, motionConfirmPckInfoList, firstPointer, secondPointer, thirdPointer);
                if (CAMinfo.isACamera(acamsMac))
                    CAMinfo.setIsHomed(acamsMac, currentACamMacsIsHomed);
                else
                    Logg.w(TAG, String.format(Locale.CHINA, "Unknown Camera mac:%s", acamsMac));
            }
            CAMinfo.setConductingIsHomed(false);


            //keep it will run only once Now;
            needMotionConfirmFlag = false;
        }
    }

//    @Override
//    protected void mkToast(String msg){
//        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
//    }


    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, String content) {
        Intent intent = new Intent(this, StartMainOpActivity.class);

        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_wifi_tethering_white_24dp);
        //builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);

        builder.setContentText(content);
        //builder.setProgress(100, progress, false);

        return builder.build();
    }
}
