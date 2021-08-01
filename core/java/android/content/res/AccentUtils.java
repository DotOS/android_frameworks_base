package android.content.res;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.graphics.Color;
import android.util.Log;
import android.content.Context;
import android.provider.Settings;
import android.app.ActivityThread;

/** @hide */
public class AccentUtils {

    private MonetWannabe monet;

    public AccentUtils() {
        this.monet = new MonetWannabe(ActivityThread.currentApplication());
    }

    private final String TAG = "AccentUtils";

    private final String ACCENT_DARK_SETTING = "accent_dark";
    private final String ACCENT_LIGHT_SETTING = "accent_light";

    public int applyOverride(@Nullable String resName, int defaultColor) {
        int resource = defaultColor;
        if (isResourceDarkAccent(resName))
            resource = getDarkAccentColor(defaultColor);
        else if (isResourceLightAccent(resName))
            resource = getLightAccentColor(defaultColor);
        else if (isResourceAccentBackground(resName))
            resource = getQSBackgroundAccentColor(defaultColor);
        else if (isResourceAccentOverlay(resName))
            resource = getOverlayAccentColor(defaultColor);
        else if (isResourceNotificationColor(resName))
            resource = getNotificationColor(defaultColor);
        else if (isResourceBackgroundColor(resName))
            resource = getBackgroundColor(defaultColor);
        else if (isResourceForegroundColor(resName))
           resource = getBackgroundSecondaryColor(defaultColor);
        return resource;
    }

    public boolean isResourceDarkAccent(@Nullable String resName) {
        return resName != null && resName.contains("accent_device_default_dark");
    }

    public boolean isResourceLightAccent(@Nullable String resName) {
        return resName != null && resName.contains("accent_device_default_light");
    }

    public boolean isResourceAccentBackground(@Nullable String resName) {
        return resName != null && resName.contains("accent_background_device_default");
    }

    public boolean isResourceAccentOverlay(@Nullable String resName) {
        return resName != null && resName.contains("accent_overlay_device_default");
    }

    public boolean isResourceNotificationColor(@Nullable String resName) {
        return resName != null && (resName.contains("dot_notification_bg") || 
                                   resName.contains("dot_notification_bg_opaque") || 
                                   resName.contains("notification_material_background_color"));
    }

    public boolean isResourceBackgroundColor(@Nullable String resName) {
        if (resName == null) return false;
        return resName.contains("dialogBackgroundColor") ||
                resName.contains("monet_background_device_default") ||
                resName.contains("primary_device_default_settings_light") ||
                resName.contains("primary_device_default_settings") ||
                resName.contains("primary_dark_device_default_dark") ||
                resName.contains("primary_dark_device_default_settings") ||
                resName.contains("primary_dark_device_default_settings_light") ||
                resName.contains("primary_dark_device_default_light");
    }

    public boolean isResourceForegroundColor(@Nullable String resName) {
        if (resName == null) return false;
        return resName.contains("dialogSubBackgroundColor") ||
                resName.contains("monet_contextual_color_device_default") ||
                resName.contains("monet_background_secondary_device_default");
    }

    public int getDarkAccentColor(int defaultColor) {
        return getAccentColor(monet, defaultColor, ACCENT_DARK_SETTING);
    }

    public int getLightAccentColor(int defaultColor) {
        return getAccentColor(monet, defaultColor, ACCENT_LIGHT_SETTING);
    }
    
    public int getNotificationColor(int defaultColor) {
        final Context context = ActivityThread.currentApplication();
        try {
            if (MonetWannabe.isMonetEnabled(context)) {
                int colorValue = monet.getAccentColorNotification();
                return colorValue == -1 ? defaultColor : colorValue;
            } else {
                return defaultColor;
            }
            
        } catch (Exception e) {
            return defaultColor;
        }
    }

    public int getBackgroundColor(int defaultColor) {
        final Context context = ActivityThread.currentApplication();
        try {
            if (MonetWannabe.isMonetEnabled(context)) {
                int colorValue = monet.getAccentColorBackground();
                return colorValue == -1 ? defaultColor : colorValue;
            } else {
                return defaultColor;
            }
            
        } catch (Exception e) {
            return defaultColor;
        }
    }

    public int getBackgroundSecondaryColor(int defaultColor) {
        final Context context = ActivityThread.currentApplication();
        try {
            if (MonetWannabe.isMonetEnabled(context)) {
                int colorValue = monet.getAccentColorBackgroundSecondary();
                return colorValue == -1 ? defaultColor : colorValue;
            } else {
                return defaultColor;
            }
            
        } catch (Exception e) {
            return defaultColor;
        }
    }

    public int getQSBackgroundAccentColor(int defaultColor) {
        final Context context = ActivityThread.currentApplication();
        try {
            if (MonetWannabe.isMonetEnabled(context)) {
                int colorValue = monet.getAccentColorQSBackground();
                return colorValue == -1 ? defaultColor : colorValue;
            } else {
                return defaultColor;
            }
            
        } catch (Exception e) {
            return defaultColor;
        }
    }

    public int getOverlayAccentColor(int defaultColor) {
        final Context context = ActivityThread.currentApplication();
        try {
            if (MonetWannabe.isMonetEnabled(context)) {
                int colorValue = monet.getAccentColorQSOverlay();
                return colorValue == -1 ? defaultColor : colorValue;
            } else {
                return defaultColor;
            }
        } catch (Exception e) {
            return defaultColor;
        }
    }

    private int getAccentColor(@NonNull MonetWannabe monet, int defaultColor, String setting) {
        final Context context = ActivityThread.currentApplication();
        if (!MonetWannabe.isMonetEnabled(context)) {
            try {
                String colorValue = Settings.Secure.getString(context.getContentResolver(), setting);
                return (colorValue == null || "-1".equals(colorValue)) ?
                    defaultColor : Color.parseColor("#" + colorValue);
            } catch (Exception e) {
                return defaultColor;
            }
        } else {
            try {
                int colorValue = monet.getAccentColor();
                return colorValue == -1 ? defaultColor : colorValue;
            } catch (Exception e) {
                return defaultColor;
            }
        }
    }
}
