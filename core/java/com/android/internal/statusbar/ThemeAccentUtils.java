/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.internal.statusbar;

import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.os.RemoteException;
import android.util.Log;

public class ThemeAccentUtils {
    public static final String TAG = "ThemeAccentUtils";

    // Vendor overlays to ignore
    public static final String[] BLACKLIST_VENDOR_OVERLAYS = {
        "SysuiDarkTheme",
        "Pixel",
        "DisplayCutoutEmulationCorner",
        "DisplayCutoutEmulationDouble",
        "DisplayCutoutEmulationNarrow",
        "DisplayCutoutEmulationWide",
    };

    private static final String[] ACCENTS = {
        "default_accent", // 0
        "com.accents.red", // 1
        "com.accents.pink", // 2
        "com.accents.purple", // 3
        "com.accents.deeppurple", // 4
        "com.accents.indigo", // 5
        "com.accents.blue", // 6
        "com.accents.lightblue", // 7
        "com.accents.cyan", // 8
        "com.accents.teal", // 9
        "com.accents.green", // 10
        "com.accents.lightgreen", // 11
        "com.accents.lime", // 12
        "com.accents.yellow", // 13
        "com.accents.amber", // 14
        "com.accents.orange", // 15
        "com.accents.deeporange", // 16
        "com.accents.brown", // 17
        "com.accents.grey", // 18
        "com.accents.bluegrey", // 19
        "com.accents.black", // 20
        "com.accents.white", // 21
    };

    private static final String[] DARK_THEMES = {
        "com.android.system.theme.dark", // 0
        "com.android.settings.theme.dark", // 1
        "com.android.systemui.custom.theme.dark", // 2
        "com.google.android.apps.wellbeing.theme.dark", // 3
    };

    private static final String[] BLACK_THEMES = {
        "com.android.system.theme.black", // 0
        "com.android.settings.theme.black", // 1
        "com.android.systemui.theme.black", // 2
        "com.google.android.apps.wellbeing.theme.black", // 3
    };

    private static final String[] NEO_THEMES = {
        "com.android.system.theme.neo", // 0
        "com.android.settings.theme.neo", // 1
        "com.android.systemui.theme.neo", // 2
        "com.google.android.apps.wellbeing.theme.neo", // 3
        "com.du.settings.doze.theme.neo", // 4
    };

    private static final String[] QS_TILE_THEMES = {
        "default", // 0
        "com.android.systemui.qstile.square", // 1
        "com.android.systemui.qstile.roundedsquare", // 2
        "com.android.systemui.qstile.squircle", // 3
        "com.android.systemui.qstile.teardrop", // 4
        "com.android.systemui.qstile.circlegradient", //5
        "com.android.systemui.qstile.circleoutline", //6
        "com.android.systemui.qstile.justicons", //7
    };

    private static final String STOCK_DARK_THEME = "com.android.systemui.theme.dark";

    // Switches theme accent from to another or back to stock
    public static void updateAccents(IOverlayManager om, int userId, int accentSetting) {
        if (accentSetting == 0) {
            unloadAccents(om, userId);
        } else if (accentSetting < 20) {
            try {
                om.setEnabled(ACCENTS[accentSetting],
                        true, userId);
            } catch (RemoteException e) {
                Log.w(TAG, "Can't change theme", e);
            }
        } else if (accentSetting == 20) {
            try {
                // If using a dark, black or Neo UI theme we use the white accent, otherwise use the black accent
                if (isUsingDarkTheme(om, userId) || isUsingBlackTheme(om, userId) || isUsingNeoTheme(om, userId)) {
                    om.setEnabled(ACCENTS[21],
                            true, userId);
                } else {
                    om.setEnabled(ACCENTS[20],
                            true, userId);
                }
            } catch (RemoteException e) {
                Log.w(TAG, "Can't change theme", e);
            }
        }
    }

    // Unload all the theme accents
    public static void unloadAccents(IOverlayManager om, int userId) {
        // skip index 0
        for (int i = 1; i < ACCENTS.length; i++) {
            String accent = ACCENTS[i];
            try {
                om.setEnabled(accent,
                        false /*disable*/, userId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // Check for the dark system theme
    public static boolean isUsingDarkTheme(IOverlayManager om, int userId) {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = om.getOverlayInfo(DARK_THEMES[0],
                    userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return themeInfo != null && themeInfo.isEnabled();
    }

    // Check for the black system theme
    public static boolean isUsingBlackTheme(IOverlayManager om, int userId) {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = om.getOverlayInfo(BLACK_THEMES[0],
                    userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return themeInfo != null && themeInfo.isEnabled();
     }

    // Check for the neo system theme
    public static boolean isUsingNeoTheme(IOverlayManager om, int userId) {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = om.getOverlayInfo(NEO_THEMES[0],
                    userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return themeInfo != null && themeInfo.isEnabled();
     }

    public static void setLightDarkTheme(IOverlayManager om, int userId, boolean useDarkTheme) {
        for (String theme : DARK_THEMES) {
                try {
                    om.setEnabled(theme,
                        useDarkTheme, userId);
                    unfuckBlackWhiteAccent(om, userId);
                    if (useDarkTheme) {
                        unloadStockDarkTheme(om, userId);
                    }
                } catch (RemoteException e) {
                    Log.w(TAG, "Can't change theme", e);
                }
        }
    }

    public static void setLightBlackTheme(IOverlayManager om, int userId, boolean useBlackTheme) {
        for (String theme : BLACK_THEMES) {
                try {
                    om.setEnabled(theme,
                        useBlackTheme, userId);
                    unfuckBlackWhiteAccent(om, userId);
                } catch (RemoteException e) {
                    Log.w(TAG, "Can't change theme", e);
                }
        }
    }

    public static void setLightNeoTheme(IOverlayManager om, int userId, boolean useNeoTheme) {
        for (String theme : NEO_THEMES) {
                try {
                    om.setEnabled(theme,
                        useNeoTheme, userId);
                    unfuckBlackWhiteAccent(om, userId);
                } catch (RemoteException e) {
                    Log.w(TAG, "Can't change theme", e);
                }
        }
    }

    // Check for black and white accent overlays
    public static void unfuckBlackWhiteAccent(IOverlayManager om, int userId) {
        OverlayInfo themeInfo = null;
        try {
            if (isUsingDarkTheme(om, userId) || isUsingBlackTheme(om, userId) || isUsingNeoTheme(om, userId)) {
                themeInfo = om.getOverlayInfo(ACCENTS[20],
                        userId);
                if (themeInfo != null && themeInfo.isEnabled()) {
                    om.setEnabled(ACCENTS[20],
                            false /*disable*/, userId);
                    om.setEnabled(ACCENTS[21],
                            true, userId);
                }
            } else {
                themeInfo = om.getOverlayInfo(ACCENTS[21],
                        userId);
                if (themeInfo != null && themeInfo.isEnabled()) {
                    om.setEnabled(ACCENTS[21],
                            false /*disable*/, userId);
                    om.setEnabled(ACCENTS[20],
                            true, userId);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // Unloads the stock dark theme
    public static void unloadStockDarkTheme(IOverlayManager om, int userId) {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = om.getOverlayInfo(STOCK_DARK_THEME,
                    userId);
            if (themeInfo != null && themeInfo.isEnabled()) {
                om.setEnabled(STOCK_DARK_THEME,
                        false /*disable*/, userId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // Check for the white accent overlay
    public static boolean isUsingWhiteAccent(IOverlayManager om, int userId) {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = om.getOverlayInfo(ACCENTS[26],
                    userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return themeInfo != null && themeInfo.isEnabled();
    }

    // Switches qs tile style to user selected.
    public static void updateTileStyle(IOverlayManager om, int userId, int qsTileStyle) {
        if (qsTileStyle == 0) {
            unlockQsTileStyles(om, userId);
        } else {
            try {
                om.setEnabled(QS_TILE_THEMES[qsTileStyle],
                        true, userId);
            } catch (RemoteException e) {
            }
        }
    }


    // Unload all the qs tile styles
    public static void unlockQsTileStyles(IOverlayManager om, int userId) {
        // skip index 0
        for (int i = 1; i < QS_TILE_THEMES.length; i++) {
            String qstiletheme = QS_TILE_THEMES[i];
            try {
                om.setEnabled(qstiletheme,
                        false /*disable*/, userId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // Check for any QS tile styles overlay
    public static boolean isUsingQsTileStyles(IOverlayManager om, int userId, int qsstyle) {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = om.getOverlayInfo(QS_TILE_THEMES[qsstyle],
                    userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return themeInfo != null && themeInfo.isEnabled();
    }
}
