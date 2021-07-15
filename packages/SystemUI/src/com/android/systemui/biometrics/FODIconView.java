/*
 * Copyright (C) 2021 ExtendedUI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.biometrics;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

import com.android.systemui.R;

public class FODIconView extends ImageView {

    private final String FOD_ICON_ANIMATION_SETTING = "fod_icon_animation";

    private AnimationDrawable iconAnim;

    private boolean mIsFODIconAnimated;
    private boolean mIsKeyguard;

    private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
    private final WindowManager mWindowManager;

    private int mPositionX;
    private int mPositionY;

    public FODIconView(Context context, int size, int x, int y) {
        super(context);

        mPositionX = x;
        mPositionY = y;
        mWindowManager = (WindowManager) context.getSystemService(WindowManager.class);

        setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        context.getResources();
        mParams.height = size;
        mParams.width = size;
        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.packageName = "android";
        mParams.type = WindowManager.LayoutParams.TYPE_DISPLAY_OVERLAY;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        mParams.gravity = Gravity.TOP | Gravity.LEFT;
        mParams.setTitle("Fingerprint on display icon");
        mWindowManager.addView(this, mParams);
        mIsFODIconAnimated = isAnimationEnabled();
        setIsAnimationEnabled(mIsFODIconAnimated);
        hide();
    }

    public void hide() {
        setVisibility(GONE);
        if (iconAnim != null && mIsFODIconAnimated) {
            clearAnimation();
            iconAnim.stop();
            iconAnim.selectDrawable(0);
        }
    }

    public void show() {
        setIsAnimationEnabled(isAnimationEnabled());
        setVisibility(VISIBLE);
        AnimationDrawable animationDrawable = iconAnim;
        if (animationDrawable != null && mIsFODIconAnimated && mIsKeyguard) {
            animationDrawable.start();
        }
    }

    public void updatePosition(int x, int y) {
        mPositionX = x;
        mPositionY = y;
        WindowManager.LayoutParams layoutParams = mParams;
        layoutParams.x = x;
        layoutParams.y = y;
        mWindowManager.updateViewLayout(this, layoutParams);
    }

    public void setIsAnimationEnabled(boolean enabled) {
        mIsFODIconAnimated = enabled;
        if (enabled) {
            setImageResource(0);
            setBackgroundResource(R.drawable.fod_icon_anim_0);
            iconAnim = (AnimationDrawable) getBackground();
            return;
        }
        setBackgroundResource(0);
        setImageResource(R.drawable.fod_icon_default);
    }

    public void setIsKeyguard(boolean isKeyguard) {
        mIsKeyguard = isKeyguard;
        if (isKeyguard && !mIsFODIconAnimated) {
            setColorFilter(-1);
        } else if (mIsKeyguard || !mIsFODIconAnimated) {
            setBackgroundTintList(null);
            setColorFilter((ColorFilter) null);
        } else {
            setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#807B7E")));
        }
    }

    private boolean isAnimationEnabled() {
        return Settings.System.getInt(getContext().getContentResolver(), FOD_ICON_ANIMATION_SETTING, 0) != 0;
    }
}