package com.android.systemui.dot;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.R;
public class LockscreenClockController extends LinearLayout {

    static int clock_Type = 0;

    public LockscreenClockController(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        inflate(context, R.layout.lockscreen_controller, this);
        setClock(clock_Type);
    }

    public void setClock(int clock) {
        View dot_clock = findViewById(R.id.clock_0);
        View stock_clock = findViewById(R.id.clock_1);
        switch (clock) {
            case 0:
                stock_clock.setVisibility(GONE);
                dot_clock.setVisibility(VISIBLE);
                break;
            case 1:
                stock_clock.setVisibility(VISIBLE);
                dot_clock.setVisibility(GONE);
                break;
        }
    }

    public static int setClock_type(int val) {
        return clock_Type = val;
    }
    public static int getClock_Type() {
        return clock_Type;
    }
}

