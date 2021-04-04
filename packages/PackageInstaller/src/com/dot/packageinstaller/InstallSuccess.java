/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.dot.packageinstaller;

import android.annotation.Nullable;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.MonetWannabe;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.widget.ButtonBarLayout;

import java.io.File;
import java.util.List;

/**
 * Finish installation: Return status code to the caller or display "success" UI to user
 */
public class InstallSuccess extends BottomAlertActivity {
    private static final String LOG_TAG = InstallSuccess.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getBooleanExtra(Intent.EXTRA_RETURN_RESULT, false)) {
            // Return result if requested
            Intent result = new Intent();
            result.putExtra(Intent.EXTRA_INSTALL_RESULT, PackageManager.INSTALL_SUCCEEDED);
            setResult(Activity.RESULT_OK, result);
            finish();
        } else {
            Intent intent = getIntent();
            ApplicationInfo appInfo =
                    intent.getParcelableExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO);
            Uri packageURI = intent.getData();

            // Set header icon and title
            PackageUtil.AppSnippet as;
            PackageManager pm = getPackageManager();

            if ("package".equals(packageURI.getScheme())) {
                as = new PackageUtil.AppSnippet(pm.getApplicationLabel(appInfo),
                        pm.getApplicationIcon(appInfo));
            } else {
                File sourceFile = new File(packageURI.getPath());
                as = PackageUtil.getAppSnippet(this, appInfo, sourceFile);
            }

            View dialogView = View.inflate(this, R.layout.install_content_view, null);
            ImageView appIcon = dialogView.requireViewById(R.id.app_icon2);
            appIcon.setImageDrawable(as.icon);
            TextView appName = dialogView.requireViewById(R.id.app_name2);
            mAlert.setView(dialogView);
            appName.setText(as.label);
            mAlert.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.launch), null,
                    null);
            mAlert.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.done),
                    (ignored, ignored2) -> {
                        if (appInfo.packageName != null) {
                            Log.i(LOG_TAG, "Finished installing " + appInfo.packageName);
                        }
                        finish();
                    }, null);
            setupAlert();
            requireViewById(R.id.install_success).setVisibility(View.VISIBLE);

            Button mOk = mAlert.getButton(DialogInterface.BUTTON_POSITIVE);
            Button mCancel = mAlert.getButton(DialogInterface.BUTTON_NEGATIVE);
            mOk.setBackgroundResource(R.drawable.dialog_sub_background);
            mOk.setTextColor(MonetWannabe.manipulateColor(getColorAttrDefaultColor(this, android.R.attr.colorAccent), 0.6f));
            mOk.setBackgroundTintList(ColorStateList.valueOf(getColorAttrDefaultColor(this, android.R.attr.colorAccent)));
            mCancel.setBackgroundResource(R.drawable.dialog_sub_background);
            mCancel.setBackgroundTintList(ColorStateList.valueOf(adjustAlpha(getColorAttrDefaultColor(this, android.R.attr.colorForeground), 0.1f)));
            mCancel.setTextColor(getColorAttrDefaultColor(this, android.R.attr.textColorPrimary));
            ButtonBarLayout buttonBarLayout = ((ButtonBarLayout) mOk.getParent());
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) buttonBarLayout.getLayoutParams();
            lp.width = LinearLayout.LayoutParams.MATCH_PARENT;
            lp.gravity = Gravity.CENTER;
            int defaultMargin = getResources().getDimensionPixelOffset(R.dimen.header_margin_start);
            lp.leftMargin = defaultMargin;
            lp.rightMargin = defaultMargin;
            lp.topMargin = defaultMargin;
            lp.bottomMargin = defaultMargin;
            for (int i = 0; i < buttonBarLayout.getChildCount(); i++) {
                View child = buttonBarLayout.getChildAt(i);
                if (child.getVisibility() != View.VISIBLE) {
                    child.setVisibility(View.GONE);
                } else {
                    LinearLayout.LayoutParams lpb = (LinearLayout.LayoutParams) child.getLayoutParams();
                    lpb.weight = 1;
                    lpb.width = LinearLayout.LayoutParams.MATCH_PARENT;
                    lpb.height = getResources().getDimensionPixelSize(R.dimen.alert_dialog_button_bar_height);
                    lpb.gravity = Gravity.CENTER;
                    int spacing = defaultMargin / 2;
                    if (child.getId() == android.R.id.button1)
                        lpb.leftMargin = spacing;
                    else
                        lpb.rightMargin = spacing;
                }
            }
            buttonBarLayout.setLayoutParams(lp);
            // Enable or disable "launch" button
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(
                    appInfo.packageName);
            boolean enabled = false;
            if (launchIntent != null) {
                List<ResolveInfo> list = getPackageManager().queryIntentActivities(launchIntent,
                        0);
                if (list != null && list.size() > 0) {
                    enabled = true;
                }
            }

            Button launchButton = mAlert.getButton(DialogInterface.BUTTON_POSITIVE);
            if (enabled) {
                launchButton.setOnClickListener(view -> {
                    try {
                        startActivity(launchIntent);
                    } catch (ActivityNotFoundException | SecurityException e) {
                        Log.e(LOG_TAG, "Could not start activity", e);
                    }
                    finish();
                });
            } else {
                launchButton.setEnabled(false);
            }
        }
    }
}
