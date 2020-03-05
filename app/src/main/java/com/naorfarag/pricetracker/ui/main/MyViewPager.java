package com.naorfarag.pricetracker.ui.main;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

import androidx.viewpager.widget.ViewPager;

import java.util.Objects;


public class MyViewPager extends ViewPager {
    private boolean pagingEnabled = true;
    private float screenY;
    private float screenX;


    public MyViewPager(Context context) {
        super(context);
        setScreenSize();
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScreenSize();
    }

    public void setPagingEnabled(boolean pagingEnabled) {
        this.pagingEnabled = pagingEnabled;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getY(event.getActionIndex()) < screenY / 6)
            return false && super.onInterceptTouchEvent(event);
        else
            return true && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getY(event.getActionIndex()) < screenY / 6)
            return false && super.onTouchEvent(event);
        else
            return true && super.onTouchEvent(event);
    }

    public void setScreenSize() {
        WindowManager windowManager =
                (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager != null ? windowManager.getDefaultDisplay() : null;
        Point outPoint = new Point();
        // include navigation bar
        Objects.requireNonNull(display).getRealSize(outPoint);
        if (outPoint.y > outPoint.x) {
            screenY = outPoint.y;
            screenX = outPoint.x;
        } else {
            screenY = outPoint.x;
            screenX = outPoint.y;
        }
    }
}

