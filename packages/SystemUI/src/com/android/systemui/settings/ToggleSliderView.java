/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.systemui.settings;

import android.annotation.ColorInt;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.settingslib.Utils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;

public class ToggleSliderView extends RelativeLayout implements ToggleSlider {
    private Listener mListener;
    private boolean mTracking;

    private CompoundButton mToggle;
    private ToggleSeekBar mSlider;
    private TextView mLabel;

    private ToggleSliderView mMirror;
    private BrightnessMirrorController mMirrorController;

    private CustomSettingsObserver mCustomSettingsObserver;
    private class CustomSettingsObserver extends ContentObserver {
        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = getContext().getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QS_PANEL_BG_USE_NEW_TINT),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.QS_PANEL_BG_USE_NEW_TINT))) {
                updateResources();
            }
        }

        public void update() {
            updateResources();
        }
    }

    public ToggleSliderView(Context context) {
        this(context, null);
    }

    public ToggleSliderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToggleSliderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        View.inflate(context, R.layout.status_bar_toggle_slider, this);

        final Resources res = context.getResources();
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.ToggleSliderView, defStyle, 0);

        mToggle = findViewById(R.id.toggle);
        mToggle.setOnCheckedChangeListener(mCheckListener);

        mSlider = findViewById(R.id.slider);
        mSlider.setOnSeekBarChangeListener(mSeekListener);

        mCustomSettingsObserver = new CustomSettingsObserver(new Handler(context.getMainLooper()));
        // Expose BrightnessSlider's progressDrawable
        if (a.getDrawable(R.styleable.ToggleSliderView_progressDrawable) != null) {
            mSlider.setProgressDrawable(a.getDrawable(R.styleable.ToggleSliderView_progressDrawable));
            mCustomSettingsObserver.observe();
            mCustomSettingsObserver.update();
            mSlider.setThumb(null);
            mSlider.setPadding(0, 0, 0, 0);
        }
        mLabel = findViewById(R.id.label);
        mLabel.setText(a.getString(R.styleable.ToggleSliderView_text));

        mSlider.setAccessibilityLabel(getContentDescription().toString());

        a.recycle();
    }

    private void updateResources() {
        boolean setQsUseNewTint = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.QS_PANEL_BG_USE_NEW_TINT, 1, UserHandle.USER_CURRENT) == 1;
        if (setQsUseNewTint)
            mSlider.setProgressTintList(ColorStateList.valueOf(adjustAlpha(Utils.getColorAccent(getContext()).getColors()[0], 0.4f)));
        else
            mSlider.setProgressTintList(Utils.getColorAccent(getContext()));
    }

    @ColorInt
    private static int adjustAlpha(@ColorInt int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public void setMirror(ToggleSliderView toggleSlider) {
        mMirror = toggleSlider;
        if (mMirror != null) {
            mMirror.setChecked(mToggle.isChecked());
            mMirror.setMax(mSlider.getMax());
            mMirror.setValue(mSlider.getProgress());
        }
    }

    public void setMirrorController(BrightnessMirrorController c) {
        mMirrorController = c;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mListener != null) {
            mListener.onInit(this);
        }
    }

    public void setEnforcedAdmin(RestrictedLockUtils.EnforcedAdmin admin) {
        mToggle.setEnabled(admin == null);
        mSlider.setEnabled(admin == null);
        mSlider.setEnforcedAdmin(admin);
    }

    public void setOnChangedListener(Listener l) {
        mListener = l;
    }

    @Override
    public void setChecked(boolean checked) {
        mToggle.setChecked(checked);
    }

    @Override
    public boolean isChecked() {
        return mToggle.isChecked();
    }

    @Override
    public void setMax(int max) {
        mSlider.setMax(max);
        if (mMirror != null) {
            mMirror.setMax(max);
        }
    }

    @Override
    public int getMax() {
        return mSlider.getMax();
    }

    @Override
    public void setValue(int value) {
        mSlider.setProgress(value);
        if (mMirror != null) {
            mMirror.setValue(value);
        }
    }

    @Override
    public int getValue() {
        return mSlider.getProgress();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mMirror != null) {
            MotionEvent copy = ev.copy();
            mMirror.dispatchTouchEvent(copy);
            copy.recycle();
        }
        return super.dispatchTouchEvent(ev);
    }

    private final OnCheckedChangeListener mCheckListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton toggle, boolean checked) {
            mSlider.setEnabled(!checked);

            if (mListener != null) {
                mListener.onChanged(
                        ToggleSliderView.this, mTracking, checked, mSlider.getProgress(), false);
            }

            if (mMirror != null) {
                mMirror.mToggle.setChecked(checked);
            }
        }
    };

    private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mListener != null) {
                mListener.onChanged(
                        ToggleSliderView.this, mTracking, mToggle.isChecked(), progress, false);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mTracking = true;

            if (mListener != null) {
                mListener.onChanged(ToggleSliderView.this, mTracking, mToggle.isChecked(),
                        mSlider.getProgress(), false);
            }

            mToggle.setChecked(false);

            if (mMirrorController != null) {
                mMirrorController.showMirror();
                mMirrorController.setLocation((View) getParent());
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mTracking = false;

            if (mListener != null) {
                mListener.onChanged(ToggleSliderView.this, mTracking, mToggle.isChecked(),
                        mSlider.getProgress(), true);
            }

            if (mMirrorController != null) {
                mMirrorController.hideMirror();
            }
        }
    };
}

