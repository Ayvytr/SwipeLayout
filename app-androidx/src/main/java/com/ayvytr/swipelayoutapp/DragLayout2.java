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
 * @author admin
 */
public class DragLayout2 extends FrameLayout {
    private int mCurrentTop;
    private int mCurrentLeft;

    private ViewDragHelper viewDragHelper;

    public DragLayout2(@NonNull Context context) {
        this(context, null);
    }

    public DragLayout2(@NonNull Context context,
                       @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        viewDragHelper = ViewDragHelper.create(this, callback);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DragLayout2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
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


    private int mDragOriLeft;
    private int mDragOriTop;

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            mDragOriLeft = child.getLeft();
            mDragOriTop = child.getTop();
            L.e(child, pointerId);
            return true;
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            L.e(releasedChild, xvel, yvel);

            if(releasedChild.getWidth() / 2 + mCurrentLeft < getWidth() / 2) {
                viewDragHelper.settleCapturedViewAt(0, mCurrentTop);
            } else {
                viewDragHelper.settleCapturedViewAt(getWidth() - releasedChild.getWidth(), mCurrentTop);
            }
            invalidate();
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            L.e(child, left, dx);
            if(left < 0) {
                mCurrentLeft = left;
                return mCurrentLeft;
            }

            int right = getWidth() - child.getWidth();
            if(left > right) {
                mCurrentLeft = right;
                return mCurrentLeft;
            }

            mCurrentLeft = left;
            return mCurrentLeft;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            L.e(child, top, dy);

            if(top < 0) {
                mCurrentTop = 0;
                return mCurrentTop;
            }

            int bottom = getHeight() - child.getHeight();
            if(top > bottom) {
                mCurrentTop = bottom;
                return mCurrentTop;
            }

            mCurrentTop = top;
            return mCurrentTop;
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            return getWidth() - child.getWidth();
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return getHeight() - child.getHeight();
        }
    };

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(viewDragHelper != null && viewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }
}
