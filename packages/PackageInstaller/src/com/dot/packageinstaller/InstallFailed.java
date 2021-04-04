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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
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

/**
 * Installation failed: Return status code to the caller or display failure UI to user
 */
public class InstallFailed extends BottomAlertActivity {
    private static final String LOG_TAG = InstallFailed.class.getSimpleName();

    /**
     * Label of the app that failed to install
     */
    private CharSequence mLabel;

    /**
     * Unhide the appropriate label for the statusCode.
     *
     * @param statusCode The status code from the package installer.
     */
    private void setExplanationFromErrorCode(int statusCode) {
        Log.d(LOG_TAG, "Installation status code: " + statusCode);

        View viewToEnable;
        switch (statusCode) {
            case PackageInstaller.STATUS_FAILURE_BLOCKED:
                viewToEnable = requireViewById(R.id.install_failed_blocked);
                break;
            case PackageInstaller.STATUS_FAILURE_CONFLICT:
                viewToEnable = requireViewById(R.id.install_failed_conflict);
                break;
            case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                viewToEnable = requireViewById(R.id.install_failed_incompatible);
                break;
            case PackageInstaller.STATUS_FAILURE_INVALID:
                viewToEnable = requireViewById(R.id.install_failed_invalid_apk);
                break;
            default:
                viewToEnable = requireViewById(R.id.install_failed);
                break;
        }

        viewToEnable.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int statusCode = getIntent().getIntExtra(PackageInstaller.EXTRA_STATUS,
                PackageInstaller.STATUS_FAILURE);

        if (getIntent().getBooleanExtra(Intent.EXTRA_RETURN_RESULT, false)) {
            int legacyStatus = getIntent().getIntExtra(PackageInstaller.EXTRA_LEGACY_STATUS,
                    PackageManager.INSTALL_FAILED_INTERNAL_ERROR);

            // Return result if requested
            Intent result = new Intent();
            result.putExtra(Intent.EXTRA_INSTALL_RESULT, legacyStatus);
            setResult(Activity.RESULT_FIRST_USER, result);
            finish();
        } else {
            Intent intent = getIntent();
            ApplicationInfo appInfo = intent
                    .getParcelableExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO);
            Uri packageURI = intent.getData();

            // Set header icon and title
            PackageUtil.AppSnippet as;
            PackageManager pm = getPackageManager();

            if ("package".equals(packageURI.getScheme())) {
                as = new PackageUtil.AppSnippet(pm.getApplicationLabel(appInfo),
                        pm.getApplicationIcon(appInfo));
            } else {
                final File sourceFile = new File(packageURI.getPath());
                as = PackageUtil.getAppSnippet(this, appInfo, sourceFile);
            }

            // Store label for dialog
            mLabel = as.label;
            View dialogView = View.inflate(this, R.layout.install_content_view, null);
            ImageView appIcon = dialogView.requireViewById(R.id.app_icon2);
            appIcon.setImageDrawable(as.icon);
            TextView appName = dialogView.requireViewById(R.id.app_name2);
            mAlert.setView(dialogView);
            appName.setText(as.label);
            mAlert.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.done),
                    (ignored, ignored2) -> finish(), null);
            setupAlert();

            Button mOk = mAlert.getButton(DialogInterface.BUTTON_POSITIVE);
            mOk.setBackgroundResource(R.drawable.dialog_sub_background);
            mOk.setTextColor(MonetWannabe.manipulateColor(getColorAttrDefaultColor(this, android.R.attr.colorAccent), 0.6f));
            mOk.setBackgroundTintList(ColorStateList.valueOf(getColorAttrDefaultColor(this, android.R.attr.colorAccent)));
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

            // Show out of space dialog if needed
            if (statusCode == PackageInstaller.STATUS_FAILURE_STORAGE) {
                (new OutOfSpaceDialog()).show(getFragmentManager(), "outofspace");
            }

            // Get status messages
            setExplanationFromErrorCode(statusCode);
        }
    }

    /**
     * Dialog shown when we ran out of space during installation. This contains a link to the
     * "manage applications" settings page.
     */
    public static class OutOfSpaceDialog extends DialogFragment {
        private InstallFailed mActivity;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);

            mActivity = (InstallFailed) context;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(mActivity)
                    .setTitle(R.string.out_of_space_dlg_title)
                    .setMessage(getString(R.string.out_of_space_dlg_text, mActivity.mLabel))
                    .setPositiveButton(R.string.manage_applications, (dialog, which) -> {
                        // launch manage applications
                        Intent intent = new Intent("android.intent.action.MANAGE_PACKAGE_STORAGE");
                        startActivity(intent);
                        mActivity.finish();
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> mActivity.finish())
                    .create();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);

            mActivity.finish();
        }
    }
}
