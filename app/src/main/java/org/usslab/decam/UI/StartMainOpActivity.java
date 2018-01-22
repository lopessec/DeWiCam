package org.usslab.decam.UI;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.usslab.decam.Background.CaptureService;
import org.usslab.decam.Base.ActivityCollector;
import org.usslab.decam.Base.BaseActivity;
import org.usslab.decam.GlobalApplication;
import org.usslab.decam.R;
import org.usslab.decam.UI.ModPreference.SpeedChart;
import org.usslab.decam.Util.Logg;

import lecho.lib.hellocharts.view.LineChartView;

public class StartMainOpActivity extends BaseActivity {
    public static final String TAG = "StartMainOpActivity";
    private static final String SPEEDTAIL = "per/s";
    private static final String COUNTTAIL = "ps";
    private static final long GAP_TIME_TORUN = 1500;//1000ms=1s
    private static final long GAP_TIME_TOSTOP = 500;//1000ms=1s
    private static final int UPDATE_SPEED_DELAY = 250;//0.5s;

    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView speedCount, cumulativeCount;
    private FloatingActionButton fabUp;
    private Intent intentCaptureService;
    private RecyclerView aPCamsListView;
    private SpeedChart lineChartView;




    private CaptureService.CaptureBinder captureBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            captureBinder = (CaptureService.CaptureBinder) service;
            captureBinder.bindAPCamsListView(aPCamsListView);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, StartMainOpActivity.class));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //run on SingleTask mode;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_main_op);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        lineChartView = (SpeedChart) findViewById(R.id.line_chart_view);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        checkPermisionAndSocket();

        navigationView.setNavigationItemSelectedListener(new NavigationItemSelectEvents());
        fabUp = (FloatingActionButton) findViewById(R.id.fab_start_page);
        fabUp.setOnClickListener(new FabClickListener());

        speedCount = (TextView) findViewById(R.id.textview_capture_speed_int);
        cumulativeCount = (TextView) findViewById(R.id.textview_cumulative_count_int);

        aPCamsListView = (RecyclerView) findViewById(R.id.ap_containCams_result_recyclerview);
        aPCamsListView.setLayoutManager(new LinearLayoutManager(this));//make a normal listview;

        intentCaptureService = new Intent(StartMainOpActivity.this, CaptureService.class);
        bindService(intentCaptureService, connection, BIND_AUTO_CREATE);


    }

    private class FabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            checkPermisionAndSocket();
            //-----------------------------------------------------------------------
            //-----------------------------------------------------------------------
            if (!captureBinder.getRunningFlag()) {
                //stop->run;show stopbuttom;
                fabUp.setImageResource(R.drawable.ic_stop_white_24dp);

                captureBinder.startCapture();
                //mkToast("Start Capture~");

                //speed Mark:
                new Thread(new UpdateCaptureSpeed(), "CaptureCountT").start();

            } else {
                //run->stop;show runbuttom;
                fabUp.setImageResource(R.drawable.ic_wifi_tethering_white_24dp);
                captureBinder.stopCapture();
            }
            new Thread(new TemeraroyDisableFabAndEnable(), "FabEnDis").start();

        }
    }

    private class UpdateCaptureSpeed implements Runnable {

        int speed, currentCount, lastCount;


        @Override
        public void run() {
            Logg.d(TAG, "Start update speed");
            lineChartView.reset();
            for (lastCount = 0, currentCount = 0;
                 captureBinder.getRunningFlag();
                 lastCount = currentCount) {

                currentCount = captureBinder.getPacketCount();
                speed = (currentCount - lastCount) * 1000 / UPDATE_SPEED_DELAY;


                if (speedCount.isShown() && cumulativeCount.isShown())
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            speedCount.setText(String.valueOf(speed).concat(SPEEDTAIL));
                            cumulativeCount.setText(String.valueOf(currentCount).concat(COUNTTAIL));
                            lineChartView.addPointDelta(speed, UPDATE_SPEED_DELAY / 1000);
                            lineChartView.updateView();
                        }
                    });


                try {
                    Thread.sleep(UPDATE_SPEED_DELAY);
                } catch (Exception e) {
                    Logg.e(TAG, "error in update speed", e);
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speedCount.setText(getString(R.string.ui_text_norun));
                }
            });
        }
    }

    private class TemeraroyDisableFabAndEnable implements Runnable {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fabUp.setEnabled(false);
                    fabUp.setImageAlpha(140);
                }
            });
            try {
                if (captureBinder.getRunningFlag()) {
                    Thread.sleep(GAP_TIME_TORUN);
                } else {
                    Thread.sleep(GAP_TIME_TOSTOP);
                }

            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fabUp.setImageAlpha(255);
                    fabUp.setEnabled(true);
                }
            });

        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (captureBinder.getRunningFlag()) {

            mkToast("Please Stop capture before exit~", true);
        } else {
            super.onBackPressed();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_start);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_start_page, menu);
        return true;
    }


    private final String alertTitleHeader = "CurrentChannel:";
    private StringBuilder channelSelectTitle = new StringBuilder(alertTitleHeader);

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_start_page_filter_item:
                //call up channel sets;
                new AlertDialog.Builder(this)
                        .setTitle(channelSelectTitle.delete(alertTitleHeader.length(),
                                channelSelectTitle.length()).append(captureBinder.getChannel())
                        )
                        .setItems(R.array.entries_list_channel_text, new DialogueSelectChannelFace())
                        .setNegativeButton("Cancel", null)
                        //.setPositiveButton("Confirm", null)
                        .show();

                break;
            case R.id.toolbar_start_page_setting_item:
                //call up major setting page
                SettingsActivity.startActivity(StartMainOpActivity.this);
                break;

            case android.R.id.home:
                //  WARNING:       android.R.id   =.=
                //mkToast("home");
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class DialogueSelectChannelFace implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // The 'which' argument contains the index position
            // of the selected item
            String[] channels = getResources().getStringArray(R.array.entries_list_channel_value);
            String selectedChannel = channels[which];
            mkToast("Target Channel: " + selectedChannel);
            captureBinder.setChannel(selectedChannel);
        }
    }

    class NavigationItemSelectEvents implements NavigationView.OnNavigationItemSelectedListener {
        private DrawerLayout mDrawerLayout;

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int id = item.getItemId();
            switch (id) {
                case R.id.nav_start:
                    StartMainOpActivity.startActivity(StartMainOpActivity.this);
                    break;
                case R.id.nav_analysis:
                    //it will never shown in the lists;
                    break;
                case R.id.nav_settings:


                    SettingsActivity.startActivity(StartMainOpActivity.this);

                    break;
                case R.id.nav_about:
                    AboutActivity.startActivity(StartMainOpActivity.this);
                    break;
                case R.id.nav_exit:
                    //TODO:if capture is running, jump out an AlertDiog;
                    captureBinder.stopCapture();
                    ActivityCollector.finishAll();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(10);
                    break;
                default:
                    break;
            }
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerLayout.closeDrawers();
            return true;
        }
    }

    private void checkPermisionAndSocket(){
        if (ContextCompat.checkSelfPermission(StartMainOpActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(StartMainOpActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            mkToast("After permission granted,Please Click again");
            return;
        }
        if (GlobalApplication.getDatagramSocket() == null) {
            mkToast("Port:5555 bind false!\ntry rebinding~");
            GlobalApplication.bindPort(5555);
            //return;
        }
    }

}
