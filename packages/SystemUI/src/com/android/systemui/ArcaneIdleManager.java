/*
 * Copyright (C) 2019 Descendant
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

package com.android.systemui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

import com.android.systemui.Dependency;

import java.util.ArrayList;
import java.util.List;

public class ArcaneIdleManager {
    static String TAG = "ArcaneIdleManager";

    static Handler h = new Handler();
    static Runnable rStateTwo;
    static Runnable rStateThree;
    static List<ActivityManager.RunningAppProcessInfo> RunningServices;
    static ActivityManager localActivityManager;
    static Context imContext;
    static ContentResolver mContentResolver;
    static List<String> killablePackages;
    static final long IDLE_TIME_NEEDED = 4000000;
    static int ultraSaverStatus;
    static final String[] LOG_MSGS = { "just ran ",
                                       "rStateTwo Immediate!",
                                       "rStateTwo",
                                       "rStateThree",
                                       "alarmTime ",
                                       "realTime " };

    public static void initManager(Context mContext) {
        imContext = mContext;
        killablePackages = new ArrayList<>();
        localActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        mContentResolver = mContext.getContentResolver();

        rStateTwo = new Runnable() {
            public void run() {
                    servicesKiller();
            }
        };
        rStateThree = new Runnable() {
            public void run() {
                haltManager();
            }
        };
    }

    public static void executeManager() {
        String TAG_SUBCLASS = "executeManager ";
        RunningServices = localActivityManager.getRunningAppProcesses();

        if (IDLE_TIME_NEEDED > msTillAlarm(imContext) && msTillAlarm(imContext) != 0) {
            IdleManLog(TAG_SUBCLASS + LOG_MSGS[1]);
            h.postDelayed(rStateTwo,100);
        } else {
            IdleManLog(TAG_SUBCLASS + LOG_MSGS[2]);
            h.postDelayed(rStateTwo,IDLE_TIME_NEEDED /*1hr*/);
        }
        if (msTillAlarm(imContext) != 0) {
            IdleManLog(TAG_SUBCLASS + LOG_MSGS[3]);
            h.postDelayed(rStateThree,(msTillAlarm(imContext) - 900000));
        }
    }

    public static void haltManager() {
        String TAG_SUBCLASS = "haltManager";
        IdleManLog(LOG_MSGS[0] + TAG_SUBCLASS);
        h.removeCallbacks(rStateTwo);
        theAwakening();
    }

    public static void theAwakening() {
        String TAG_SUBCLASS = "theAwakening";
        IdleManLog(LOG_MSGS[0] + TAG_SUBCLASS);
        h.removeCallbacks(rStateThree);
    }

    public static long msTillAlarm(Context imContext) {
        String TAG_SUBCLASS = "msTillAlarm";
        IdleManLog(LOG_MSGS[0] + TAG_SUBCLASS);
        AlarmManager.AlarmClockInfo info =
                ((AlarmManager)imContext.getSystemService(Context.ALARM_SERVICE)).getNextAlarmClock();
        if (info != null) {
            long alarmTime = info.getTriggerTime();
            IdleManLog(TAG_SUBCLASS + LOG_MSGS[4] + Long.toString(alarmTime));
            long realTime = alarmTime - System.currentTimeMillis();
            IdleManLog(TAG_SUBCLASS + LOG_MSGS[5] + Long.toString(realTime));
            return realTime;
        } else {
            return 0;
        }
    }

    public static void servicesKiller() {
        String TAG_SUBCLASS = "servicesKiller";
        IdleManLog(LOG_MSGS[0] + TAG_SUBCLASS);
        localActivityManager = (ActivityManager) imContext.getSystemService(Context.ACTIVITY_SERVICE);
        RunningServices = localActivityManager.getRunningAppProcesses();
        for (int i=0; i < RunningServices.size(); i++) {
          if (!RunningServices.get(i).pkgList[0].toString().contains("com.android.") &&
                !RunningServices.get(i).pkgList[0].toString().equals("android") &&
                !RunningServices.get(i).pkgList[0].toString().contains("google") &&
                !RunningServices.get(i).pkgList[0].toString().equals("google") &&
                !RunningServices.get(i).pkgList[0].toString().contains("instagram") &&
                !RunningServices.get(i).pkgList[0].toString().contains("facebook") &&
                !RunningServices.get(i).pkgList[0].toString().contains("zhihu") &&
                !RunningServices.get(i).pkgList[0].toString().contains("ugc") &&
                !RunningServices.get(i).pkgList[0].toString().contains("GoogleCamera") &&
                !RunningServices.get(i).pkgList[0].toString().contains("settings") &&
                !RunningServices.get(i).pkgList[0].toString().contains("ims")) {
                    localActivityManager.killBackgroundProcesses(RunningServices.get(i).pkgList[0].toString());
            }
        }
    }

    private static void IdleManLog(String msg) {
        if (SystemProperties.getBoolean("arcane.debug", false))
            Log.d(TAG, msg);
    }
}
