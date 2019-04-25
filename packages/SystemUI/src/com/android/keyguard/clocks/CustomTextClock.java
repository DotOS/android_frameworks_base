/*
**
** Copyright 2019, Pearl Project
** Copyright 2019, Descendant
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

package com.android.keyguard.clocks;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.format.DateUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import android.provider.Settings;
import android.app.WallpaperManager;
import android.graphics.Color;
import android.app.WallpaperColors;
import android.support.v7.graphics.Palette;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import java.lang.NullPointerException;
import java.lang.IllegalStateException;
import android.graphics.Paint;
import android.os.ParcelFileDescriptor;
import android.graphics.BitmapFactory;

import com.android.internal.util.ArrayUtils;
import com.android.keyguard.clocks.ColorText;
import com.android.keyguard.clocks.LangGuard;

import java.lang.String;
import java.util.Locale;
import java.util.TimeZone;

import com.android.systemui.R;

public class CustomTextClock extends TextView {

    private String[] TensString = getResources().getStringArray(R.array.TensString);
    private String[] UnitsString = getResources().getStringArray(R.array.UnitsString);
    private String[] TensStringH = getResources().getStringArray(R.array.TensStringH);
    private String[] UnitsStringH = getResources().getStringArray(R.array.UnitsStringH);
    private String[] langExceptions = getResources().getStringArray(R.array.langExceptions);
    private String curLang = Locale.getDefault().getLanguage();
    private String topText = getResources().getString(R.string.custom_text_clock_top_text_default);
    private String highNoonFirstRow = getResources().getString(R.string.high_noon_first_row);
    private String highNoonSecondRow = getResources().getString(R.string.high_noon_second_row);

    private Time mCalendar;

    private boolean mAttached;

    private int handType;

    private Context mContext;

    private boolean h24;

    public CustomTextClock(Context context) {
        this(context, null);
    }

    public CustomTextClock(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.CustomTextClock);

        handType = a.getInteger(R.styleable.CustomTextClock_HandType, 2);

        mContext = context;
        mCalendar = new Time();


    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();

            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            filter.addAction(Intent.ACTION_LOCALE_CHANGED);

            // OK, this is gross but needed. This class is supported by the
            // remote views machanism and as a part of that the remote views
            // can be inflated by a context for another user without the app
            // having interact users permission - just for loading resources.
            // For exmaple, when adding widgets from a user profile to the
            // home screen. Therefore, we register the receiver as the current
            // user not the one the context is for.
            getContext().registerReceiverAsUser(mIntentReceiver,
                    android.os.Process.myUserHandle(), filter, null, getHandler());

        }

        // NOTE: It's safe to do these after registering the receiver since the receiver always runs
        // in the main thread, therefore the receiver can't run before this method returns.

        // The time zone may have changed while the receiver wasn't registered, so update the Time
        mCalendar = new Time();

        // Make sure we update to the current time
        onTimeChanged();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            getContext().unregisterReceiver(mIntentReceiver);
            mAttached = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (handType == 2) {
            //setText(topText);
            setTextColor(ColorText.getWallColor(mContext));
        }
    }

    private void onTimeChanged() {
        mCalendar.setToNow();
        h24 = DateFormat.is24HourFormat(getContext());

        int hour = mCalendar.hour;
        int minute = mCalendar.minute;

        if (!h24) {
            if (hour > 12) {
                hour = hour - 12;
            }
        }

        switch(handType){
            case 0:
                if (hour == 12 && minute == 0) {
                setText(highNoonFirstRow);
                } else {
                setText(getIntStringHour(hour));
                }
                break;
            case 1:
                if (hour == 12 && minute == 0) {
                setText(R.string.high_noon_second_row);
                } else {
                setText(getIntStringMin(minute));
                }
                break;
            default:
                break;
        }

        updateContentDescription(mCalendar, getContext());
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
            }
            if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                TensString = getResources().getStringArray(R.array.TensString);
                UnitsString = getResources().getStringArray(R.array.UnitsString);
                TensStringH = getResources().getStringArray(R.array.TensStringH);
                UnitsStringH = getResources().getStringArray(R.array.UnitsStringH);
                curLang = Locale.getDefault().getLanguage();
                topText = getResources().getString(R.string.custom_text_clock_top_text_default);
                highNoonFirstRow = getResources().getString(R.string.high_noon_first_row);
                highNoonSecondRow = getResources().getString(R.string.high_noon_second_row);
            }
            onTimeChanged();

            invalidate();
        }
    };

    private void updateContentDescription(Time time, Context mContext) {
        final int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR;
        String contentDescription = DateUtils.formatDateTime(mContext,
                time.toMillis(false), flags);
        setContentDescription(contentDescription);
    }

    private String getIntStringHour (int num) {
        int tens, units;
        String NumString = "";
        if(num >= 20) {
            units = num % 10 ;
            tens =  num / 10;
            if ( units == 0 ) {
                NumString = TensStringH[tens];
            } else {
                if (LangGuard.isAvailable(langExceptions,curLang)) {
                    NumString = LangGuard.evaluateExMin(curLang, units, TensString, UnitsString, tens);
                } else {
                    NumString = TensString[tens]+" "+UnitsString[units].substring(2, UnitsString[units].length());
                }
            }
        } else if (num < 20 ) {
            NumString = UnitsStringH[num];
        }

        return NumString;
    }

    private String getIntStringMin (int num) {
        int tens, units;
        String NumString = "";
        if(num >= 20) {
            units = num % 10 ;
            tens =  num / 10;
            if ( units == 0 ) {
                NumString = TensString[tens];
            } else {
                if (LangGuard.isAvailable(langExceptions,curLang)) {
                    NumString = LangGuard.evaluateExMin(curLang, units, TensString, UnitsString, tens);
                } else {
                    NumString = TensString[tens]+" "+UnitsString[units].substring(2, UnitsString[units].length());
                }
            }
        } else if (num < 10 ) {
            NumString = UnitsString[num];
        } else if (num >= 10 && num < 20) {
            NumString = UnitsString[num];
        }

        return NumString;
    }

    private boolean langExEval (String langVal) {
        return (ArrayUtils.contains(langExceptions, langVal) ? true : false);
    }

    private Bitmap drawEmpty() {
        Bitmap convertedBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(convertedBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        canvas.drawPaint(paint);
        return convertedBitmap;
    }
}

