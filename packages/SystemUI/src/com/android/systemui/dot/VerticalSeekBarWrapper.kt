/*
 *    Copyright (C) 2020 The dotOS Project
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.android.systemui.dot

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat

class VerticalSeekBarWrapper @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (useViewRotation()) {
            onSizeChangedUseViewRotation(w, h, oldw, oldh)
        } else {
            onSizeChangedTraditionalRotation(w, h, oldw, oldh)
        }
    }

    private fun onSizeChangedTraditionalRotation(w: Int, h: Int, oldw: Int, oldh: Int) {
        val seekBar = childSeekBar
        if (seekBar != null) {
            val hPadding = paddingLeft + paddingRight
            val vPadding = paddingTop + paddingBottom
            val lp = seekBar.layoutParams as LayoutParams
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.height = 0.coerceAtLeast(h - vPadding)
            seekBar.layoutParams = lp
            seekBar.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            val seekBarMeasuredWidth = seekBar.measuredWidth
            seekBar.measure(
                MeasureSpec.makeMeasureSpec(0.coerceAtLeast(w - hPadding), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(0.coerceAtLeast(h - vPadding), MeasureSpec.EXACTLY)
            )
            lp.gravity = Gravity.TOP or Gravity.START
            lp.leftMargin = (0.coerceAtLeast(w - hPadding) - seekBarMeasuredWidth) / 2
            seekBar.layoutParams = lp
        }
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun onSizeChangedUseViewRotation(w: Int, h: Int, oldw: Int, oldh: Int) {
        val seekBar = childSeekBar
        if (seekBar != null) {
            val hPadding = paddingLeft + paddingRight
            val vPadding = paddingTop + paddingBottom
            seekBar.measure(
                MeasureSpec.makeMeasureSpec(0.coerceAtLeast(h - vPadding), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0.coerceAtLeast(w - hPadding), MeasureSpec.AT_MOST)
            )
        }
        applyViewRotation(w, h)
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val seekBar = childSeekBar
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (seekBar != null && widthMode != MeasureSpec.EXACTLY) {
            val seekBarWidth: Int
            val seekBarHeight: Int
            val hPadding = paddingLeft + paddingRight
            val vPadding = paddingTop + paddingBottom
            val innerContentWidthMeasureSpec =
                MeasureSpec.makeMeasureSpec(0.coerceAtLeast(widthSize - hPadding), widthMode)
            val innerContentHeightMeasureSpec =
                MeasureSpec.makeMeasureSpec(0.coerceAtLeast(heightSize - vPadding), heightMode)
            if (useViewRotation()) {
                seekBar.measure(innerContentHeightMeasureSpec, innerContentWidthMeasureSpec)
                seekBarWidth = seekBar.measuredHeight
                seekBarHeight = seekBar.measuredWidth
            } else {
                seekBar.measure(innerContentWidthMeasureSpec, innerContentHeightMeasureSpec)
                seekBarWidth = seekBar.measuredWidth
                seekBarHeight = seekBar.measuredHeight
            }
            val measuredWidth = resolveSizeAndState(seekBarWidth + hPadding, widthMeasureSpec, 0)
            val measuredHeight = resolveSizeAndState(seekBarHeight + vPadding, heightMeasureSpec, 0)
            setMeasuredDimension(measuredWidth, measuredHeight)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    fun applyViewRotation() {
        applyViewRotation(width, height)
    }

    private fun applyViewRotation(w: Int, h: Int) {
        val seekBar = childSeekBar
        if (seekBar != null) {
            val isLTR = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR
            val seekBarMeasuredWidth = seekBar.measuredWidth
            val seekBarMeasuredHeight = seekBar.measuredHeight
            val hPadding = paddingLeft + paddingRight
            val vPadding = paddingTop + paddingBottom
            val hOffset = (0.coerceAtLeast(w - hPadding) - seekBarMeasuredHeight) * 0.5f
            val lp = seekBar.layoutParams
            lp.width = 0.coerceAtLeast(h - vPadding)
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            seekBar.layoutParams = lp
            seekBar.pivotX = (if (isLTR) 0F else 0.coerceAtLeast(h - vPadding).toFloat())
            seekBar.pivotY = 0f
            seekBar.rotation = 270f
            if (isLTR) {
                seekBar.translationX = hOffset
                seekBar.translationY = seekBarMeasuredWidth.toFloat()
            } else {
                seekBar.translationX = -(seekBarMeasuredHeight + hOffset)
                seekBar.translationY = 0f
            }
        }
    }

    private val childSeekBar: VerticalSeekBar?
        get() {
            val child = if (childCount > 0) getChildAt(0) else null
            return if (child is VerticalSeekBar) child else null
        }

    private fun useViewRotation(): Boolean {
        val seekBar = childSeekBar
        return seekBar?.useViewRotation() ?: false
    }
}