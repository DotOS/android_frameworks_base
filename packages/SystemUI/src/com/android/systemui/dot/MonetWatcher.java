package com.android.systemui.dot;

import android.annotation.NonNull;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.MonetWannabe;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;

public class MonetWatcher {

    public MonetWatcher(@NonNull Context context) {
        WallpaperManager wm = WallpaperManager.getInstance(context);
        if (MonetWannabe.isMonetEnabled(context)) {
            wm.addOnColorsChangedListener((colors, which) -> update(context),
                new Handler(context.getMainLooper()), UserHandle.USER_CURRENT);
            if (MonetWannabe.shouldForceLoad(context)) update(context);
        }
    }

    private void update(Context context) {
        Settings.Secure.putString(context.getContentResolver(), 
            Settings.Secure.MONET_BASE_ACCENT, String.valueOf(MonetWannabe.updateMonet(context)));
    }

}