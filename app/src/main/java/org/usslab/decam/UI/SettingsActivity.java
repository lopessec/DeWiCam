package org.usslab.decam.UI;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.preference.Preference;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.angads25.filepicker.view.FilePickerPreference;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import org.usslab.decam.Base.AppCompatPreferenceActivity;
import org.usslab.decam.R;
import org.usslab.decam.Util.Logg;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity implements Preference.OnPreferenceClickListener{
    public static final String TAG="SettingsActivity";
    public static final String ERROR="Error";
    public static final int KILL_NEXMON_ID=78;
    public static final String kill_nexmon_shell="am force-stop de.tu_darmstadt.seemoo.nexmon";

    public static void startActivity(Context context){
        context.startActivity(
            new Intent(
                context,
                SettingsActivity.class)
        );
    }

    Preference pref_btn_clear_nexmon;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
        //use activity is much easier for simple usages than fragments;
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            //default indicator is a return mark;
        }


        pref_btn_clear_nexmon=findPreference(getString(R.string.pref_btn_clear_nexmon));
        pref_btn_clear_nexmon.setOnPreferenceClickListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();

        //setup edit/list value to his summary;
        //flashListSummary();//should be put into preference itself;

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key=preference.getKey();
        String nexmon_app_package_name=getString(R.string.nexmon_app_package_id);
        switch (key) {
            case "1":
                break;

            default:
                break;
        }

        final Command kill_cmd=new Command(KILL_NEXMON_ID,kill_nexmon_shell);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RootTools.getShell(true).add(kill_cmd);
                } catch(Exception e) {
                    Logg.e(TAG,"kill nexmon process",e);
                }
                Toast.makeText(SettingsActivity.this, "Nexmon has been force-stopped.", Toast.LENGTH_SHORT).show();
            }
        }).run();


        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                //WARNING:android.R.id   =.=
                finish();
                break;
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }

}

//https://github.com/Angads25/android-filepicker