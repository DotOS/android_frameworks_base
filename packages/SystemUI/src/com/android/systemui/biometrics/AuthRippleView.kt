/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.systemui.biometrics

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import android.view.animation.PathInterpolator
import com.android.internal.graphics.ColorUtils
import com.android.settingslib.Utils
import com.android.systemui.animation.Interpolators
import com.android.systemui.statusbar.charging.DwellRippleShader
import com.android.systemui.statusbar.charging.RippleShader
import com.android.systemui.R

private const val RIPPLE_SPARKLE_STRENGTH: Float = 0.4f

/**
 * Expanding ripple effect
 * - startUnlockedRipple for the transition from biometric authentication success to showing
 * launcher.
 * - startDwellRipple for the ripple expansion out when the user has their finger down on the UDFPS
 * sensor area
 * - retractRipple for the ripple animation inwards to signal a failure
 */
class AuthRippleView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val retractInterpolator = PathInterpolator(.05f, .93f, .1f, 1f)

    private val dwellPulseDuration = 100L
    private val dwellExpandDuration = 2000L - dwellPulseDuration

    private var drawDwell: Boolean = false
    private var drawRipple: Boolean = false

    private var lockScreenColorVal = Color.WHITE
    private val retractDuration = 400L
    private var alphaInDuration: Long = 0
    private var unlockedRippleInProgress: Boolean = false
    private val dwellShader = DwellRippleShader()
    private val dwellPaint = Paint()
    private val rippleShader = RippleShader()
    private val ripplePaint = Paint()
    private var retractAnimator: Animator? = null
    private var dwellPulseOutAnimator: Animator? = null
    private var dwellRadius: Float = 0f
        set(value) {
            dwellShader.maxRadius = value
            field = value
        }
    private var dwellOrigin: PointF = PointF()
        set(value) {
            dwellShader.origin = value
            field = value
        }
    private var radius: Float = 0f
        set(value) {
            rippleShader.radius = value
            field = value
        }
    private var origin: PointF = PointF()
        set(value) {
            rippleShader.origin = value
            field = value
        }

    init {
        rippleShader.color = Utils.getColorAttr(context, android.R.attr.colorAccent).defaultColor
        rippleShader.progress = 0f
        rippleShader.sparkleStrength = RIPPLE_SPARKLE_STRENGTH
        ripplePaint.shader = rippleShader

        dwellShader.color = 0xffffffff.toInt() // default color
        dwellShader.progress = 0f
        dwellShader.distortionStrength = .4f
        dwellPaint.shader = dwellShader
        visibility = GONE
    }

    fun setSensorLocation(location: PointF) {
        origin = location
        radius = maxOf(location.x, location.y, width - location.x, height - location.y).toFloat()
    }

    fun setFingerprintSensorLocation(location: PointF, sensorRadius: Float) {
        origin = location
        radius = maxOf(location.x, location.y, width - location.x, height - location.y).toFloat()
        dwellOrigin = location
        dwellRadius = sensorRadius * 1.5f
    }

    fun setAlphaInDuration(duration: Long) {
        alphaInDuration = duration
    }

    /**
     * Animate ripple inwards back to radius 0
     */
    fun retractRipple() {
        if (retractAnimator?.isRunning == true) {
            return // let the animation finish
        }

        if (dwellPulseOutAnimator?.isRunning == true) {
            val retractRippleAnimator = ValueAnimator.ofFloat(dwellShader.progress, 0f)
                    .apply {
                interpolator = retractInterpolator
                duration = retractDuration
                addUpdateListener { animator ->
                    val now = animator.currentPlayTime
                    dwellShader.progress = animator.animatedValue as Float
                    dwellShader.time = now.toFloat()

                    invalidate()
                }
            }

            val retractAlphaAnimator = ValueAnimator.ofInt(255, 0).apply {
                interpolator = Interpolators.LINEAR
                duration = retractDuration
                addUpdateListener { animator ->
                    dwellShader.color = ColorUtils.setAlphaComponent(
                            dwellShader.color,
                            animator.animatedValue as Int
                    )
                    invalidate()
                }
            }

            retractAnimator = AnimatorSet().apply {
                playTogether(retractRippleAnimator, retractAlphaAnimator)
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        dwellPulseOutAnimator?.cancel()
                        drawDwell = true
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        drawDwell = false
                        resetDwellAlpha()
                    }
                })
                start()
            }
        }
    }

    /**
     * Plays a ripple animation that grows to the dwellRadius with distortion.
     */
    fun startDwellRipple(isDozing: Boolean) {
        if (unlockedRippleInProgress || dwellPulseOutAnimator?.isRunning == true) {
            return
        }

        updateDwellRippleColor(isDozing)

        val dwellPulseOutRippleAnimator = ValueAnimator.ofFloat(0f, .8f).apply {
            interpolator = Interpolators.LINEAR
            duration = dwellPulseDuration
            addUpdateListener { animator ->
                val now = animator.currentPlayTime
                dwellShader.progress = animator.animatedValue as Float
                dwellShader.time = now.toFloat()

                invalidate()
            }
        }

        // slowly animate outwards until we receive a call to retractRipple or startUnlockedRipple
        val expandDwellRippleAnimator = ValueAnimator.ofFloat(.8f, 1f).apply {
            interpolator = Interpolators.LINEAR_OUT_SLOW_IN
            duration = dwellExpandDuration
            addUpdateListener { animator ->
                val now = animator.currentPlayTime
                dwellShader.progress = animator.animatedValue as Float
                dwellShader.time = now.toFloat()

                invalidate()
            }
        }

        dwellPulseOutAnimator = AnimatorSet().apply {
            playSequentially(
                    dwellPulseOutRippleAnimator,
                    expandDwellRippleAnimator
            )
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    retractAnimator?.cancel()
                    visibility = VISIBLE
                    drawDwell = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    drawDwell = false
                    resetRippleAlpha()
                }
            })
            start()
        }
    }

    /**
     * Ripple that bursts outwards from the position of the sensor to the edges of the screen
     */
    fun startUnlockedRipple(onAnimationEnd: Runnable?) {
        if (unlockedRippleInProgress) {
            return // Ignore if ripple effect is already playing
        }

        val rippleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            interpolator = Interpolators.LINEAR_OUT_SLOW_IN
            duration = AuthRippleController.RIPPLE_ANIMATION_DURATION
            addUpdateListener { animator ->
                val now = animator.currentPlayTime
                rippleShader.progress = animator.animatedValue as Float
                rippleShader.time = now.toFloat()

                invalidate()
            }
        }

        val alphaInAnimator = ValueAnimator.ofInt(0, 255).apply {
            duration = alphaInDuration
            addUpdateListener { animator ->
                rippleShader.color = ColorUtils.setAlphaComponent(
                    rippleShader.color,
                    animator.animatedValue as Int
                )
                invalidate()
            }
        }

        val animatorSet = AnimatorSet().apply {
            playTogether(
                rippleAnimator,
                alphaInAnimator
            )
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                val showFpRipple = context.resources.getBoolean(R.bool.config_show_fp_ripple)
                    unlockedRippleInProgress = true
                    rippleShader.shouldFadeOutRipple = true
                    drawRipple = true
                    if (showFpRipple) {
                        visibility = VISIBLE
                    } else {
                        visibility = GONE
                    }
                }

                override fun onAnimationEnd(animation: Animator?) {
                    onAnimationEnd?.run()
                    unlockedRippleInProgress = false
                    drawRipple = false
                    visibility = GONE
                }
            })
        }
        animatorSet.start()
    }

    fun resetRippleAlpha() {
        rippleShader.color = ColorUtils.setAlphaComponent(
                rippleShader.color,
                255
        )
    }

    fun setLockScreenColor(color: Int) {
        lockScreenColorVal = color
        rippleShader.color = lockScreenColorVal
        resetRippleAlpha()
    }

    fun updateDwellRippleColor(isDozing: Boolean) {
        if (isDozing) {
            dwellShader.color = Color.WHITE
        } else {
            dwellShader.color = lockScreenColorVal
        }
        resetDwellAlpha()
    }

    fun resetDwellAlpha() {
        dwellShader.color = ColorUtils.setAlphaComponent(
                dwellShader.color,
                255
        )
    }

    override fun onDraw(canvas: Canvas?) {
        // To reduce overdraw, we mask the effect to a circle whose radius is big enough to cover
        // the active effect area. Values here should be kept in sync with the
        // animation implementation in the ripple shader.
        if (drawDwell) {
            val maskRadius = (1 - (1 - dwellShader.progress) * (1 - dwellShader.progress) *
                    (1 - dwellShader.progress)) * dwellRadius * 2f
            canvas?.drawCircle(dwellOrigin.x, dwellOrigin.y, maskRadius, dwellPaint)
        }

        if (drawRipple) {
            val mask = (1 - (1 - rippleShader.progress) * (1 - rippleShader.progress) *
                    (1 - rippleShader.progress)) * radius * 2f
            canvas?.drawCircle(origin.x, origin.y, mask, ripplePaint)
        }
    }
}
