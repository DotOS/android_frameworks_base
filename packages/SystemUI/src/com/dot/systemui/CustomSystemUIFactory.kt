/*
 * Copyright (C) 2021 The Proton AOSP Project
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

package com.dot.systemui

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import com.android.systemui.SystemUIFactory
import com.android.systemui.dagger.GlobalRootComponent
import com.android.systemui.screenshot.ScreenshotNotificationSmartActionsProvider
import com.android.systemui.theme.ThemeOverlayController
import com.dot.systemui.dagger.DaggerSysUIGoogleGlobalRootComponent
import com.dot.systemui.dagger.SysUIGoogleSysUIComponent
import com.dot.systemui.theme.CustomThemeOverlayController
import com.google.android.systemui.screenshot.ScreenshotNotificationSmartActionsProviderGoogle
import java.util.concurrent.ExecutionException

class CustomSystemUIFactory : SystemUIFactory() {

    @Throws(ExecutionException::class, InterruptedException::class)
    override fun init(context: Context?, fromTest: Boolean) {
        super.init(context, fromTest)
        if (shouldInitializeComponents()) {
            (getSysUIComponent() as SysUIGoogleSysUIComponent).createKeyguardSmartspaceController()
        }
    }

    override fun createScreenshotNotificationSmartActionsProvider(
        context: Context?,
        executor: Executor?,
        handler: Handler?
    ): ScreenshotNotificationSmartActionsProvider {
        return ScreenshotNotificationSmartActionsProviderGoogle(context, executor, handler)
    }

    // ML back gesture provider
    override fun createBackGestureTfClassifierProvider(am: AssetManager, modelName: String) =
        CustomBackGestureTfClassifierProvider(am, modelName)

    // Override services without having to copy the entire array
    override fun getSystemUIServiceComponents(resources: Resources): Array<String> {
        val services = super.getSystemUIServiceComponents(resources)
        return services.map { CUSTOM_SERVICES[it] ?: it }.toTypedArray()
    }

    override fun buildGlobalRootComponent(context: Context?): GlobalRootComponent {
        return DaggerSysUIGoogleGlobalRootComponent.builder()
            .context(context)
            .build()
    }

    companion object {
        private val CUSTOM_SERVICES = mapOf(
            ThemeOverlayController::class to CustomThemeOverlayController::class,
        ).map { it.key.java.name to it.value.java.name }.toMap()
    }
}
