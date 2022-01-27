/*
 * Copyright (C) 2019 The OmniROM Project
 * Copyright (C) 2020 crDroid Android Project
 * Copyright (C) 2021-2022 AOSP-Krypton Project
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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.service.quicksettings.Tile
import android.util.Log
import android.view.View

import androidx.annotation.Nullable

import com.android.internal.logging.MetricsLogger
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.dagger.qualifiers.Main
import com.android.systemui.FPSInfoService
import com.android.systemui.plugins.ActivityStarter
import com.android.systemui.plugins.FalsingManager
import com.android.systemui.plugins.qs.QSTile.BooleanState
import com.android.systemui.plugins.statusbar.StatusBarStateController
import com.android.systemui.qs.logging.QSLogger
import com.android.systemui.qs.QSHost
import com.android.systemui.qs.tileimpl.QSTileImpl
import com.android.systemui.R

import java.io.File

import javax.inject.Inject

class FPSInfoTile @Inject constructor(
    host: QSHost,
    @Background backgroundLooper: Looper,
    @Main mainHandler: Handler,
    falsingManager: FalsingManager,
    metricsLogger: MetricsLogger,
    statusBarStateController: StatusBarStateController,
    activityStarter: ActivityStarter,
    qsLogger: QSLogger,
): QSTileImpl<BooleanState>(
    host,
    backgroundLooper,
    mainHandler,
    falsingManager,
    metricsLogger,
    statusBarStateController,
    activityStarter,
    qsLogger,
) {

    private val available: Boolean

    private var serviceBound = false
    private var fpsInfoService: FPSInfoService? = null

    private val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder?) {
            logD("onServiceConnected")
            fpsInfoService = (service as? FPSInfoService.ServiceBinder)?.service
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            logD("onServiceDisconnected")
            fpsInfoService = null
        }
    }

    init {
        val fpsInfoSysNode = mContext.resources.getString(R.string.config_fpsInfoSysNode)
        available = fpsInfoSysNode != null && (File(fpsInfoSysNode).isFile)
        logD("fpsInfoSysNode = $fpsInfoSysNode, available = $available")
    }

    override fun isAvailable(): Boolean = available

    override fun newTileState() = BooleanState()

    override protected fun handleInitialize() {
        logD("handleInitialize")
        if (!serviceBound) {
            val intent = Intent(mContext, FPSInfoService::class.java)
            serviceBound = mContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            logD("serviceBound = $serviceBound")
        }
    }

    override protected fun handleDestroy() {
        logD("handleDestroy")
        if (serviceBound) {
            logD("unbinding")
            mContext.unbindService(serviceConnection)
            fpsInfoService = null
            serviceBound = false
        }
        super.handleDestroy()
    }

    override protected fun handleClick(view: View?) {
        toggleState()
        refreshState()
    }

    override fun getMetricsCategory(): Int = MetricsEvent.CUSTOM_TILES

    private fun toggleState() {
        logD("toggleState")
        fpsInfoService?.let {
            if (it.isReading) {
                logD("stopReading")
                it.stopReading()
            } else {
                logD("startReading")
                it.startReading()
            }
        }
    }

    override fun getLongClickIntent(): Intent? = null

    override fun getTileLabel(): CharSequence =
        mContext.getString(R.string.quick_settings_fpsinfo_label)

    override protected fun handleUpdateState(state: BooleanState, arg: Any?) {
        state.label = mContext.getString(R.string.quick_settings_fpsinfo_label)
        state.icon = ResourceIcon.get(R.drawable.ic_qs_fps_info)
        logD("handleUpdateState, isReading = ${fpsInfoService?.isReading}")
	    if (fpsInfoService?.isReading == true) {
            state.contentDescription =  mContext.getString(
                    R.string.accessibility_quick_settings_fpsinfo_on)
            state.state = Tile.STATE_ACTIVE
	    } else {
            state.contentDescription =  mContext.getString(
                    R.string.accessibility_quick_settings_fpsinfo_off)
            state.state = Tile.STATE_INACTIVE
	    }
    }

    companion object {
        private const val TAG = "FPSInfoTile"
        private const val DEBUG = false

        private fun logD(msg: String) {
            if (DEBUG) Log.d(TAG, msg)
        }
    }
}
