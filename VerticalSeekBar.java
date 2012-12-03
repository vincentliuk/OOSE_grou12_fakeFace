package com.group12.oose.fakeface;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

/**
 * This class is for drawing a slide bar vertically,
 * cause in android there is no vertical slide bar.
 * @author JHU Fall2012 OOSE group 12
 */
public class VerticalSeekBar extends SeekBar {

	/**
	 * Constructor1
	 * @param context
	 */
    public VerticalSeekBar(Context context) {
        super(context);
    }
    /**
     * constructor2
     * @param context
     * @param attrs
     * @param defStyle
     */
    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * constructor3
     * @param context
     * @param attrs
     */
    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    /**
     * onSizeChanged between old bar and new bar.
     * @param w, h, oldw, oldh
     * @return
     */
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    /**
     * onDraw draw the rotated 90 degree new bar
     * @param c canvas
     * @return
     */
    protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(), 0);

        super.onDraw(c);
    }

    /**
     * onTouchEvent 
     * @param event
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;

            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }
}
