package com.android.systemui.dot

import android.content.Context
import android.content.res.MonetWannabe
import android.provider.Settings

import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.extensions.toArgb
import com.kieronquinn.monetcompat.interfaces.MonetColorsChangedListener

import dev.kdrag0n.monet.theme.DynamicColorScheme

class MonetWatcher(private val context: Context) {

    init {
        if (MonetWannabe.isMonetEnabled(context)) {
            getMonetCompat().addMonetColorsChangedListener(object : MonetColorsChangedListener {
                override fun onMonetColorsChanged(
                    monet: MonetCompat,
                    monetColors: DynamicColorScheme,
                    isInitialChange: Boolean
                ) {
                    update(monet)
                }
            }, false)
            if (MonetWannabe.shouldForceLoad(context)) update(getMonetCompat())
        }
    }

    fun forceUpdate() {
        update(getMonetCompat())
    }

    private fun getMonetCompat(): MonetCompat {
        MonetCompat.setup(context)
        val chroma = Settings.Secure.getFloat(context.contentResolver, Settings.Secure.MONET_CHROMA, 1.0f).toDouble()
        return MonetCompat.getInstance(chroma)
    }

    private fun update(monet: MonetCompat) {
        val colors = monet.getMonetColors()

        val accent = colors.accent1[100]?.toArgb()
        val accentLight = colors.accent1[500]?.toArgb()

        val background = colors.neutral1[900]?.toArgb()
        val backgroundLight = colors.neutral1[50]?.toArgb()

        val backgroundSecondary = colors.neutral1[700]?.toArgb()
        val backgroundSecondaryLight = colors.neutral1[100]?.toArgb()


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
}