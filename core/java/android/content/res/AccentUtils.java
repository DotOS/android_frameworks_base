package android.content.res;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.graphics.Color;
import android.content.Context;
import android.provider.Settings;
import android.app.ActivityThread;

/** @hide */
public class AccentUtils {

    private MonetWannabe monet;
    private Context context;

    public AccentUtils() {
        context = ActivityThread.currentApplication();
        monet = new MonetWannabe(context);
    }

    private final String ACCENT_DARK_SETTING = "accent_dark";
    private final String ACCENT_LIGHT_SETTING = "accent_light";

    public int applyOverride(@Nullable String resName, int defaultColor) {
        int resource = defaultColor;
        /* System Colors */
        if (isResourceDarkAccent(resName))
            resource = getDarkAccentColor(defaultColor);
        else if (isResourceLightAccent(resName))
            resource = getLightAccentColor(defaultColor);
        else if (isResourceAccentSecondary(resName))
            resource = getAccentColorSecondary(defaultColor);
        else if (isResourceAccentTertiary(resName))
            resource = getAccentColorTertiary(defaultColor);
        else if (isResourceBackgroundColor(resName))
            resource = getBackgroundColor(defaultColor);
        else if (isResourceBackgroundSecondary(resName))
            resource = getBackgroundSecondaryColor(defaultColor);
        /* Keyguard Colors */
        else if (isResourceAccentKeyguard(resName))
            resource = getKeyguardAccentColor(defaultColor);
        else if (isResourceKeyguardBackgroundColor(resName))
            resource = getKeyguardBackgroundColor(defaultColor);
        else if (isResourceKeyguardBackgroundSecondary(resName))
            resource = getKeyguardBackgroundSecondaryColor(defaultColor);
        /* Extra Colors */
        else if (isResourceAccentBackground(resName))
            resource = getQSBackgroundAccentColor(defaultColor);
        else if (isResourceAccentOverlay(resName))
            resource = getOverlayAccentColor(defaultColor);
        return resource;
    }

    public boolean isResourceDarkAccent(@Nullable String resName) {
        return resName != null && resName.contains("accent_device_default_dark");
    }

    public boolean isResourceLightAccent(@Nullable String resName) {
        return resName != null && resName.contains("accent_device_default_light");
    }

    public boolean isResourceAccentSecondary(@Nullable String resName) {
        return resName != null && resName.contains("monet_accent_secondary_device_default");
    }

    public boolean isResourceAccentTertiary(@Nullable String resName) {
        return resName != null && resName.contains("monet_accent_tertiary_device_default");
    }

    public boolean isResourceAccentBackground(@Nullable String resName) {
        return resName != null && resName.contains("accent_background_device_default");
    }

    public boolean isResourceAccentOverlay(@Nullable String resName) {
        return resName != null && resName.contains("accent_overlay_device_default");
    }

    public boolean isResourceAccentKeyguard(@Nullable String resName) {
        return resName != null && resName.contains("monet_clock_color_device_default");
    }

    public boolean isResourceBackgroundColor(@Nullable String resName) {
        return resName != null && resName.contains("dialogBackgroundColor") ||
            resName.contains("dot_notification_bg") || 
            resName.contains("dot_notification_bg_opaque") || 
            resName.contains("notification_material_background_color") ||
            resName.contains("monet_background_device_default");
    }

    public boolean isResourceBackgroundSecondary(@Nullable String resName) {
        return resName != null && resName.contains("dialogSubBackgroundColor") ||
            resName.contains("dot_notification_dim") ||
            resName.contains("monet_contextual_color_device_default") ||
            resName.contains("monet_background_secondary_device_default");
    }

    public boolean isResourceKeyguardBackgroundColor(@Nullable String resName) {
        return resName != null && resName.contains("monet_lockscreen_background_device_default");
    }

    public boolean isResourceKeyguardBackgroundSecondary(@Nullable String resName) {
        return resName != null && resName.contains("monet_lockscreen_background_secondary_device_default");
    }

    public int getDarkAccentColor(int defaultColor) {
        return getAccentColor(monet, defaultColor, ACCENT_DARK_SETTING);
    }

    public int getLightAccentColor(int defaultColor) {
        return getAccentColor(monet, defaultColor, ACCENT_LIGHT_SETTING);
    }

    public int getAccentColorSecondary(int defaultColor) {
        return getAccentColorSecondary(monet, defaultColor, ACCENT_LIGHT_SETTING);
    }

    public int getAccentColorTertiary(int defaultColor) {
        return getAccentColorTertiary(monet, defaultColor, ACCENT_LIGHT_SETTING);
    }

    public int getKeyguardAccentColor(int defaultColor) {
        try {
            if (MonetWannabe.isMonetEnabled(context)) {
                int colorValue = monet.getKeyguardAccentColor();
                return colorValue == -1 ? defaultColor : colorValue;
            } else {
                return defaultColor;
            }
        } catch (Exception e) {
            return defaultColor;
        }
    }

    public int getKeyguardBackgroundColor(int defaultColor) {
        try {
            if (MonetWannabe.isMonetEnabled(context)) {
                int colorValue = monet.getKeyguardBackground();
                return colorValue == -1 ? defaultColor : colorValue;
            } else {
                return defaultColor;
            }
            
        } catch (Exception e) {
            return defaultColor;
        }
    }

    public int getKeyguardBackgroundSecondaryColor(int defaultColor) {
        try {
            if (MonetWannabe.isMonetEnabled(context)) {
                int colorValue = monet.getKeyguardBackgroundSecondary();
                return colorValue == -1 ? defaultColor : colorValue;
            } else {
                return defaultColor;
            }
            
        } catch (Exception e) {
            return defaultColor;
        }
    }

    public int getBackgroundColor(int defaultColor) {
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

    private int getAccentColorSecondary(@NonNull MonetWannabe monet, int defaultColor, String setting) {
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
                int colorValue = monet.getAccentColoSecondary();
                return colorValue == -1 ? defaultColor : colorValue;
            } catch (Exception e) {
                return defaultColor;
            }
        }
    }

    private int getAccentColorTertiary(@NonNull MonetWannabe monet, int defaultColor, String setting) {
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
                int colorValue = monet.getAccentColorTertiary();
                return colorValue == -1 ? defaultColor : colorValue;
            } catch (Exception e) {
                return defaultColor;
            }
        }
    }
}
