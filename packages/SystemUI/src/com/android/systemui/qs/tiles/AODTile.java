/*
 * Copyright (C) 2018 The OmniROM Project
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

package com.android.systemui.qs.tiles;

import android.annotation.Nullable;
import android.content.ComponentName;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.view.View;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.dagger.qualifiers.Background;
import com.android.systemui.dagger.qualifiers.Main;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.qs.QSTile.State;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.qs.QSHost;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.util.settings.SecureSettings;
import com.android.systemui.util.settings.SystemSettings;

import javax.inject.Inject;

public class AODTile extends QSTileImpl<State> implements
        BatteryController.BatteryStateChangeCallback {
    private boolean mListening;
    private final Icon mIcon = ResourceIcon.get(R.drawable.ic_qs_aod);
    private final SecureSettings mSecureSettings;
    private final SystemSettings mSystemSettings;

    private final ContentObserver mObserver;

    private final BatteryController mBatteryController;

    private static final Intent LS_DISPLAY_SETTINGS = new Intent().setComponent(
        new ComponentName(
            "com.android.settings",
            "com.android.settings.Settings$LockScreenSettingsActivity"
        )
    );

    @Inject
    public AODTile(
        QSHost host,
        @Background Looper backgroundLooper,
        @Main Handler mainHandler,
        FalsingManager falsingManager,
        MetricsLogger metricsLogger,
        StatusBarStateController statusBarStateController,
        ActivityStarter activityStarter,
        QSLogger qsLogger,
        SecureSettings secureSettings,
        SystemSettings systemSettings,
        BatteryController batteryController
    ) {
        super(host, backgroundLooper, mainHandler, falsingManager, metricsLogger,
                statusBarStateController, activityStarter, qsLogger);
        mSecureSettings = secureSettings;
        mSystemSettings = systemSettings;
        mObserver = new ContentObserver(mainHandler) {
            @Override
            public void onChange(boolean selfChange) {
                refreshState();
            }
        };
        mBatteryController = batteryController;
        batteryController.observe(getLifecycle(), this);
    }

    @Override
    public void onPowerSaveChanged(boolean isPowerSave) {
        refreshState();
    }

    private int getAodState() {
        int aodState = mSecureSettings.getInt(Settings.Secure.DOZE_ALWAYS_ON, 0);
        if (aodState == 0) {
            aodState = mSystemSettings.getInt(Settings.System.DOZE_ON_CHARGE, 0) == 1 ? 2 : 0;
        }
        return aodState;
    }

    @Override
    public boolean isAvailable() {
        return mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_dozeAlwaysOnDisplayAvailable);
    }

    @Override
    public State newTileState() {
        return new State();
    }

    @Override
    public void handleClick(@Nullable View view) {
        int aodState = getAodState();
        if (aodState < 2) {
            aodState++;
        } else {
            aodState = 0;
        }
        mSecureSettings.putInt(Settings.Secure.DOZE_ALWAYS_ON, aodState == 2 ? 0 : aodState);
        mSystemSettings.putInt(Settings.System.DOZE_ON_CHARGE, aodState == 2 ? 1 : 0);
        refreshState();
    }

    @Override
    public Intent getLongClickIntent() {
        return LS_DISPLAY_SETTINGS;
    }

    @Override
    public CharSequence getTileLabel() {
        if (mBatteryController.isAodPowerSave()) {
            return mContext.getString(R.string.quick_settings_aod_off_powersave_label);
        }
        switch (getAodState()) {
            case 1:
                return mContext.getString(R.string.quick_settings_aod_label);
            case 2:
                return mContext.getString(R.string.quick_settings_aod_on_charge_label);
            default:
                return mContext.getString(R.string.quick_settings_aod_off_label);
        }
    }

    @Override
    protected void handleUpdateState(State state, Object arg) {
        state.icon = mIcon;
        state.label = getTileLabel();
        if (mBatteryController.isAodPowerSave()) {
            state.state = Tile.STATE_UNAVAILABLE;
        } else {
            state.state = getAodState() == 0 ? Tile.STATE_INACTIVE : Tile.STATE_ACTIVE;
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_TILES;
    }

    @Override
    public void handleSetListening(boolean listening) {
        if (mListening != listening) {
            mListening = listening;
            if (listening) {
                mSecureSettings.registerContentObserver(Settings.Secure.DOZE_ALWAYS_ON, mObserver);
            } else {
                mSecureSettings.unregisterContentObserver(mObserver);
            }
        }
    }
}
