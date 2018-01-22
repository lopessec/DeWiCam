package org.usslab.decam.Background;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.content.Context;

import org.usslab.decam.Data.Packet;
import org.usslab.decam.Data.PcapFileReader;
import org.usslab.decam.Data.PcapFileWriter;
import org.usslab.decam.GlobalApplication;
import org.usslab.decam.R;
import org.usslab.decam.Util.FileOperation;
import org.usslab.decam.Util.Logg;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pip on 2017/1/30.
 */

public class SocketTask extends AsyncTask<String,Packet,Integer> {
    private final String TAG = "SocketAsyncTask";

    public static final int TYPE_STOP=2;
    public static final int TYPE_FAIL=3;

    private CaptureListener listener;
    private boolean isStopped=false;
    private boolean dumpPcapFile;

    private SharedPreferences sharedPreferencesObj;

    private String filename;
    private String directory;
    private Context context;
    private int packetCount;
    private PcapFileWriter fileWriter;
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");//设置日期格式

    public void stopSocket(){
        this.isStopped=true;
    }
    public SocketTask(CaptureListener listener){
        this.listener=listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //config filename and default storage;
        filename= FileOperation.Packet_PRE_FIX+df.format(new Date())+FileOperation.Packet_POST_FIX;

        if (context==null)
            context=GlobalApplication.getContext();
        if (sharedPreferencesObj==null)
            sharedPreferencesObj=context.getSharedPreferences(
                context.getString(R.string.pref_setting_file_name),
                Context.MODE_PRIVATE);

        String default_directory= Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS).getPath();

        dumpPcapFile=sharedPreferencesObj.getBoolean(context.getString(R.string.pref_switch_dump_packets),false);
        directory=sharedPreferencesObj.getString(
                context.getString(R.string.pref_key_str_tcpdumpfiles_folder),default_directory);
        if (directory.endsWith(":")){
            directory=directory.substring(0,directory.length()-1);
        }
        if (directory.isEmpty()){
            directory=default_directory;
        }


        packetCount=0;
        isStopped=false;

        if(dumpPcapFile){
            fileWriter=new PcapFileWriter(directory,filename);
        }

        this.listener.onStart(filename);
    }

    @Override
    protected Integer doInBackground(String... params) {
        int returnValue;
        Logg.d(TAG,"Background.ThreadInit");
        Packet aPacket;
        int length=2048;
        byte[] packet = new byte[length];
        try {

            DatagramSocket datagramSocket= GlobalApplication.getDatagramSocket();
            DatagramPacket datagramPacket = new DatagramPacket(packet, packet.length);
            int headerLen = PcapFileReader.PCAP_PACKET_HEADER_LENGTH;
            while(!isStopped) {

                datagramSocket.receive(datagramPacket);
                byte[] header = new byte[headerLen];
                byte[] data = new byte[datagramPacket.getLength() - headerLen];
                packetCount++;

                for(int i = 0; i < headerLen; i++)
                    header[i] = datagramPacket.getData()[i];

                for(int i = headerLen; i < datagramPacket.getLength(); i++)
                    data[i-headerLen] = datagramPacket.getData()[i];

                aPacket=new Packet(data,header,packetCount);

                if (dumpPcapFile){
                    fileWriter.writePacket(aPacket);
                }
                publishProgress(aPacket);
            }

            returnValue=TYPE_STOP;
        }
        catch(Exception e) {
            Logg.e(TAG,"UDP Client UnChecked Error!",e);
            returnValue = TYPE_FAIL;
        }finally {
            if(dumpPcapFile){
                fileWriter.closeFileWriter();
            }
        }
        return returnValue;
    }

    @Override
    protected void onProgressUpdate(Packet... values) {
        listener.onProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer){
            case TYPE_FAIL:
                listener.onFail();
                break;
            case TYPE_STOP:
                listener.onStop();
                break;
            default:break;
        }

    }
}
