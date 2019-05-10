/*
 * Copyright (C) 2019 AOSiP
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
 * limitations under the License
 */

package com.android.systemui.qs;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.keyguard.CarrierText;
import com.android.settingslib.Utils;
import com.android.settingslib.graph.SignalDrawable;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NetworkController.SignalCallback;

public class QSMobileGroup extends LinearLayout implements SignalCallback {

    private CarrierText mCarrierText;

    private boolean mListening;
    
    private View mMobileGroup;
    private ImageView mMobileSignal;
    private ImageView mMobileRoaming;
    private final int mColorForeground;
    private final CellSignalState mInfo = new CellSignalState();

    public QSMobileGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        mColorForeground = Utils.getColorAttr(context, android.R.attr.colorForeground);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mMobileGroup = findViewById(R.id.mobile_combo);
        mMobileSignal = findViewById(R.id.mobile_signal);
        mMobileRoaming = findViewById(R.id.mobile_roaming);
        mCarrierText = findViewById(R.id.qs_carrier_text);

        setListening(true);
    }

    @Override
    @VisibleForTesting
    public void onDetachedFromWindow() {
        setListening(false);
        super.onDetachedFromWindow();
    }

    public void setListening(boolean listening) {
        if (listening == mListening) {
            return;
        }
        mListening = listening;
        updateListeners();
    }

    private void updateListeners() {
        if (mListening) {
            if (Dependency.get(NetworkController.class).hasVoiceCallingFeature()) {
                Dependency.get(NetworkController.class).addCallback(this);
            }
        } else {
            Dependency.get(NetworkController.class).removeCallback(this);
        }
    }

    private void handleUpdateState() {
        mMobileGroup.setVisibility(mInfo.visible ? View.VISIBLE : View.GONE);
        if (mInfo.visible) {
            mMobileRoaming.setVisibility(mInfo.roaming ? View.VISIBLE : View.GONE);
            mMobileRoaming.setImageTintList(ColorStateList.valueOf(mColorForeground));
            SignalDrawable d = new SignalDrawable(mContext);
            d.setDarkIntensity(QuickStatusBarHeader.getColorIntensity(mColorForeground));
            mMobileSignal.setImageDrawable(d);
            mMobileSignal.setImageLevel(mInfo.mobileSignalIconId);

            StringBuilder contentDescription = new StringBuilder();
            if (mInfo.contentDescription != null) {
                contentDescription.append(mInfo.contentDescription).append(", ");
            }
            if (mInfo.roaming) {
                contentDescription
                        .append(mContext.getString(R.string.data_connection_roaming))
                        .append(", ");
            }
            // TODO: show mobile data off/no internet text for 5 seconds before carrier text
            if (TextUtils.equals(mInfo.typeContentDescription,
                    mContext.getString(R.string.data_connection_no_internet))
                || TextUtils.equals(mInfo.typeContentDescription,
                    mContext.getString(R.string.cell_data_off_content_description))) {
                contentDescription.append(mInfo.typeContentDescription);
            }
            mMobileSignal.setContentDescription(contentDescription);
        }
    }

    @Override
    public void setMobileDataIndicators(NetworkController.IconState statusIcon,
            NetworkController.IconState qsIcon, int statusType,
            int qsType, boolean activityIn, boolean activityOut, int volteId,
            String typeContentDescription,
            String description, boolean isWide, int subId, boolean roaming) {
        mInfo.visible = statusIcon.visible;
        mInfo.mobileSignalIconId = statusIcon.icon;
        mInfo.contentDescription = statusIcon.contentDescription;
        mInfo.typeContentDescription = typeContentDescription;
        mInfo.roaming = roaming;
        handleUpdateState();
    }

    @Override
    public void setNoSims(boolean hasNoSims, boolean simDetected) {
        if (hasNoSims) {
            mInfo.visible = false;
        }
        handleUpdateState();
    }

    private final class CellSignalState {
        boolean visible;
        int mobileSignalIconId;
        public String contentDescription;
        String typeContentDescription;
        boolean roaming;
    }
}
