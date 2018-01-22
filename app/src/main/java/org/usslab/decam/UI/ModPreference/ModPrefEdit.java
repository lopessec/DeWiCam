package org.usslab.decam.UI.ModPreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

import org.usslab.decam.R;
import org.usslab.decam.Util.Logg;

/**
 * Created by pip on 2017/3/17.
 */

public class ModPrefEdit extends EditTextPreference implements Preference.OnPreferenceChangeListener{
    public final String TAG="ModPrefEdit";
    public ModPrefEdit(Context context){
        super(context);
        this.setOnPreferenceChangeListener(this);
    }
    public ModPrefEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnPreferenceChangeListener(this);
    }
    public ModPrefEdit(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }



    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        String keyChanged=preference.getKey();
        Logg.i(TAG,keyChanged);
        String inpured=(String)o;
        this.setSummary(inpured);

        return true;
    }

    @Override
    public CharSequence getSummary() {
        String summary = super.getSummary().toString();
        return String.format(summary, getText());
    }


}
