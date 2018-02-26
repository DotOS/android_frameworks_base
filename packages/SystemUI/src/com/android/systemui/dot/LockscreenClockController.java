package com.android.systemui.dot;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.android.systemui.R;

public class LockscreenClockController extends LinearLayout {

    int clock_Type = 0;

    public LockscreenClockController(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        View view = inflate(context, R.layout.lockscreen_controller, this);
        View dot_clock = view.findViewById(R.id.clock_0);
        View stock_clock = view.findViewById(R.id.clock_1);
        switch (clock_Type) {
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

    public void setClock_type(int val) {
        clock_Type = val;
    }
    public int getClock_Type() {
        return clock_Type;
    }
}

