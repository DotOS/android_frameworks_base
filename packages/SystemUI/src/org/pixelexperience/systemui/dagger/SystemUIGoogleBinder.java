/*
 * Copyright (C) 2021 The Pixel Experience Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pixelexperience.systemui.dagger;

import android.app.Activity;
import android.app.Service;

import com.android.systemui.LatencyTester;
import com.android.systemui.ScreenDecorations;
import com.android.systemui.SliceBroadcastRelayHandler;
import com.android.systemui.SystemUI;
import com.android.systemui.accessibility.SystemActions;
import com.android.systemui.accessibility.WindowMagnification;
import com.android.systemui.biometrics.AuthController;
import com.android.systemui.globalactions.GlobalActionsComponent;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.dagger.KeyguardModule;
import com.android.systemui.media.systemsounds.HomeSoundEffectController;
import com.android.systemui.power.PowerUI;
import com.android.systemui.privacy.television.TvOngoingPrivacyChip;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsModule;
import com.android.systemui.shortcut.ShortcutKeyDispatcher;
import com.android.systemui.statusbar.notification.InstantAppNotifier;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.tv.TvStatusBar;
import com.android.systemui.statusbar.tv.notifications.TvNotificationPanel;
import com.android.systemui.theme.ThemeOverlayController;
import com.android.systemui.toast.ToastUI;
import com.android.systemui.util.leak.GarbageMonitor;
import com.android.systemui.volume.VolumeUI;
import com.android.systemui.wmshell.WMShell;
import com.dot.systemui.theme.CustomThemeOverlayController;

import org.pixelexperience.systemui.GoogleServices;
import org.pixelexperience.systemui.columbus.ColumbusTargetRequestServiceWrapper;
import org.pixelexperience.systemui.gamedashboard.GameMenuActivityWrapper;
import org.pixelexperience.systemui.statusbar.dagger.StatusBarGoogleModule;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;

@Module(includes = {RecentsModule.class, StatusBarGoogleModule.class, KeyguardModule.class})
public abstract class SystemUIGoogleBinder {
    /**
     * Inject into AuthController.
     */
    @Binds
    @IntoMap
    @ClassKey(AuthController.class)
    public abstract SystemUI bindAuthController(AuthController service);

    /**
     * Inject into GarbageMonitor.Service.
     */
    @Binds
    @IntoMap
    @ClassKey(GarbageMonitor.Service.class)
    public abstract SystemUI bindGarbageMonitorService(GarbageMonitor.Service sysui);

    /**
     * Inject into GlobalActionsComponent.
     */
    @Binds
    @IntoMap
    @ClassKey(GlobalActionsComponent.class)
    public abstract SystemUI bindGlobalActionsComponent(GlobalActionsComponent sysui);

    /**
     * Inject into InstantAppNotifier.
     */
    @Binds
    @IntoMap
    @ClassKey(InstantAppNotifier.class)
    public abstract SystemUI bindInstantAppNotifier(InstantAppNotifier sysui);

    /**
     * Inject into KeyguardViewMediator.
     */
    @Binds
    @IntoMap
    @ClassKey(KeyguardViewMediator.class)
    public abstract SystemUI bindKeyguardViewMediator(KeyguardViewMediator sysui);

    /**
     * Inject into LatencyTests.
     */
    @Binds
    @IntoMap
    @ClassKey(LatencyTester.class)
    public abstract SystemUI bindLatencyTester(LatencyTester sysui);

    /**
     * Inject into PowerUI.
     */
    @Binds
    @IntoMap
    @ClassKey(PowerUI.class)
    public abstract SystemUI bindPowerUI(PowerUI sysui);

    /**
     * Inject into Recents.
     */
    @Binds
    @IntoMap
    @ClassKey(Recents.class)
    public abstract SystemUI bindRecents(Recents sysui);

    /**
     * Inject into ScreenDecorations.
     */
    @Binds
    @IntoMap
    @ClassKey(ScreenDecorations.class)
    public abstract SystemUI bindScreenDecorations(ScreenDecorations sysui);

    /**
     * Inject into ShortcutKeyDispatcher.
     */
    @Binds
    @IntoMap
    @ClassKey(ShortcutKeyDispatcher.class)
    public abstract SystemUI bindsShortcutKeyDispatcher(ShortcutKeyDispatcher sysui);

    /**
     * Inject into SliceBroadcastRelayHandler.
     */
    @Binds
    @IntoMap
    @ClassKey(SliceBroadcastRelayHandler.class)
    public abstract SystemUI bindSliceBroadcastRelayHandler(SliceBroadcastRelayHandler sysui);

    /**
     * Inject into StatusBar.
     */
    @Binds
    @IntoMap
    @ClassKey(StatusBar.class)
    public abstract SystemUI bindsStatusBar(StatusBar sysui);

    /**
     * Inject into SystemActions.
     */
    @Binds
    @IntoMap
    @ClassKey(SystemActions.class)
    public abstract SystemUI bindSystemActions(SystemActions sysui);

    /**
     * Inject into ThemeOverlayController.
     */
    @Binds
    @IntoMap
    @ClassKey(ThemeOverlayController.class)
    public abstract SystemUI bindThemeOverlayController(CustomThemeOverlayController sysui);

    /**
     * Inject into ToastUI.
     */
    @Binds
    @IntoMap
    @ClassKey(ToastUI.class)
    public abstract SystemUI bindToastUI(ToastUI service);

    /**
     * Inject into TvStatusBar.
     */
    @Binds
    @IntoMap
    @ClassKey(TvStatusBar.class)
    public abstract SystemUI bindsTvStatusBar(TvStatusBar sysui);

    /**
     * Inject into TvNotificationPanel.
     */
    @Binds
    @IntoMap
    @ClassKey(TvNotificationPanel.class)
    public abstract SystemUI bindsTvNotificationPanel(TvNotificationPanel sysui);

    /**
     * Inject into TvOngoingPrivacyChip.
     */
    @Binds
    @IntoMap
    @ClassKey(TvOngoingPrivacyChip.class)
    public abstract SystemUI bindsTvOngoingPrivacyChip(TvOngoingPrivacyChip sysui);

    /**
     * Inject into VolumeUI.
     */
    @Binds
    @IntoMap
    @ClassKey(VolumeUI.class)
    public abstract SystemUI bindVolumeUI(VolumeUI sysui);

    /**
     * Inject into WindowMagnification.
     */
    @Binds
    @IntoMap
    @ClassKey(WindowMagnification.class)
    public abstract SystemUI bindWindowMagnification(WindowMagnification sysui);

    /**
     * Inject into WMShell.
     */
    @Binds
    @IntoMap
    @ClassKey(WMShell.class)
    public abstract SystemUI bindWMShell(WMShell sysui);

    /**
     * Inject into HomeSoundEffectController.
     */
    @Binds
    @IntoMap
    @ClassKey(HomeSoundEffectController.class)
    public abstract SystemUI bindHomeSoundEffectController(HomeSoundEffectController sysui);

    /**
     * Inject into GoogleServices.
     */
    @Binds
    @IntoMap
    @ClassKey(GoogleServices.class)
    public abstract SystemUI bindGoogleServices(GoogleServices sysui);

    /**
     * Inject into GameMenuActivity.
     */
    @Binds
    @IntoMap
    @ClassKey(GameMenuActivityWrapper.class)
    public abstract Activity bindGameMenuActivity(GameMenuActivityWrapper activity);

    /**
     * Inject into GameMenuActivity.
     */
    @Binds
    @IntoMap
    @ClassKey(ColumbusTargetRequestServiceWrapper.class)
    public abstract Service bindColumbusTargetRequestService(ColumbusTargetRequestServiceWrapper activity);
}
