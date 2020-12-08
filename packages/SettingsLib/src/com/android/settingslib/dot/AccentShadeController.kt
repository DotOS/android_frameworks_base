package com.android.settingslib.dot

import android.annotation.ColorInt
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat

import com.android.settingslib.Utils;

class AccentShadeController(val context: Context) {
    @ColorInt
    fun getColorForAccent(): Int {
        val utils = PrivUtils()
        val accent = utils.getAccent(context)
        val nightModeFlags: Int = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        val darkness: Double =
            1 - (0.299 * Color.red(accent) + 0.587 * Color.green(accent) + 0.114 * Color.blue(accent)) / 255
        val isDark = darkness >= 0.5
        var normalizedTextColor: Int =
            if (isDark) utils.getTextColorSecondary(context)
            else utils.getInverseTextColorSecondary(context)
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> normalizedTextColor =
                if (isDark) utils.getTextColorSecondary(context)
                else utils.getInverseTextColorSecondary(context)
            Configuration.UI_MODE_NIGHT_NO -> normalizedTextColor =
                utils.getTextColorSecondary(context)
        }
        return normalizedTextColor
    }

    fun getNormalColor(): Int = PrivUtils().getTextColorSecondary(context)

    private inner class PrivUtils {
        
        fun getTextColor(context: Context): Int {
            return context.resolveColorAttr(android.R.attr.textColorPrimary)
        }

        fun getTextColorSecondary(context: Context): Int {
            return context.resolveColorAttr(android.R.attr.textColorSecondary)
        }

        fun getInverseTextColor(context: Context): Int {
            return context.resolveColorAttr(android.R.attr.textColorPrimaryInverse)
        }

        fun getInverseTextColorSecondary(context: Context): Int {
            return context.resolveColorAttr(android.R.attr.textColorSecondaryInverse)
        }

        fun getAccent(context: Context): Int {
            return Utils.getColorAccent(context).defaultColor
        }

        @ColorInt
        fun Context.resolveColorAttr(@AttrRes colorAttr: Int): Int {
            val resolvedAttr = resolveThemeAttr(colorAttr)
            val colorRes =
                if (resolvedAttr.resourceId != 0) resolvedAttr.resourceId else resolvedAttr.data
            return ContextCompat.getColor(this, colorRes)
        }

        fun Context.resolveThemeAttr(@AttrRes attrRes: Int): TypedValue {
            val typedValue = TypedValue()
            theme.resolveAttribute(attrRes, typedValue, true)
            return typedValue
        }
    }
}
