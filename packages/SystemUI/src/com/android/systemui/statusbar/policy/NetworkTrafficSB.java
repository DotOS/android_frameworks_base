package com.android.systemui.statusbar.policy;

import static com.android.systemui.statusbar.StatusBarIconView.STATE_DOT;
import static com.android.systemui.statusbar.StatusBarIconView.STATE_HIDDEN;
import static com.android.systemui.statusbar.StatusBarIconView.STATE_ICON;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver;
import com.android.systemui.statusbar.StatusIconDisplayable;

public class NetworkTrafficSB extends NetworkTraffic implements DarkReceiver, StatusIconDisplayable {

    public static final String SLOT = "networktraffic";
    private int mVisibleState = -1;
    private boolean mTrafficVisible = false;
    private boolean mSystemIconVisible = true;
    private boolean mKeyguardShowing;

    /*
     *  @hide
     */
    public NetworkTrafficSB(Context context) {
        this(context, null);
    }

    /*
     *  @hide
     */
    public NetworkTrafficSB(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /*
     *  @hide
     */
    public NetworkTrafficSB(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void onDarkChanged(Rect area, float darkIntensity, int tint) {
        if (!mIsEnabled || mLocation == 1) return;
        mTintColor = DarkIconDispatcher.getTint(area, this, tint);
        setTextColor(mTintColor);
        updateTrafficDrawable();
    }

    @Override
    public String getSlot() {
        return SLOT;
    }

    @Override
    public boolean isIconVisible() {
        return mIsEnabled && mLocation == 0;
    }

    @Override
    public int getVisibleState() {
        return mVisibleState;
    }

    @Override
    public void setVisibleState(int state, boolean mIsEnabled) {
        if (state == mVisibleState || !mIsEnabled || !mAttached) {
            return;
        }
        mVisibleState = state;

        switch (state) {
            case STATE_ICON:
                mSystemIconVisible = true;
                break;
            case STATE_DOT:
            case STATE_HIDDEN:
            default:
                mSystemIconVisible = false;
                break;
        }
    }

    @Override
    protected void makeVisible() {
        boolean show = mSystemIconVisible && !mKeyguardShowing && mLocation == 0;
        setVisibility(show ? View.VISIBLE
                : View.GONE);
        mVisible = show;
    }

    @Override
    public void setStaticDrawableColor(int color) {
        mTintColor = color;
        setTextColor(mTintColor);
        updateTrafficDrawable();
    }

    @Override
    public void setDecorColor(int color) {
    }

    @Override
    protected void updateTrafficDrawable() {
        Drawable d = getContext().getDrawable(R.drawable.stat_sys_network_traffic_spacer);
        setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
        setTextColor(mTintColor);
    }

    public void setKeyguardShowing(boolean showing) {
        mKeyguardShowing = showing;
        if (showing) {
            setVisibility(View.GONE);
            mVisible = false;
        } else {
            maybeRestoreVisibility();
        }
    }

    private void maybeRestoreVisibility() {
        if (!mVisible && mIsEnabled && mLocation == 0 && !mKeyguardShowing &&
                mSystemIconVisible && restoreViewQuickly()) {
            setVisibility(View.VISIBLE);
            mVisible = true;
            // then let the traffic handler do its checks
            update();
        }
    }

    public void setTintColor(int color) {
        mTintColor = color;
        updateTrafficDrawable();
    }
}
