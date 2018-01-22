package org.usslab.decam.UI;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.usslab.decam.Algo.Core;
import org.usslab.decam.Algo.Core.CAM_RES;
import org.usslab.decam.Data.CAMinfo;
import org.usslab.decam.R;

import java.util.List;
import java.util.Locale;

/**
 * Created by pip on 2017/2/13.
 */

public class CameraAdapter extends RecyclerView.Adapter<CameraAdapter.ViewHolder> {
    public static final String RES_HIGH_STR="High";
    public static final String RES_MID_STR="Mid";
    public static final String RES_LOW_STR="Low";
    public static final String RES_UNKNOW="Unknow";
    public static final String TIME_FORMAT="%.2fs";
    private List<Core.ResultCamInfo> resultCamInfos;

    private Context context;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView aCameraMac;
        TextView aCameraResolution,aCameraFirstTime;
        ImageView aCamreaAudio;
        ImageView aCameraIsHomed;

        public ViewHolder(View view){
            super(view);
            aCameraFirstTime= (TextView)view.findViewById(R.id.cams_contained_in_ap_lists_cost_time);
            aCameraMac=       (TextView)view.findViewById(R.id.cams_contained_in_ap_lists_macaddr);
            aCameraResolution=(TextView)view.findViewById(R.id.cams_contained_in_ap_lists_resolution);
            aCamreaAudio=    (ImageView)view.findViewById(R.id.cams_contained_in_ap_lists_audio_icon);
            aCameraIsHomed=  (ImageView)view.findViewById(R.id.cams_contained_in_ap_lists_isinhome_icon);

        }
    }
    public CameraAdapter(List<Core.ResultCamInfo> listx){
        resultCamInfos=listx;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (context==null){
            context=parent.getContext();
        }
        View view= LayoutInflater.from(context)
                .inflate(R.layout.cams_contained_in_ap_items,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
        //return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Core.ResultCamInfo aCamera =resultCamInfos.get(position);
        holder.aCameraMac.setText(aCamera.macaddr);

        //first show time;
        long showtime=CAMinfo.findACamInfo(aCamera.macaddr);
        double timeCost=(showtime-CAMinfo.getStarttime())/1000.0;
        holder.aCameraFirstTime.setText(String.format(Locale.CHINA,TIME_FORMAT,timeCost));

        switch (aCamera.resolution){
            case HIGH:holder.aCameraResolution.setText(RES_HIGH_STR);
                break;
            case MID:holder.aCameraResolution.setText(RES_MID_STR);
                break;
            case LOW:holder.aCameraResolution.setText(RES_LOW_STR);
                break;
            default:holder.aCameraResolution.setText(RES_UNKNOW);

        }

        if (!CAMinfo.getConductingIsHomed())
        if (CAMinfo.getIsHomed(aCamera.macaddr)){
            holder.aCameraIsHomed.setImageResource(R.drawable.ic_home_black_24dp);
        }
        else {
            holder.aCameraIsHomed.setImageResource(R.drawable.ic_nature_people_black_24dp);//out of home
        }

        if (aCamera.cameraHasAudio){//default icon is a no microphone
            holder.aCamreaAudio.setImageResource(R.drawable.ic_mic_black_24dp);
        }


    }



    @Override
    public int getItemCount() {
        return resultCamInfos.size();
    }
}
