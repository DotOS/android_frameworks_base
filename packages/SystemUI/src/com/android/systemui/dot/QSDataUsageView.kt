package com.android.systemui.dot

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.telephony.SubscriptionManager
import android.text.Html
import android.util.AttributeSet
import android.widget.TextView

import androidx.annotation.ColorInt

import java.text.CharacterIterator
import java.text.StringCharacterIterator

import com.android.systemui.R

import com.android.settingslib.net.DataUsageController

class QSDataUsageView : TextView {

    private var usageController: DataUsageController
    private var subscriptionManager:SubscriptionManager

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
        usageController = DataUsageController(mContext)
        update()
    }

    fun update() {
        val subInfoList = subscriptionManager.availableSubscriptionInfoList
        val subActive = subscriptionManager.defaultDataSubscriptionInfo
        val unknown = context.getString(R.string.dot_data_unknown)
        if (subActive != null && subInfoList != null) {
            usageController.setSubscriptionId(subActive.subscriptionId)
            val updatedStatus = Html.fromHtml(context.getString(R.string.dot_data_used, 
                                        colorToHex(subActive.iconTint), 
                                        formatSize(usageController.dataUsageInfo.usageLevel)))
            if (text != updatedStatus) text = updatedStatus
        } else {
            if (text != unknown) text = unknown
        }
    }

    private fun colorToHex(@ColorInt color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }

    private fun formatSize(bytes: Long): String? {
        val absB = if (bytes == Long.MIN_VALUE) Long.MAX_VALUE else Math.abs(bytes)
        if (absB < 1024) {
            return "$bytes B"
        }
        var value = absB
        val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
        var i = 40
        while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
            value = value shr 10
            ci.next()
            i -= 10
        }
        value *= java.lang.Long.signum(bytes).toLong()
        return String.format("%.1f %cB", value / 1024.0, ci.current())
    }
}