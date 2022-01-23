/*
 * Copyright (C) 2015 The CyanogenMod Project
 * Copyright (C) 2022 Benzo Rom
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
package com.android.systemui.qs.tiles

import android.content.ContentResolver
import android.content.Intent
import android.content.SyncStatusObserver
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.view.View
import com.android.internal.logging.MetricsLogger
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.systemui.R
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.dagger.qualifiers.Main
import com.android.systemui.plugins.ActivityStarter
import com.android.systemui.plugins.FalsingManager
import com.android.systemui.plugins.qs.QSTile.BooleanState
import com.android.systemui.plugins.statusbar.StatusBarStateController
import com.android.systemui.qs.QSHost
import com.android.systemui.qs.logging.QSLogger
import com.android.systemui.qs.tileimpl.QSTileImpl
import javax.inject.Inject

/** Quick settings tile: Sync  */
class SyncTile @Inject constructor(
    host: QSHost,
    @Background backgroundLooper: Looper,
    @Main mainHandler: Handler,
    falsingManager: FalsingManager,
    metricsLogger: MetricsLogger,
    statusBarStateController: StatusBarStateController,
    activityStarter: ActivityStarter,
    qsLogger: QSLogger
) : QSTileImpl<BooleanState>(
    host, backgroundLooper, mainHandler, falsingManager, metricsLogger,
    statusBarStateController, activityStarter, qsLogger
) {
    private val icon = ResourceIcon.get(R.drawable.ic_qs_sync)
    private var listening = false
    private var syncObserverHandle: Any? = null
    private val syncObserver = SyncStatusObserver {
        mHandler.post(Runnable { refreshState() })
    }

    override fun newTileState(): BooleanState {
        return BooleanState()
    }

    override fun handleClick(view: View?) {
        ContentResolver.setMasterSyncAutomatically(!state.value)
        refreshState()
    }

    override fun getLongClickIntent(): Intent? {
        val intent = Intent("android.settings.SYNC_SETTINGS")
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        return intent
    }

    override fun handleSetListening(listening: Boolean) {
        super.handleSetListening(listening)
        if (this.listening == listening) return
        this.listening = listening
        syncObserverHandle = if (listening) {
            ContentResolver.addStatusChangeListener(
                ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS,
                syncObserver
            )
        } else {
            ContentResolver.removeStatusChangeListener(
                syncObserverHandle
            )
            null
        }
    }

    override fun getTileLabel(): CharSequence {
        return mContext.getText(
            R.string.quick_settings_sync_label
        )
    }

    override fun handleUpdateState(state: BooleanState, arg: Any?) {
        state.value = ContentResolver.getMasterSyncAutomatically()
        state.label = tileLabel
        state.icon = icon
        if (state.value) {
            state.contentDescription = mContext.getString(
                R.string.accessibility_quick_settings_sync_on
            )
            state.state = Tile.STATE_ACTIVE
        } else {
            state.contentDescription = mContext.getString(
                R.string.accessibility_quick_settings_sync_off
            )
            state.state = Tile.STATE_INACTIVE
        }
    }

    override fun composeChangeAnnouncement(): String {
        return if (state.value) {
            mContext.getString(
                R.string.accessibility_quick_settings_sync_changed_on
            )
        } else {
            mContext.getString(
                R.string.accessibility_quick_settings_sync_changed_off
            )
        }
    }

    override fun getMetricsCategory(): Int {
        return MetricsEvent.CUSTOM_TILES
    }
}
