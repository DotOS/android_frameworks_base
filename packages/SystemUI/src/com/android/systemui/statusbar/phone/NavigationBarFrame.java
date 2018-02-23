/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.systemui.statusbar.phone;

import static android.view.MotionEvent.ACTION_OUTSIDE;

import android.annotation.AttrRes;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.widget.FrameLayout;

import com.android.internal.util.dot.DOTUtils;
import com.android.systemui.statusbar.policy.DeadZone;

public class NavigationBarFrame extends FrameLayout {

    private boolean mIsDoubleTapEnabled;

    private DeadZone mDeadZone = null;

    private boolean mEnabled = false;

    private GestureDetector mNavDoubleTapToSleep;

    public NavigationBarFrame(@NonNull Context context) {
        super(context);
    }

    public NavigationBarFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
        mNavDoubleTapToSleep = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                DOTUtils.switchScreenOff(context);
                return true;
            }
        });
    }

    public NavigationBarFrame(@NonNull Context context, @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDeadZone(@NonNull DeadZone deadZone) {
        mDeadZone = deadZone;
        mEnabled = true;
    }

    public void disableDeadZone() {
        mEnabled = false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == ACTION_OUTSIDE) {
            if (mDeadZone != null && mEnabled) {
                return mDeadZone.onTouchEvent(event);
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsDoubleTapEnabled && mNavDoubleTapToSleep != null
                && mNavDoubleTapToSleep.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setDoubleTapToSleep(boolean isDoubleTapEnabled) {
        mIsDoubleTapEnabled = isDoubleTapEnabled;
    }
}
