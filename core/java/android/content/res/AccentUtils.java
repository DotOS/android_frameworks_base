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
            resource = getBackgroundAccentColor(defaultColor);
        else if (isResourceAccentOverlayLight(resName))
            resource = getOverlayLightAccentColor(defaultColor);
        else if (isResourceAccentOverlayDark(resName))
            resource = getOverlayDarkAccentColor(defaultColor);
        /* Experimental */
        else if (isResourceNotificationColor(resName))
            resource = getNotificationColor(defaultColor);
        // Still WIP
        else if (isResourceBackgroundColor(resName))
            resource = getBackgroundAppColor(defaultColor);
        else if (isResourceForegroundColor(resName))
           resource = getForegroundAppColor(defaultColor);
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

    public boolean isResourceAccentOverlayLight(@Nullable String resName) {
        return resName != null && resName.contains("accent_overlay_device_default_light");
    }

    public boolean isResourceAccentOverlayDark(@Nullable String resName) {
        return resName != null && resName.contains("accent_overlay_device_default_dark");
    }

    public boolean isResourceNotificationColor(@Nullable String resName) {
        return resName != null && (resName.contains("dot_notification_bg") || 
                                   resName.contains("dot_notification_bg_opaque") || 
                                   resName.contains("notification_material_background_color"));
    }

    public boolean isResourceBackgroundColor(@Nullable String resName) {
        if (resName == null) return false;
        return resName.contains("dialogBackgroundColor");
    }

    public boolean isResourceForegroundColor(@Nullable String resName) {
        if (resName == null) return false;
        return resName.contains("dialogSubBackgroundColor");
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
            Log.e(TAG, "Setting default for monetwannabe");
            return defaultColor;
        }
    }

    public int getBackgroundAppColor(int defaultColor) {
        final Context context = ActivityThread.currentApplication();
        try {
            if (MonetWannabe.isMonetEnabled(context)) {
                int colorValue = monet.getAccentColorBackgroundApp();
                return colorValue == -1 ? defaultColor : colorValue;
            } else {
                return defaultColor;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Setting default for monetwannabe");
            return defaultColor;
        }
    }

    public int getForegroundAppColor(int defaultColor) {
        final Context context = ActivityThread.currentApplication();
        try {
            if (MonetWannabe.isMonetEnabled(context)) {
                int colorValue = monet.getAccentColorForegroundApp();
                return colorValue == -1 ? defaultColor : colorValue;
            } else {
                return defaultColor;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Setting default for monetwannabe");
            return defaultColor;
        }
    }

    public int getBackgroundAccentColor(int defaultColor) {
        final Context context = ActivityThread.currentApplication();
        try {
            if (MonetWannabe.isMonetEnabled(context)) {
                int colorValue = monet.getAccentColorBackground();
                return colorValue == -1 ? defaultColor : colorValue;
            } else {
                return defaultColor;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Setting default for monetwannabe");
            return defaultColor;
        }
    }

    public int getOverlayLightAccentColor(int defaultColor) {
        final Context context = ActivityThread.currentApplication();
        try {
            if (MonetWannabe.isMonetEnabled(context)) {
                int colorValue = monet.getAccentColorOverlayLight();
                return colorValue == -1 ? defaultColor : colorValue;
            } else {
                return defaultColor;
            }
        } catch (Exception e) {
            Log.e(TAG, "Setting default for monetwannabe");
            return defaultColor;
        }
    }

    public int getOverlayDarkAccentColor(int defaultColor) {
        final Context context = ActivityThread.currentApplication();
        try {
            if (MonetWannabe.isMonetEnabled(context)) {
                int colorValue = monet.getAccentColorOverlayDark();
                return colorValue == -1 ? defaultColor : colorValue;
            } else {
                return defaultColor;
            }
        } catch (Exception e) {
            Log.e(TAG, "Setting default for monetwannabe");
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
                Log.e(TAG, "Failed to set accent: " + e.getMessage() +
                        "\nSetting default: " + defaultColor);
                return defaultColor;
            }
        } else {
            try {
                int colorValue = monet.getAccentColor();
                return colorValue == -1 ? defaultColor : colorValue;
            } catch (Exception e) {
                Log.e(TAG, "Failed to set monet accent: " + e.getMessage() + 
                        "\nSetting default: " + defaultColor);
                return defaultColor;
            }
        }
    }
}
