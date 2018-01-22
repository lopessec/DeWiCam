package org.usslab.decam.UI;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.usslab.decam.Data.Packet;
import org.usslab.decam.Data.ResultAPCams;
import org.usslab.decam.R;

import java.util.List;

/**
 * Created by pip on 2017/2/11.
 */

public class APAdapter extends RecyclerView.Adapter<APAdapter.ViewHolder> {
    private List<ResultAPCams> discoveredAPStation;//result data structure,reference to algorithm generated;
    private Context context;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView aAPSSID,aAPBSSID,aAPCamCount;
        RecyclerView aAPsCamsListView;

        public ViewHolder(View view){
            super(view);
            aAPBSSID=(TextView)view.findViewById(R.id.ap_containCams_result_item_tx_bssid_value);
            aAPSSID=(TextView)view.findViewById(R.id.ap_containCams_result_item_tx_ssid_value);
            aAPCamCount=(TextView)view.findViewById(R.id.ap_containCams_result_item_tx_camcount_value);
            aAPsCamsListView=(RecyclerView)view.findViewById(R.id.ap_containCams_result_item_camlists);

        }
    }
    public APAdapter(List<ResultAPCams> discoveredAPStation){
        this.discoveredAPStation=discoveredAPStation;
    }

    @Override
    public APAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (context==null){
            context=parent.getContext();
        }
        View view= LayoutInflater.from(context)
                .inflate(R.layout.ap_contain_cams_result_item,parent,false);
        //setClickEvent
        return new APAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(APAdapter.ViewHolder holder, int position) {
        ResultAPCams itemAP=this.discoveredAPStation.get(position);
        holder.aAPBSSID.setText(itemAP.bssid);
        holder.aAPCamCount.setText(Integer.toString(itemAP.getDetectedCams().size()));
        holder.aAPSSID.setText(itemAP.charname);
        holder.aAPsCamsListView.setLayoutManager(new LinearLayoutManager(context));
        holder.aAPsCamsListView.setAdapter(new CameraAdapter(itemAP.getDetectedCams()));
    }



    @Override
    public int getItemCount() {
        return discoveredAPStation.size();
    }


}
