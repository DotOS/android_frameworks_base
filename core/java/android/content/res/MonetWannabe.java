package android.content.res;

import android.annotation.ColorInt;
import android.annotation.NonNull;
import android.content.Context;
import android.graphics.Color;
import android.provider.Settings;

import com.android.internal.graphics.ColorUtils;

import java.util.HashMap;

/**
 * @hide
 */
public class MonetWannabe {

    private final Context context;

    /**
     * Color Shades
     */

    // System
    private int accentColor;
    private int accentColorSecondary;
    private int accentColorTertiary;
    private int accentColorBackground;
    private int accentColorBackgroundSecondary;

    // Keyguard
    private int accentColorKeyguard;
    private int accentColorKeyguardBackground;
    private int accentColorKeyguardBackgroundSecondary;

    // Extra
    private int accentColorQSBackground;
    private int accentColorQSOverlay;


    public static final float DEFAULT_LIGHT_ALTERATION = 0.7f;
    public static final float DEFAULT_DARK_ALTERATION = 0.8f;

    boolean isDarkMode = Resources.getSystem().getConfiguration().isNightModeActive();

    /**
     * MonetWannabe 2.0
     */
    public MonetWannabe(@NonNull Context context) {
        this.context = context;
        if (isMonetEnabled(context)) generateColors();
    }

    private void generateColors() {
        HashMap<String, Integer> monetColors = updateMonet();

        /* System */
        accentColor = isDarkMode ? monetColors.get("accentColor") :
                monetColors.get("accentColorLight");
        accentColorSecondary = isDarkMode ? monetColors.get("accentColorSecondary") :
                monetColors.get("accentColorSecondaryLight");
        accentColorTertiary = isDarkMode ? monetColors.get("accentColorTertiary") :
                monetColors.get("accentColorTertiaryLight");
        accentColorBackground = isDarkMode ? monetColors.get("backgroundColor") :
                monetColors.get("backgroundColorLight");
        accentColorBackgroundSecondary = isDarkMode ? monetColors.get("backgroundSecondaryColor") :
                monetColors.get("backgroundSecondaryColorLight");

        /* Extra */
        accentColorQSBackground = isDarkMode ? manipulateColor(accentColor, 0.6f) : manipulateColor(accentColor, 0.8f);
        accentColorQSOverlay = isDarkMode ? getDarkCousinColor(accentColor) : getLightCousinColor(accentColor);

        /* Keyguard */
        accentColorKeyguard = isDarkMode ? monetColors.get("keyguardAccentColor") :
                monetColors.get("keyguardAccentColorLight");
        accentColorKeyguardBackground = isDarkMode ? monetColors.get("keyguardBackgroundColor") :
                monetColors.get("keyguardBackgroundColorLight");
        accentColorKeyguardBackgroundSecondary = isDarkMode ? monetColors.get("keyguardBackgroundSecondaryColor") :
                monetColors.get("keyguardBackgroundSecondaryColorLight");
    }

    public int getAccentColor() {
        return accentColor;
    }

    public int getAccentColoSecondary() {
        return accentColorSecondary;
    }

    public int getAccentColorTertiary() {
        return accentColorTertiary;
    }

    public int getAccentColorQSBackground() {
        return accentColorQSBackground;
    }

    public int getAccentColorQSOverlay() {
        return accentColorQSOverlay;
    }

    public int getAccentColorBackground() {
        return accentColorBackground;
    }

    public int getAccentColorBackgroundSecondary() {
        return accentColorBackgroundSecondary;
    }

    public int getKeyguardAccentColor() {
        return accentColorKeyguard;
    }

    public int getKeyguardBackground() {
        return accentColorKeyguardBackground;
    }

    public int getKeyguardBackgroundSecondary() {
        return accentColorKeyguardBackgroundSecondary;
    }

    private HashMap<String, Integer> updateMonet() {
        HashMap<String, Integer> colorHashMap = new HashMap<>();

        colorHashMap.put("accentColor",
                getMonetColorSetting(Settings.Secure.MONET_BASE_ACCENT));
        colorHashMap.put("accentColorLight",
                getMonetColorSetting(Settings.Secure.MONET_BASE_ACCENT_LIGHT));

        /* Start of nullable accents */
        colorHashMap.put("accentColorSecondary",
                getMonetColorSetting(Settings.Secure.MONET_BASE_ACCENT_SECONDARY));
        colorHashMap.put("accentColorSecondaryLight",
                getMonetColorSetting(Settings.Secure.MONET_BASE_ACCENT_SECONDARY_LIGHT));

        colorHashMap.put("accentColorTertiary",
                getMonetColorSetting(Settings.Secure.MONET_BASE_ACCENT_TERTIARY));
        colorHashMap.put("accentColorTertiaryLight",
                getMonetColorSetting(Settings.Secure.MONET_BASE_ACCENT_TERTIARY_LIGHT));

        /*
         * Handle nullability
         * If one of the nullable accents are null, replace it with the main accent
         * Their Setting will be null every wallpaper change and used on demand
         */
        if (colorHashMap.get("accentColorSecondary") == -1 || colorHashMap.get("accentColorSecondaryLight") == -1) {
            colorHashMap.put("accentColorSecondary", colorHashMap.get("accentColor"));
            colorHashMap.put("accentColorSecondaryLight", colorHashMap.get("accentColorLight"));
        }
        if (colorHashMap.get("accentColorTertiary") == -1 || colorHashMap.get("accentColorTertiaryLight") == -1) {
            colorHashMap.put("accentColorTertiary", colorHashMap.get("accentColor"));
            colorHashMap.put("accentColorTertiaryLight", colorHashMap.get("accentColorLight"));
        }
        /* End of nullable accents */

        colorHashMap.put("backgroundColor",
                getMonetColorSetting(Settings.Secure.MONET_BACKGROUND));
        colorHashMap.put("backgroundColorLight",
                getMonetColorSetting(Settings.Secure.MONET_BACKGROUND_LIGHT));

        colorHashMap.put("backgroundSecondaryColor",
                getMonetColorSetting(Settings.Secure.MONET_BACKGROUND_SECONDARY));
        colorHashMap.put("backgroundSecondaryColorLight",
                getMonetColorSetting(Settings.Secure.MONET_BACKGROUND_SECONDARY_LIGHT));

        colorHashMap.put("keyguardAccentColor",
                getMonetColorSetting(Settings.Secure.MONET_BASE_KEYGUARD_ACCENT));
        colorHashMap.put("keyguardAccentColorLight",
                getMonetColorSetting(Settings.Secure.MONET_BASE_KEYGUARD_ACCENT_LIGHT));

        colorHashMap.put("keyguardBackgroundColor",
                getMonetColorSetting(Settings.Secure.MONET_KEYGUARD_BACKGROUND));
        colorHashMap.put("keyguardBackgroundColorLight",
                getMonetColorSetting(Settings.Secure.MONET_KEYGUARD_BACKGROUND_LIGHT));

        colorHashMap.put("keyguardBackgroundSecondaryColor",
                getMonetColorSetting(Settings.Secure.MONET_KEYGUARD_BACKGROUND_SECONDARY));
        colorHashMap.put("keyguardBackgroundSecondaryColorLight",
                getMonetColorSetting(Settings.Secure.MONET_KEYGUARD_BACKGROUND_SECONDARY_LIGHT));
        return colorHashMap;
    }

    private int getMonetColorSetting(String settingsName) {
        String color = Settings.Secure.getString(context.getContentResolver(), settingsName);
        return color == null || color.equals("-1") ? -1 : Integer.parseInt(color);
    }

    private int getLightCousinColor(int darkColor) {
        return adjustAlpha(ColorUtils.blendARGB(darkColor, Color.WHITE, DEFAULT_LIGHT_ALTERATION), 1f);
    }

    private int getDarkCousinColor(int lightColor) {
        return adjustAlpha(ColorUtils.blendARGB(lightColor, Color.BLACK, DEFAULT_DARK_ALTERATION), 1f);
    }

    /**
     * Manipulate color
     *
     * @param color  original color
     * @param factor under 1.0f to darken, over 1.0f to lighten
     * @return altered color
     */
    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a, Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }

    @ColorInt
    public static int adjustAlpha(@ColorInt int color, float factor) {
        return ColorUtils.setAlphaComponent(color, Math.round((float) Color.alpha(color) * factor));
    }

    public static int getInactiveAccent(@NonNull Context context) {
        boolean isDarkMode = Resources.getSystem().getConfiguration().isNightModeActive();
        return adjustAlpha(Resources.getSystem().getColor(android.R.color.accent_background_device_default, context.getTheme()), isDarkMode ? 0.6f : 0.3f);
    }

    public static boolean isMonetEnabled(@NonNull Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.MONET_ENGINE, 1) == 1;
    }

    public static boolean shouldForceLoad(@NonNull Context context) {
        String accent = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.MONET_BASE_ACCENT);
        boolean mainAccentFine = accent == null || accent.equals("-1");
        String accentKeyguard = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.MONET_BASE_KEYGUARD_ACCENT);
        boolean keyguardAccentFine = accentKeyguard == null || accentKeyguard.equals("-1");
        return !(mainAccentFine && keyguardAccentFine);
    }

}
