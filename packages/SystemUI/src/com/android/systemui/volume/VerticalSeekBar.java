package com.android.systemui.volume;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class VerticalSeekBar extends android.support.v7.widget.AppCompatSeekBar {

    private static final int ROTATION_ANGLE = -90;
    private OnSeekBarChangeListener mOnSeekBarChangeListener;


    public VerticalSeekBar(final Context context) {
        super(context);
    }

    public VerticalSeekBar(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalSeekBar(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected final void onSizeChanged(final int width, final int height, final int oldWidth, final int oldHeight) {
        super.onSizeChanged(height, width, oldHeight, oldWidth);
    }

    @Override
    protected final synchronized void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    protected final void onDraw(@NonNull final Canvas c) {
        c.rotate(ROTATION_ANGLE);
        c.translate(-getHeight(), 0);

        super.onDraw(c);
    }

    @Override
    public final void setOnSeekBarChangeListener(final OnSeekBarChangeListener listener) {
        mOnSeekBarChangeListener = listener;
    }

    @Override
    public final boolean onTouchEvent(@NonNull final MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setProgressInternally(getMax() - (int) (getMax() * event.getY() / getHeight()), true);
                if (mOnSeekBarChangeListener != null) {
                    mOnSeekBarChangeListener.onStartTrackingTouch(this);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                setProgressInternally(getMax() - (int) (getMax() * event.getY() / getHeight()), true);
                break;

            case MotionEvent.ACTION_UP:
                setProgressInternally(getMax() - (int) (getMax() * event.getY() / getHeight()), true);
                if (mOnSeekBarChangeListener != null) {
                    mOnSeekBarChangeListener.onStopTrackingTouch(this);
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                if (mOnSeekBarChangeListener != null) {
                    mOnSeekBarChangeListener.onStopTrackingTouch(this);
                }
                break;

            default:
                break;
        }

        return true;
    }

    public final void setProgressInternally(final int progress, final boolean fromUser) {
        if (progress != getProgress()) {
            super.setProgress(progress);
            if (mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener.onProgressChanged(this, progress, fromUser);
            }
        }
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }

    @Override
    public final void setProgress(final int progress) {
        setProgressInternally(progress, false);
    }
}