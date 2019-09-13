package com.google.android.systemui;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.NotificationLockscreenUserManagerImpl;
import com.google.android.systemui.smartspace.SmartSpaceController;
import android.app.admin.DevicePolicyManager;
import android.os.UserManager;
import com.android.systemui.statusbar.NotificationClickNotifier;
import android.app.KeyguardManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import android.os.Handler;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.KeyguardStateController;

import javax.inject.Inject;
import javax.inject.Singleton;
import com.android.systemui.dagger.qualifiers.Main;

@Singleton
public class NotificationLockscreenUserManagerGoogle extends NotificationLockscreenUserManagerImpl {
    @Inject
    public NotificationLockscreenUserManagerGoogle(Context context,
            BroadcastDispatcher broadcastDispatcher,
            DevicePolicyManager devicePolicyManager,
            UserManager userManager,
            NotificationClickNotifier clickNotifier,
            KeyguardManager keyguardManager,
            StatusBarStateController statusBarStateController,
            @Main Handler mainHandler,
            DeviceProvisionedController deviceProvisionedController,
            KeyguardStateController keyguardStateController) {
        super(context,broadcastDispatcher,devicePolicyManager,userManager,clickNotifier,keyguardManager,statusBarStateController,mainHandler,deviceProvisionedController,keyguardStateController);
    }

    @Override
    public void updateLockscreenNotificationSetting() {
        super.updateLockscreenNotificationSetting();
        updateAodVisibilitySettings();
    }

    public void updateAodVisibilitySettings() {
        SmartSpaceController.get(this.mContext).setHideSensitiveData(!userAllowsPrivateNotificationsInPublic(this.mCurrentUserId));
    }
}
