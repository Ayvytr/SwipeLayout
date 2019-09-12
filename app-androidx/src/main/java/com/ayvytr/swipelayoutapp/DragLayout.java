package com.ayvytr.swipelayoutapp;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.ayvytr.logger.L;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.customview.widget.ViewDragHelper;

/**
 * demo
 * @author admin
 */
public class DragLayout extends FrameLayout {

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            L.e(child, pointerId);
            return true;
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            L.e(releasedChild, xvel, yvel);
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            L.e(child, left, dx);
            if(left < 0) {
                return 0;
            }

            int right = getWidth() - child.getWidth();
            if(left > right) {
                return right;
            }

            return left;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            L.e(child, top, dy);

            if(top < 0) {
                return 0;
            }

            int bottom = getHeight() - child.getHeight();
            if(top > bottom) {
                return bottom;
            }

            return top;
        }
    };
    private ViewDragHelper viewDragHelper;

    public DragLayout(@NonNull Context context) {
        this(context, null);
    }

    public DragLayout(@NonNull Context context,
                      @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        viewDragHelper = ViewDragHelper.create(this, callback);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DragLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                      int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        viewDragHelper = ViewDragHelper.create(this, callback);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }
}
