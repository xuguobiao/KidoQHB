package com.everlastxgb.qianghongbao;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * @author Kido
 * @email everlastxgb@gmail.com
 * @create_time 2017/1/14 17:15
 */

public class BaseSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Config.PREFERENCE_NAME);
    }
}
