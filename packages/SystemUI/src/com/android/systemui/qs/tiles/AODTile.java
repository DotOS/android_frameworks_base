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
import android.net.Uri;
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
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.qs.QSHost;
import com.android.systemui.R;
import com.android.systemui.util.settings.SecureSettings;

import javax.inject.Inject;

public class AODTile extends QSTileImpl<BooleanState> {
    private boolean mAodDisabled;
    private boolean mListening;
    private final Icon mIcon = ResourceIcon.get(R.drawable.ic_qs_aod);
    private final SecureSettings mSecureSettings;

    private final ContentObserver mObserver;

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
        SecureSettings secureSettings
    ) {
        super(host, backgroundLooper, mainHandler, falsingManager, metricsLogger,
                statusBarStateController, activityStarter, qsLogger);
        mSecureSettings = secureSettings;
        mObserver = new ContentObserver(mainHandler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                refreshState();
            }
        };
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void handleClick(@Nullable View view) {
        mAodDisabled = !mAodDisabled;
        mSecureSettings.putInt(Settings.Secure.DOZE_ALWAYS_ON,
                mAodDisabled ? 0 : 1, UserHandle.USER_CURRENT);
        refreshState();
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_aod_label);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        if (state.slash == null) {
            state.slash = new SlashState();
        }
        mAodDisabled = mSecureSettings.getInt(Settings.Secure.DOZE_ALWAYS_ON, 1) == 0;
        state.icon = mIcon;
        state.value = mAodDisabled;
        state.slash.isSlashed = state.value;
        state.label = mContext.getString(R.string.quick_settings_aod_label);
        if (mAodDisabled) {
            state.state = Tile.STATE_INACTIVE;
        } else {
            state.state = Tile.STATE_ACTIVE;
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
