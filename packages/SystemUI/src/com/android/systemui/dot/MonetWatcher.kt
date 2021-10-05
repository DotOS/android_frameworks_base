package com.android.systemui.dot

import android.app.WallpaperManager
import android.content.Context
import android.content.res.MonetWannabe
import android.os.Handler
import android.provider.Settings

import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.core.WallpaperTypes
import com.kieronquinn.monetcompat.extensions.toArgb
import com.kieronquinn.monetcompat.interfaces.MonetColorsChangedListener
import dev.kdrag0n.monet.colors.Srgb

import dev.kdrag0n.monet.colors.CieLab
import dev.kdrag0n.monet.colors.Illuminants
import dev.kdrag0n.monet.colors.Zcam
import dev.kdrag0n.monet.theme.ZcamDynamicColorScheme
import dev.kdrag0n.monet.theme.ZcamMaterialYouTargets

import kotlin.math.log10
import kotlin.math.pow

class MonetWatcher(private val context: Context) {

    private var monetSystem: MonetCompat
    private var wallpaperManager: WallpaperManager = WallpaperManager.getInstance(context)

    init {
        monetSystem = getMonetCompat()
        monetSystem.addMonetColorsChangedListener(object : MonetColorsChangedListener {
            override fun onMonetColorsChanged(
                monet: MonetCompat,
                monetColors: ZcamDynamicColorScheme,
                isInitialChange: Boolean
            ) {
                update(monetColors)
            }
        }, false)
        wallpaperManager.addOnColorsChangedListener({ _, which ->
            if (which == WallpaperManager.FLAG_LOCK) {
                updateKeyguard()
            }
        }, Handler(context.mainLooper))
        if (MonetWannabe.shouldForceLoad(context)) {
            update()
            updateKeyguard()
        }
    }

    fun forceUpdate() {
        monetSystem = getMonetCompat()
        update()
        updateKeyguard()
    }

    private fun getMonetCompat(): MonetCompat {
        MonetCompat.wallpaperSource = WallpaperTypes.WALLPAPER_SYSTEM
        MonetCompat.wallpaperColorPicker = {
            val userPickedColor = Settings.Secure.getInt(context.contentResolver, Settings.Secure.MONET_WALLPAPER_COLOR_PICKER, -1)
            val wallpaperColor = it?.firstOrNull { color -> color == userPickedColor } ?: it?.firstOrNull()
            if (wallpaperColor != null && userPickedColor != wallpaperColor) {
                Settings.Secure.putString(context.contentResolver, Settings.Secure.MONET_WALLPAPER_COLOR_PICKER, wallpaperColor!!.toString())
            }
            wallpaperColor
        }
        MonetCompat.setup(context)
        val chroma = Settings.Secure.getFloat(context.contentResolver, Settings.Secure.MONET_CHROMA, 1.0f).toDouble()
        val lightness = Settings.Secure.getFloat(context.contentResolver, Settings.Secure.MONET_LIGHTNESS, 425.0f).toDouble()
        return MonetCompat.getInstance(chroma, lightness)
    }

    private fun update() {
        monetSystem.updateMonetColors()
        update(monetSystem.getMonetColors())
    }

    private fun update(colors: ZcamDynamicColorScheme) {
        val accent = colors.accent1[100]?.toArgb()
        val accentLight = colors.accent1[500]?.toArgb()

        val background = colors.neutral1[900]?.toArgb()
        val backgroundLight = colors.neutral1[50]?.toArgb()

        val backgroundSecondary = colors.neutral1[700]?.toArgb()
        val backgroundSecondaryLight = colors.neutral1[100]?.toArgb()

        if (colors.accent2[100] != null && colors.accent2[500] != null) {
            val accentSecondary = colors.accent2[100]?.toArgb()
            val accentSecondaryLight = colors.accent2[500]?.toArgb()
            Settings.Secure.putString(context.contentResolver,
                Settings.Secure.MONET_BASE_ACCENT_SECONDARY, accentSecondary.toString())
            Settings.Secure.putString(context.contentResolver,
                Settings.Secure.MONET_BASE_ACCENT_SECONDARY_LIGHT, accentSecondaryLight.toString())
        } else {
            Settings.Secure.putString(context.contentResolver,
                Settings.Secure.MONET_BASE_ACCENT_SECONDARY, "-1")
            Settings.Secure.putString(context.contentResolver,
                Settings.Secure.MONET_BASE_ACCENT_SECONDARY_LIGHT, "-1")
        }

        if (colors.accent3[100] != null && colors.accent3[500] != null) {
            val accentTertiary = colors.accent3[100]?.toArgb()
            val accentTertiaryLight = colors.accent3[500]?.toArgb()
            Settings.Secure.putString(context.contentResolver,
                Settings.Secure.MONET_BASE_ACCENT_TERTIARY, accentTertiary.toString())
            Settings.Secure.putString(context.contentResolver,
                Settings.Secure.MONET_BASE_ACCENT_TERTIARY_LIGHT, accentTertiaryLight.toString())
        } else {
            Settings.Secure.putString(context.contentResolver,
                Settings.Secure.MONET_BASE_ACCENT_TERTIARY, "-1")
            Settings.Secure.putString(context.contentResolver,
                Settings.Secure.MONET_BASE_ACCENT_TERTIARY_LIGHT, "-1")
        }

        Settings.Secure.putString(context.contentResolver,
            Settings.Secure.MONET_BASE_ACCENT, accent.toString())
        Settings.Secure.putString(context.contentResolver,
            Settings.Secure.MONET_BASE_ACCENT_LIGHT, accentLight.toString())
        Settings.Secure.putString(context.contentResolver,
            Settings.Secure.MONET_BACKGROUND, background.toString())
        Settings.Secure.putString(context.contentResolver,
            Settings.Secure.MONET_BACKGROUND_LIGHT, backgroundLight.toString())
        Settings.Secure.putString(context.contentResolver,
            Settings.Secure.MONET_BACKGROUND_SECONDARY, backgroundSecondary.toString())
        Settings.Secure.putString(context.contentResolver,
            Settings.Secure.MONET_BACKGROUND_SECONDARY_LIGHT, backgroundSecondaryLight.toString())
    }

    private fun updateKeyguard() {
        val wallpaperColors = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_LOCK)
        val chroma = Settings.Secure.getFloat(context.contentResolver, Settings.Secure.MONET_CHROMA, 1.0f).toDouble()
        val lightness = Settings.Secure.getInt(context.contentResolver, Settings.Secure.MONET_LIGHTNESS, 425)
        val cond = createZcamViewingConditions(parseWhiteLuminanceUser(lightness))
        wallpaperColors?.let {
            val primaryColor = it!!.primaryColor.toArgb()
            val colors = ZcamDynamicColorScheme(ZcamMaterialYouTargets(chroma, true, cond), Srgb(primaryColor), chroma, cond)
            val accent = colors.accent1[100]?.toArgb()
            val accentLight = colors.accent1[500]?.toArgb()

            val background = colors.neutral1[900]?.toArgb()
            val backgroundLight = colors.neutral1[50]?.toArgb()

            val backgroundSecondary = colors.neutral1[700]?.toArgb()
            val backgroundSecondaryLight = colors.neutral1[100]?.toArgb()

            Settings.Secure.putString(context.contentResolver,
                Settings.Secure.MONET_BASE_KEYGUARD_ACCENT, accent.toString())
            Settings.Secure.putString(context.contentResolver,
                Settings.Secure.MONET_BASE_KEYGUARD_ACCENT_LIGHT, accentLight.toString())
            Settings.Secure.putString(context.contentResolver,
                Settings.Secure.MONET_KEYGUARD_BACKGROUND, background.toString())
            Settings.Secure.putString(context.contentResolver,
                Settings.Secure.MONET_KEYGUARD_BACKGROUND_LIGHT, backgroundLight.toString())
            Settings.Secure.putString(context.contentResolver,
                Settings.Secure.MONET_KEYGUARD_BACKGROUND_SECONDARY, backgroundSecondary.toString())
            Settings.Secure.putString(context.contentResolver,
                Settings.Secure.MONET_KEYGUARD_BACKGROUND_SECONDARY_LIGHT, backgroundSecondaryLight.toString())
        }
    }

    private fun parseWhiteLuminanceUser(userValue: Int): Double {
        val userSrc = userValue.toDouble() / WHITE_LUMINANCE_USER_MAX
        val userInv = 1.0 - userSrc
        return (10.0).pow(userInv * log10(WHITE_LUMINANCE_MAX))
            .coerceAtLeast(WHITE_LUMINANCE_MIN)
    }

    private fun createZcamViewingConditions(whiteLuminance: Double) = Zcam.ViewingConditions(
        Zcam.ViewingConditions.SURROUND_AVERAGE,
        0.4 * whiteLuminance,
        CieLab(50.0, 0.0, 0.0).toCieXyz().y * whiteLuminance,
        Illuminants.D65 * whiteLuminance, whiteLuminance
    )

    companion object {
        private const val WHITE_LUMINANCE_MIN = 1.0
        private const val WHITE_LUMINANCE_MAX = 10000.0
        private const val WHITE_LUMINANCE_USER_MAX = 1000
    }
}