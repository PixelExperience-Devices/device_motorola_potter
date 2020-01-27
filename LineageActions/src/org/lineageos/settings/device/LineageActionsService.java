/*
 * Copyright (c) 2015 The CyanogenMod Project
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

package org.lineageos.settings.device;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import java.util.List;
import java.util.LinkedList;

import org.lineageos.settings.device.actions.UpdatedStateNotifier;
import org.lineageos.settings.device.actions.CameraActivationSensor;
import org.lineageos.settings.device.actions.ChopChopSensor;
import org.lineageos.settings.device.actions.FlipToMute;
import org.lineageos.settings.device.actions.LiftToSilence;
import org.lineageos.settings.device.actions.ProximitySilencer;

public class LineageActionsService extends IntentService implements UpdatedStateNotifier {
    private static final String TAG = "LineageActions";

    private final PowerManager mPowerManager;
    private final PowerManager.WakeLock mWakeLock;

    private final List<UpdatedStateNotifier> mUpdatedStateNotifiers = new LinkedList<>();

    public LineageActionsService(Context context) {
        super("MotoActionService");

        Log.d(TAG, "Starting");

        LineageActionsSettings lineageActionsSettings = new LineageActionsSettings(context, this);
        SensorHelper sensorHelper = new SensorHelper(context);

        // Other actions that are always enabled
        mUpdatedStateNotifiers.add(new CameraActivationSensor(lineageActionsSettings, sensorHelper));
        mUpdatedStateNotifiers.add(new ChopChopSensor(lineageActionsSettings, sensorHelper));
        mUpdatedStateNotifiers.add(new ProximitySilencer(lineageActionsSettings, context, sensorHelper));
        mUpdatedStateNotifiers.add(new FlipToMute(lineageActionsSettings, context, sensorHelper));
        mUpdatedStateNotifiers.add(new LiftToSilence(lineageActionsSettings, context, sensorHelper));

        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        String tag = context.getPackageName() + ":ServiceWakeLock";
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag);
        updateState();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    public void updateState() {
        for (UpdatedStateNotifier notifier : mUpdatedStateNotifiers) {
            notifier.updateState();
        }
    }
}
