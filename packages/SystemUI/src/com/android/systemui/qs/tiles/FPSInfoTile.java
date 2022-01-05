/*
 * Copyright (C) 2019 The OmniROM Project
 * Copyright (C) 2020 crDroid Android Project
 * Copyright (C) 2021 AOSP-Krypton Project
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

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.view.View;

import androidx.annotation.Nullable;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.systemui.dagger.qualifiers.Background;
import com.android.systemui.dagger.qualifiers.Main;
import com.android.systemui.FPSInfoService;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.R;

import java.io.File;

import javax.inject.Inject;

public class FPSInfoTile extends QSTileImpl<BooleanState> {

    private boolean mServiceRunning = false;
    private final boolean isAvailable;

    @Inject
    public FPSInfoTile(
            QSHost host,
            @Background Looper backgroundLooper,
            @Main Handler mainHandler,
            FalsingManager falsingManager,
            MetricsLogger metricsLogger,
            StatusBarStateController statusBarStateController,
            ActivityStarter activityStarter,
            QSLogger qsLogger
    ) {
        super(host, backgroundLooper, mainHandler, falsingManager, metricsLogger,
                statusBarStateController, activityStarter, qsLogger);
        final String fpsInfoSysNode = mContext.getResources().getString(
                R.string.config_fpsInfoSysNode);
        isAvailable = fpsInfoSysNode != null && (new File(fpsInfoSysNode).isFile());
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick(@Nullable View view) {
        toggleState();
        refreshState();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_TILES;
    }

    protected void toggleState() {
        final Intent intent = new Intent(mContext, FPSInfoService.class);
        if (mServiceRunning) {
            mContext.stopService(intent);
        } else {
            mContext.startService(intent);
        }
        mServiceRunning = !mServiceRunning;
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_fpsinfo_label);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.label = mContext.getString(R.string.quick_settings_fpsinfo_label);
        state.icon = ResourceIcon.get(R.drawable.ic_qs_fps_info);
	    if (mServiceRunning) {
            state.contentDescription =  mContext.getString(
                    R.string.accessibility_quick_settings_fpsinfo_on);
            state.state = Tile.STATE_ACTIVE;
	    } else {
            state.contentDescription =  mContext.getString(
                    R.string.accessibility_quick_settings_fpsinfo_off);
            state.state = Tile.STATE_INACTIVE;
	    }
    }

    @Override
    public boolean isAvailable() {
        return isAvailable;
    }
}
