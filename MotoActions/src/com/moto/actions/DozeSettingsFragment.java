/*
 * Copyright (c) 2017 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dirtyunicorns.settings.device;

import android.app.ActionBar;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

public class DozeSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private SwitchPreference mAmbientDisplayPreference;
    private SwitchPreference mHandwavePreference;
    private SwitchPreference mPickupPreference;

    private TextView mSwitchBarText;
    private Switch mAmbientDisplaySwitch;

    private String AMBIENT_DISPLAY_KEY = "ambient_display";
    private String KEY_GESTURE_HAND_WAVE = "gesture_hand_wave";
    private String KEY_GESTURE_PICK_UP = "gesture_pick_up";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionbar = getActivity().getActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setTitle(R.string.ambient_display_custom_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.doze, container, false);
        ((ViewGroup) view).addView(super.onCreateView(inflater, container, savedInstanceState));
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.doze_panel);
        boolean dozeEnabled = MotoActionsSettings.isDozeEnabled(getActivity().getContentResolver());

        mAmbientDisplayPreference = (SwitchPreference) findPreference(AMBIENT_DISPLAY_KEY);
        mAmbientDisplayPreference.setOnPreferenceChangeListener(this);
        mAmbientDisplayPreference.setChecked(dozeEnabled);

        mHandwavePreference = (SwitchPreference) findPreference(KEY_GESTURE_HAND_WAVE);
        mPickupPreference = (SwitchPreference) findPreference(KEY_GESTURE_PICK_UP);
        updatePrefs(dozeEnabled);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        final boolean enabled = (Boolean) newValue;
        if (AMBIENT_DISPLAY_KEY.equals(key)) {
            mAmbientDisplayPreference.setChecked(enabled);
            updatePrefs(enabled);
            enableDoze(enabled);
            return true;
        }
        return false;
    }

    private void updatePrefs(boolean enabled){
        mHandwavePreference.setEnabled(enabled);
        mPickupPreference.setEnabled(enabled);
    }

    private boolean enableDoze(boolean enable) {
        return Settings.Secure.putInt(getActivity().getContentResolver(),
                Settings.Secure.DOZE_ENABLED, enable ? 1 : 0);
    }
}
