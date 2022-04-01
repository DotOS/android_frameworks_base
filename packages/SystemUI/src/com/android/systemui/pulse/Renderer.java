/**
 * Copyright (C) 2016-2021 crDroid Android Project
 *
 * @author: Randall Rushing <randall.rushing@gmail.com>
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
 *
 * Base class of things that render eye candy
 *
 */

package com.android.systemui.pulse;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;

public abstract class Renderer implements VisualizerStreamHandler.Listener {
    protected Context mContext;
    protected Handler mHandler;
    protected PulseView mView;
    protected ColorController mColorController;
    protected boolean mIsValidStream;

    private static final long ANIM_FPS_MAX = 40;
    private static final long ANIM_FPS_TO_MILLIS = 1000 / ANIM_FPS_MAX;
    private long mCurrentTime;
    private long mRenderCounter;
    private long mCurrentCounter;

    protected boolean mKeyguardShowing;

    public Renderer(Context context, Handler handler, PulseView view, ColorController colorController) {
        mContext = context;
        mHandler = handler;
        mView = view;
        mColorController = colorController;
        mRenderCounter = System.currentTimeMillis();
    }

    protected final void postInvalidate() {
        mCurrentTime = System.currentTimeMillis();
        mCurrentCounter = mCurrentTime - mRenderCounter;
        if (mCurrentCounter >= ANIM_FPS_TO_MILLIS) {
            mRenderCounter = mCurrentTime;
            mView.postInvalidate();
        }
    }

    public abstract void draw(Canvas canvas);

    @Override
    public void onWaveFormUpdate(byte[] bytes) {}

    @Override
    public void onFFTUpdate(byte[] fft) {}

    public void onVisualizerLinkChanged(boolean linked) {}

    public void destroy() {}

    public void setLeftInLandscape(boolean leftInLandscape) {}

    public void onSizeChanged(int w, int h, int oldw, int oldh) {}

    public void onUpdateColor(int color) {}

    public boolean isValidStream() { return mIsValidStream; }

    public void setKeyguardShowing(boolean showing) {
        mKeyguardShowing = showing;
        onSizeChanged(0, 0, 0, 0);
    }
}
