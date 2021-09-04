package com.android.systemui.dot

import android.content.Context
import android.os.AsyncTask
import android.telephony.SubscriptionManager
import android.util.AttributeSet
import android.widget.TextView

import java.text.CharacterIterator
import java.text.StringCharacterIterator

import com.android.systemui.R

import com.android.settingslib.net.DataUsageController

class QSDataUsageView : TextView {

    private var usageController: DataUsageController = DataUsageController(mContext)
    private var subscriptionManager: SubscriptionManager = context.getSystemService(SubscriptionManager::class.java)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        update()
    }

    fun update() {
        Task().execute()
    }
    
    inner class Task: AsyncTask<Void, Void, String>() {

        var resultString = context.getString(R.string.dot_data_unknown)

        override fun doInBackground(vararg params: Void?): String {
            val subInfoList = subscriptionManager.availableSubscriptionInfoList
            val subActive = subscriptionManager.defaultDataSubscriptionInfo
            val unknown = context.getString(R.string.dot_data_unknown)
            if (subActive != null && subInfoList != null) {
                usageController.setSubscriptionId(subActive.subscriptionId)
                resultString = formatSize(usageController.dataUsageInfo.usageLevel)
            }
            return resultString
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            post { 
                text = resultString
            }
        }
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