package android.content.res;

import android.annotation.ColorInt;
import android.annotation.NonNull;
import android.annotation.SuppressLint;
import android.annotation.Size;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;

import com.android.internal.graphics.ColorUtils;
import com.android.internal.graphics.palette.Palette;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

import java.util.HashMap;

/** @hide */
public class MonetWannabe {

    private final Context context;

    /**
     * Color Shades
     */
    private int accentColor;
    private int accentColorNotification;
    private int accentColorBackground;
    private int accentColorBackgroundSecondary;

    private int accentColorQSBackground /* Reserved for QSPanel */;
    private int accentColorQSOverlay /* Reserved for QSPanel */;


    public static final float DEFAULT_LIGHT_ALTERATION = 0.7f;
    public static final float DEFAULT_DARK_ALTERATION = 0.8f;

    public static final float DEFAULT_LIGHT_NOTIFICATION_BLEND = 0.80f;
    public static final float DEFAULT_DARK_NOTIFICATION_BLEND = 0.85f;

    public static final float DEFAULT_LIGHT_BACKGROUND_BLEND = 0.75f;
    public static final float DEFAULT_DARK_BACKGROUND_BLEND = 0.75f;

    private static final float MIN_LIGHTNESS = 0.35f;
    private static final float MAX_LIGHTNESS = 0.85f;

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
        accentColor = isDarkMode ? monetColors.get("accentColor") : 
                                   monetColors.get("accentColorLight");
        accentColorQSBackground = isDarkMode ? manipulateColor(accentColor, 0.6f) : 
                                               manipulateColor(accentColor, 0.8f);
        accentColorQSOverlay = isDarkMode ? getDarkCousinColor(accentColor) :
                                            getLightCousinColor(accentColor);

        /* Background & Foreground App Color */
        if (monetColors.get("backgroundSecondaryColor") == null && monetColors.get("backgroundColor") == null) {
            int backgroundBase = isDarkMode ?
                    blendColor(Utils.lighten(accentColor, 20f), BLACK, DEFAULT_DARK_BACKGROUND_BLEND) :
                    blendColor(accentColor, WHITE, DEFAULT_LIGHT_BACKGROUND_BLEND);
            accentColorBackground = isDarkMode ? backgroundBase : 
                                                 Utils.lighten(backgroundBase, 10f);
            accentColorBackgroundSecondary = isDarkMode ? Utils.lighten(backgroundBase, 10f) : 
                                                          backgroundBase;
        } else {
            accentColorBackground = isDarkMode ? monetColors.get("backgroundColor") : 
                                                 monetColors.get("backgroundColorLight");
            accentColorBackgroundSecondary = isDarkMode ? monetColors.get("backgroundSecondaryColor") : 
                                                          monetColors.get("backgroundSecondaryColorLight");
        }

        if (monetColors.get("backgroundSecondaryColor") == null) {
            accentColorNotification = isDarkMode ? blendColor(accentColor, BLACK, DEFAULT_DARK_NOTIFICATION_BLEND) : 
                                                   blendColor(accentColor, WHITE, DEFAULT_LIGHT_NOTIFICATION_BLEND);
        } else {
            accentColorNotification = accentColorBackground;
        }
    }

    public int getAccentColor() {
        return accentColor;
    }

    public int getAccentColorQSBackground() {
        return accentColorQSBackground;
    }

    public int getAccentColorQSOverlay() {
        return accentColorQSOverlay;
    }

    public int getAccentColorNotification() {
        return accentColorNotification;
    }

    public int getAccentColorBackground() {
        return accentColorBackground;
    }

    public int getAccentColorBackgroundSecondary() {
        return accentColorBackgroundSecondary;
    }

    private int blendColor(int blendColor, int blend, float blendvalue) {
        return ColorUtils.blendARGB(blendColor, blend, blendvalue);
    }

    /**
     * Manipulate color
     * @param color original color
     * @param factor under 1.0f to darken, over 1.0f to lighten
     * @return altered color
     */
    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a, Math.min(r,255), Math.min(g,255), Math.min(b,255));
    }


    private HashMap<String, Integer> updateMonet() {
        HashMap<String, Integer> colorHashMap = new HashMap<>();

        colorHashMap.put("accentColor",
                getMonetColorSetting(context, Settings.Secure.MONET_BASE_ACCENT));
        colorHashMap.put("accentColorLight",
                getMonetColorSetting(context, Settings.Secure.MONET_BASE_ACCENT_LIGHT));

        colorHashMap.put("backgroundColor",
                getMonetColorSetting(context, Settings.Secure.MONET_BACKGROUND));
        colorHashMap.put("backgroundColorLight",
                getMonetColorSetting(context, Settings.Secure.MONET_BACKGROUND_LIGHT));

        colorHashMap.put("backgroundSecondaryColor",
                getMonetColorSetting(context, Settings.Secure.MONET_BACKGROUND_SECONDARY));
        colorHashMap.put("backgroundSecondaryColorLight",
                getMonetColorSetting(context, Settings.Secure.MONET_BACKGROUND_SECONDARY_LIGHT));

        /*
         * We already made sure that we always provide a non-null set of colors inside
         * MonetWatcher (com.android.systemui.dot.MonetWatcher)
         * But to be sure, check again before continuing and generating a simple palette
         */
        if (colorHashMap.get("accentColor") == -1) {
            WallpaperColors colors = WallpaperColors.fromBitmap(getBitmap(context));
            int accentColor;
            if (colors.getSecondaryColor() == null)
                accentColor = colors.getPrimaryColor().toArgb();
            else {
                int secondary = colors.getSecondaryColor().toArgb();
                int primary = colors.getPrimaryColor().toArgb();
                if (getLightness(secondary) < getLightness(primary))
                    accentColor = primary;
                else
                    accentColor = secondary;
            }
            colorHashMap.put("accentColor", accentColor);
            colorHashMap.put("backgroundColor", null);
            colorHashMap.put("backgroundSecondaryColor", null);

            colorHashMap.put("accentColorLight", accentColor);
            colorHashMap.put("backgroundColorLight", null);
            colorHashMap.put("backgroundSecondaryColorLight", null);
        }
        return colorHashMap;
    }

    private static int getMonetColorSetting(Context context, String settingsName) {
        String color = Settings.Secure.getString(context.getContentResolver(), settingsName);
        return color == null || color.equals("-1") ? -1 : Integer.parseInt(color);
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
        return accent == null || accent.equals("-1");
    }

    private static Bitmap getBitmap(Context context) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        @SuppressLint("MissingPermission") ParcelFileDescriptor pfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM);
        Bitmap bitmap;
        if (pfd != null) {
            bitmap = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
        } else {
            bitmap = drawableToBitmap(wallpaperManager.getDrawable());
        }
        return bitmap;
    }

    @ColorInt
    private static int getColorAttrDefaultColor(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        @ColorInt int colorAccent = ta.getColor(0, 0);
        ta.recycle();
        return colorAccent;
    }

    private static int getLightCousinColor(int darkColor) {
        return adjustAlpha(ColorUtils.blendARGB(darkColor, Color.WHITE, DEFAULT_LIGHT_ALTERATION), 1f);
    }

    private static int getDarkCousinColor(int lightColor) {
        return adjustAlpha(ColorUtils.blendARGB(lightColor, Color.BLACK, DEFAULT_DARK_ALTERATION), 1f);
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bit;
        if (drawable instanceof BitmapDrawable && ((BitmapDrawable) drawable).getBitmap() != null) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            if (drawable.getIntrinsicWidth() > 0 && drawable.getIntrinsicHeight() > 0) {
                bit = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            } else {
                bit = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            }
            Bitmap bitmap = bit;
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

    private static double getLightness(int color) {
        float[] hsl = Utils.colorToHSL(color);
        return hsl[2];
    }

    /** @hide */
    private static class Utils {

        public static @ColorInt int lighten(@ColorInt int color, float value) {
            float[] hsl = colorToHSL(color);
            hsl[2] += value / 100;
            hsl[2] = Math.max(0f, Math.min(hsl[2], 1f));
            return HSLToColor(hsl);
        }

        public static @NonNull @Size(3) float[] colorToHSL(@ColorInt int color) {
            return colorToHSL(color, new float[3]);
        }

        public static @NonNull @Size(3) float[] colorToHSL(@ColorInt int color, @NonNull @Size(3) float[] hsl) {
            final float r = Color.red(color) / 255f;
            final float g = Color.green(color) / 255f;
            final float b = Color.blue(color) / 255f;

            final float max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
            hsl[2] = (max + min) / 2;

            if (max == min) {
                hsl[0] = hsl[1] = 0;
            } else {
                float d = max - min;
                //noinspection Range
                hsl[1] = (hsl[2] > 0.5f) ? d / (2 - max - min) : d / (max + min);
                if (max == r) hsl[0] = (g - b) / d + (g < b ? 6 : 0);
                else if (max == g) hsl[0] = (b - r) / d + 2;
                else if (max == b) hsl[0] = (r - g) / d + 4;
                hsl[0] /= 6;
            }

            return hsl;
        }

        public static @ColorInt int HSLToColor(@NonNull @Size(3) float[] hsl) {
            float r, g, b;

            final float h = hsl[0];
            final float s = hsl[1];
            final float l = hsl[2];

            if (s == 0) {
                r = g = b = l;
            } else {
                float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
                float p = 2 * l - q;
                r = hue2rgb(p, q, h + 1f/3);
                g = hue2rgb(p, q, h);
                b = hue2rgb(p, q, h - 1f/3);
            }

            return Color.rgb((int) (r*255), (int) (g*255), (int) (b*255));
        }

        private static float hue2rgb(float p, float q, float t) {
            if(t < 0) t += 1;
            if(t > 1) t -= 1;
            if(t < 1f/6) return p + (q - p) * 6 * t;
            if(t < 1f/2) return q;
            if(t < 2f/3) return p + (q - p) * (2f/3 - t) * 6;
            return p;
        }
    }

}
