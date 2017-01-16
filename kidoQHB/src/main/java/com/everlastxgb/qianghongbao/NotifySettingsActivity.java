package com.everlastxgb.qianghongbao;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.Preference;

/**
 * @author Kido
 * @email everlastxgb@gmail.com
 * @create_time 2017/1/14 17:15
 */

public class NotifySettingsActivity extends BaseSettingsActivity {
    @Override
    public Fragment getSettingsFragment() {
        return new NotifySettingsFragment();
    }

    public static class NotifySettingsFragment extends BaseSettingsFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.notify_settings);

            findPreference(Config.KEY_NOTIFY_SOUND).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    QHBApplication.eventStatistics(getActivity(), "notify_sound", String.valueOf(newValue));
                    return true;
                }
            });

            findPreference(Config.KEY_NOTIFY_VIBRATE).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    QHBApplication.eventStatistics(getActivity(), "notify_vibrate", String.valueOf(newValue));
                    return true;
                }
            });

            findPreference(Config.KEY_NOTIFY_NIGHT_ENABLE).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    QHBApplication.eventStatistics(getActivity(), "notify_night", String.valueOf(newValue));
                    return true;
                }
            });
        }
    }
}
