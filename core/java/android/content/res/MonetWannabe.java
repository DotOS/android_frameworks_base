package android.content.res;

import android.annotation.ColorInt;
import android.annotation.NonNull;
import android.annotation.SuppressLint;
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
import android.util.Log;

import com.android.internal.graphics.ColorUtils;
import com.android.internal.graphics.palette.Palette;

/** @hide */
public class MonetWannabe {

    private final Context context;

    private int accentColor;
    private int accentColorBackground;
    private int accentColorOverlayLight;
    private int accentColorOverlayDark;

    public static final int DEFAULT_COLOR_GEN = 16;
    public static final float DEFAULT_LIGHT_ALTERATION = 0.8f;
    public static final float DEFAULT_DARK_ALTERATION = 0.8f;

    /**
     * Palette generation types
     */
    private static final int VIBRANT = 0;
    private static final int LIGHT_VIBRANT = 1;
    private static final int DARK_VIBRANT = 2;
    private static final int DOMINANT = 3;
    private static final int MUTED = 4;
    private static final int LIGHT_MUTED = 5;
    private static final int DARK_MUTED = 6;

    private static final String TAG = "MonetWannabe";

    /**
     * MonetWannabe 1.0
     * [accentColor] - generated from wallpaper
     * [accentColorBackground] - suitable for button backgrounds or inactive state of views
     * [accentColorOverlayLight] - fits best on QSPanel scrim background on light theme
     * [accentColorOverlayDark] - fits best on QSPanel scrim background on dark theme
     */
    public MonetWannabe(@NonNull Context context) {
        this.context = context;
        if (isMonetEnabled(context)) generateColors();
    }

    private void generateColors() {
        boolean isDarkMode = context.getResources().getConfiguration().isNightModeActive();
        accentColor = getAccentSetting() == -1 ? updateMonet(context) : getAccentSetting();
        accentColorBackground = isDarkMode ? manipulateColor(accentColor, 0.6f) : manipulateColor(accentColor, 0.8f);
        accentColorOverlayLight = getLightCousinColor(accentColor);
        accentColorOverlayDark = getDarkCousinColor(accentColor);
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

    /**
     * Generate a new accent based on wallpaper
     */
    public static int updateMonet(@NonNull Context context) {
        int accentColor;
        boolean isDarkMode = context.getResources().getConfiguration().isNightModeActive();
        int colorAmount = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.MONET_COLOR_GEN, DEFAULT_COLOR_GEN);
        Palette p = Palette.from(getBitmap(context)).maximumColorCount(colorAmount).generate();
        Palette.Swatch colorPalette = getPalette(context, p);
        if (colorPalette != null) {
            accentColor = colorPalette.getRgb();
        } else {
            Log.w(TAG, "Swatch not found. Falling back to wallpaper colors.");
            WallpaperColors colors = WallpaperColors.fromBitmap(getBitmap(context));
            if (colors.getSecondaryColor() == null)
                accentColor = colors.getPrimaryColor().toArgb();
            else {
                int secondary = colors.getSecondaryColor().toArgb();
                int primary = colors.getPrimaryColor().toArgb();
                if (getDarkness(secondary) > getDarkness(primary))
                    accentColor = primary;
                else
                    accentColor = secondary;
            }
        }
        if (isDarkMode) {
            accentColor = manipulateColor(accentColor, 2f);
        } else {
            accentColor = manipulateColor(accentColor, 1.6f);
        }
        if (isTooLight(accentColor)) {
            accentColor = ColorUtils.blendARGB(accentColor, Color.BLACK, 0.3f);
        }
        if (isTooDark(accentColor))
            accentColor = ColorUtils.blendARGB(accentColor, Color.WHITE, 0.3f);
        return accentColor;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public int getAccentColorBackground() {
        return accentColorBackground;
    }

    public int getAccentColorOverlayLight() {
        return accentColorOverlayLight;
    }

    public int getAccentColorOverlayDark() {
        return accentColorOverlayDark;
    }

    public static int getInactiveAccent(@NonNull Context context) {
        boolean isDarkMode = context.getResources().getConfiguration().isNightModeActive();
        return adjustAlpha(getColorAttrDefaultColor(context, android.R.attr.colorAccentBackground), isDarkMode ? 0.6f : 0.3f);
    }

    public static boolean isMonetEnabled(@NonNull Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.MONET_ENGINE, 1) == 1;
    }

    public static boolean shouldForceLoad(@NonNull Context context) {
        String accent = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.MONET_BASE_ACCENT);
        return accent == null || accent.equals("-1");
    }

    private int getAccentSetting() {
        String accent = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.MONET_BASE_ACCENT);
        return accent == null || accent.equals("-1") ? -1 : Integer.parseInt(accent);
    }

    private static Palette.Swatch getPalette(@NonNull Context context, @NonNull Palette palette) {
        int paletteType = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.MONET_PALETTE, VIBRANT);
        switch (paletteType) {
            case VIBRANT:
                return palette.getVibrantSwatch();
            case LIGHT_VIBRANT:
                return palette.getLightVibrantSwatch();
            case DARK_VIBRANT:
                return palette.getDarkVibrantSwatch();
            case DOMINANT:
                return palette.getDominantSwatch();
            case MUTED:
                return palette.getMutedSwatch();
            case LIGHT_MUTED:
                return palette.getLightMutedSwatch();
            case DARK_MUTED:
                return palette.getDarkMutedSwatch();
        }
        return null;
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
    public static int adjustAlpha(@ColorInt int color, float factor) {
        return ColorUtils.setAlphaComponent(color, Math.round((float) Color.alpha(color) * factor));
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

    private static double getDarkness(int color) {
        return (double) 1 - (0.299D * (double) Color.red(color) + 0.587D * (double) Color.green(color)) + 0.114D * (double) Color.blue(color) / (double) 255;
    }

    private static boolean isTooLight(int color) {
        return getDarkness(color) < 0.2D;
    }

    private static boolean isTooDark(int color) {
        return getDarkness(color) > 0.8D;
    }

}
