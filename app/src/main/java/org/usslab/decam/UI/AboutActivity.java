package org.usslab.decam.UI;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.Gravity;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

import org.usslab.decam.Base.BaseActivity;
import org.usslab.decam.R;
import org.usslab.decam.Util.Logg;

public class AboutActivity extends BaseActivity {
    public static final String TAG="AboutActivity";
    private Toolbar toolbar;
    public static void startActivity(Context context){
        context.startActivity(
                new Intent(context,AboutActivity.class)
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_about);


        //Element adsElement = new Element();
        //adsElement.setTitle("Advertise with pip5");
        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setDescription(getString(R.string.about_page_app_description))
                .setImage(R.drawable.start_gae_logo_1)
                //.addItem(adsElement)
                .addGroup(getString(R.string.about_page_group_name))
                .addEmail(getString(R.string.about_page_email_addr))
                .addWebsite(getString(R.string.about_page_website_link))
                .addFacebook("")
                .addTwitter("")
                .addYoutube("")
                .addPlayStore(getString(R.string.about_page_app_play_store))
                .addInstagram("")
                .addGitHub(getString(R.string.about_page_github_name))
                .addGroup(getString(R.string.about_page_group_name_version))
                .addItem(new Element().setTitle(GetPackageInfomation.getPackageVersionName(this)))
                .addItem(getCopyRightsElement())
                .create();

        setContentView(aboutPage);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            //default indicator is a return mark;
        }


    }
    Element getCopyRightsElement() {
        Element copyRightsElement = new Element();
        final String copyrights = String.format(getString(R.string.copy_right), Calendar.getInstance().get(Calendar.YEAR));
        copyRightsElement.setTitle(copyrights);
        copyRightsElement.setIcon(R.drawable.about_icon_copy_right);
        copyRightsElement.setColor(ContextCompat.getColor(this, mehdi.sakout.aboutpage.R.color.about_item_icon_color));
        copyRightsElement.setGravity(Gravity.CENTER);
        copyRightsElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(AboutActivity.this, copyrights, Toast.LENGTH_SHORT).show();
                mkToast(copyrights);
            }
        });
        return copyRightsElement;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                //  WARNING:       android.R.id   =.=
                finish();
                break;
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }


    public static class GetPackageInfomation{
        public static String name;
        public static final String ERRORNAME="VersionName Error";
        public static String getPackageVersionName(Context context){

            PackageManager pm =context.getPackageManager();

            try {
                PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
                //getPackageName()是你当前类的包名，0代表是获取版本信息
                name= pi.versionName;
                //int code = pi.versionCode;

            }catch (PackageManager.NameNotFoundException e){
                Logg.e(TAG,ERRORNAME,e);
                name=ERRORNAME;
            }
            return name;

        }
    }
}
