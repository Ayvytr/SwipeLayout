package com.ayvytr.swipelayout;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.NestedScrollingParent;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

/**
 * 可实现侧滑删除等功能的侧滑布局.
 *
 * @author Ayvytr <a href="https://github.com/Ayvytr" target="_blank">'s GitHub</a>
 * @since 0.1.0
 */
public class SwipeLayout extends ViewGroup {
    private View centerView;
    private View leftView;
    private View rightView;
    private ViewDragHelper viewDragHelper;

    private static final float VELOCITY_THRESHOLD = 1500f;
    private float velocityThreshold = VELOCITY_THRESHOLD;

    private float touchSlop;
    private WeakReference<ObjectAnimator> weakAnimator;
    private final Map<View, Boolean> hackedParents = new WeakHashMap<>();
    private boolean leftSwipeEnabled = true;
    private boolean rightSwipeEnabled = true;

    private static final int TOUCH_STATE_WAIT = 0;
    private static final int TOUCH_STATE_SWIPE = 1;
    private static final int TOUCH_STATE_SKIP = 2;

    private int touchState = TOUCH_STATE_WAIT;
    private float touchX;
    private float touchY;
    private OnStateChangedListener onStateChangedListener;

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(attrs, defStyleAttr, defStyleRes);
    }

    private void initView(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        viewDragHelper = ViewDragHelper.create(this, 1f, mCallback);
        velocityThreshold = TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, VELOCITY_THRESHOLD,
                        getResources().getDisplayMetrics());
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        if(attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SwipeLayout,
                    defStyleAttr, defStyleRes);
            if(a.hasValue(R.styleable.SwipeLayout_swipeEnabled)) {
                leftSwipeEnabled = a.getBoolean(R.styleable.SwipeLayout_swipeEnabled, true);
                rightSwipeEnabled = a.getBoolean(R.styleable.SwipeLayout_swipeEnabled, true);
            }
            if(a.hasValue(R.styleable.SwipeLayout_swipeLeftEnabled)) {
                leftSwipeEnabled = a.getBoolean(R.styleable.SwipeLayout_swipeLeftEnabled, true);
            }
            if(a.hasValue(R.styleable.SwipeLayout_swipeRightEnabled)) {
                rightSwipeEnabled = a.getBoolean(R.styleable.SwipeLayout_swipeRightEnabled, true);
            }

            a.recycle();
        }
    }

    public boolean isClose() {
        return getOffset() == 0;
    }

    public boolean isOpen() {
        return getOffset() != 0;
    }

    public void close() {
        animateReset();
    }

    /**
     * reset swipe-layout state to initial position with animation (200ms)
     */
    public void animateReset() {
        if(centerView != null || getOffset() != 0) {
            if(centerView == null || getOffset() == 0) {
                return;
            }
            runAnimation(centerView.getLeft(), 0);
        }
    }

    /**
     * Swipe with animation to left by right view's width
     * <p>
     * Ignores {@link SwipeLayout#isSwipeEnabled()} and {@link SwipeLayout#isLeftSwipeEnabled()}
     */
    public void animateSwipeLeft() {
        if(centerView != null && rightView != null) {
            int target = -rightView.getWidth();
            runAnimation(getOffset(), target);
        }
    }

    /**
     * Swipe with animation to right by left view's width
     * <p>
     * Ignores {@link SwipeLayout#isSwipeEnabled()} and {@link SwipeLayout#isRightSwipeEnabled()}
     */
    public void animateSwipeRight() {
        if(centerView != null && leftView != null) {
            int target = leftView.getWidth();
            runAnimation(getOffset(), target);
        }
    }

    private void runAnimation(int initialX, int targetX) {
        finishAnimator();
        viewDragHelper.abort();

        ObjectAnimator animator = new ObjectAnimator();
        animator.setTarget(this);
        animator.setPropertyName("offset");
        animator.setInterpolator(new AccelerateInterpolator());
        animator.setIntValues(initialX, targetX);
        animator.setDuration(200);
        animator.start();
        this.weakAnimator = new WeakReference<>(animator);
    }

    private void finishAnimator() {
        if(weakAnimator != null) {
            ObjectAnimator animator = this.weakAnimator.get();
            if(animator != null) {
                this.weakAnimator.clear();
                if(animator.isRunning()) {
                    animator.end();
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        int maxHeight = 0;

        // Find out how big everyone wants to be
        if(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            measureChildren(widthMeasureSpec, heightMeasureSpec);
        } else {
            //find a child with biggest height
            for(int i = 0; i < count; i++) {
                View child = getChildAt(i);
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
            }

            if(maxHeight > 0) {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY);
                measureChildren(widthMeasureSpec, heightMeasureSpec);
            }
        }

        // Find rightmost and bottom-most child
        for(int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if(child.getVisibility() != GONE) {
                int childBottom;

                childBottom = child.getMeasuredHeight();
                maxHeight = Math.max(maxHeight, childBottom);
            }
        }

        maxHeight += getPaddingTop() + getPaddingBottom();
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());

        setMeasuredDimension(resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                resolveSize(maxHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutChildren(left, top, right, bottom);
    }

    private void layoutChildren(int left, int top, int right, int bottom) {
        final int count = getChildCount();

        final int parentTop = getPaddingTop();

        centerView = null;
        leftView = null;
        rightView = null;
        for(int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if(child.getVisibility() == GONE) {
                continue;
            }

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            switch(lp.gravity) {
                case LayoutParams.CENTER:
                    centerView = child;
                    break;

                case LayoutParams.LEFT:
                    leftView = child;
                    break;

                case LayoutParams.RIGHT:
                    rightView = child;
                    break;
            }
        }

        if(centerView == null) {
            throw new RuntimeException("Child view must be added");
        }

        for(int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if(child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft;
                int childTop;

                int orientation = lp.gravity;

                switch(orientation) {
                    case LayoutParams.LEFT:
                        childLeft = centerView.getLeft() - width;
                        break;

                    case LayoutParams.RIGHT:
                        childLeft = centerView.getRight();
                        break;

                    case LayoutParams.CENTER:
                    default:
                        childLeft = child.getLeft();
                        break;
                }
                childTop = parentTop;

                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        public static final int LEFT = -1;
        public static final int RIGHT = 1;
        public static final int CENTER = 0;
        public static final int BRING_TO_CLAMP_NO = -1;

        private int gravity;

        private int bringToClamp = BRING_TO_CLAMP_NO;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray ta = c.obtainStyledAttributes(attrs, R.styleable.SwipeLayout_Layout);

            gravity = ta.getInt(R.styleable.SwipeLayout_Layout_layout_gravity, CENTER);
            bringToClamp = ta
                    .getLayoutDimension(R.styleable.SwipeLayout_Layout_layout_autoOpenDistance,
                            BRING_TO_CLAMP_NO);

            ta.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    public boolean isSwipeEnabled() {
        return leftSwipeEnabled || rightSwipeEnabled;
    }

    public boolean isLeftSwipeEnabled() {
        return leftSwipeEnabled;
    }

    public boolean isRightSwipeEnabled() {
        return rightSwipeEnabled;
    }

    private boolean internalOnInterceptTouchEvent(MotionEvent event) {
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            onTouchBegin(event);
        }
        return viewDragHelper.shouldInterceptTouchEvent(event);
    }

    private void onTouchBegin(MotionEvent event) {
        touchState = TOUCH_STATE_WAIT;
        touchX = event.getX();
        touchY = event.getY();
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return isSwipeEnabled()
                ? internalOnInterceptTouchEvent(event)
                : super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean defaultResult = super.onTouchEvent(event);
        if(!isSwipeEnabled()) {
            return defaultResult;
        }

        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                onTouchBegin(event);
                break;

            case MotionEvent.ACTION_MOVE:
                if(touchState == TOUCH_STATE_WAIT) {
                    float dx = Math.abs(event.getX() - touchX);
                    float dy = Math.abs(event.getY() - touchY);

                    boolean isLeftToRight = (event.getX() - touchX) > 0;

                    if(((isLeftToRight && !leftSwipeEnabled) || (!isLeftToRight && !rightSwipeEnabled))
                            && getOffset() == 0) {

                        return defaultResult;
                    }

                    if(dx >= touchSlop || dy >= touchSlop) {
                        touchState = dy == 0 || dx / dy > 1f ? TOUCH_STATE_SWIPE : TOUCH_STATE_SKIP;
                        if(touchState == TOUCH_STATE_SWIPE) {
                            requestDisallowInterceptTouchEvent(true);

                            hackParents();
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if(touchState == TOUCH_STATE_SWIPE) {
                    unHackParents();
                    requestDisallowInterceptTouchEvent(false);
                }
                touchState = TOUCH_STATE_WAIT;
                break;
            default:
                break;
        }

        if(event.getActionMasked() != MotionEvent.ACTION_MOVE || touchState == TOUCH_STATE_SWIPE) {
            viewDragHelper.processTouchEvent(event);
        }

        return true;
    }

    /**
     * get horizontal offset from initial position
     */
    public int getOffset() {
        return centerView == null ? 0 : centerView.getLeft();
    }

    /**
     * set horizontal offset from initial position
     */
    public void setOffset(int offset) {
        if(centerView != null) {
            offsetChildren(null, offset - centerView.getLeft());
        }
    }

    private void offsetChildren(View skip, int dx) {
        if(dx == 0) {
            return;
        }

        int count = getChildCount();
        for(int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if(child == skip) {
                continue;
            }

            child.offsetLeftAndRight(dx);
            invalidate(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
        }
    }

    private void hackParents() {
        ViewParent parent = getParent();
        while(parent != null) {
            if(parent instanceof NestedScrollingParent) {
                View view = (View) parent;
                hackedParents.put(view, view.isEnabled());
            }
            parent = parent.getParent();
        }
    }

    private void unHackParents() {
        for(Map.Entry<View, Boolean> entry : hackedParents.entrySet()) {
            View view = entry.getKey();
            if(view != null) {
                view.setEnabled(entry.getValue());
            }
        }
        hackedParents.clear();
    }

    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        private int initLeft;

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            initLeft = child.getLeft();
            return true;
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            return getWidth();
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            if(dx > 0) {
                return clampMoveRight(child, left);
            } else {
                return clampMoveLeft(child, left);
            }
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            int dx = releasedChild.getLeft() - initLeft;
            if(dx == 0) {
                return;
            }

            boolean handled = false;
            if(dx > 0) {

                handled = xvel >= 0 ? onMoveRightReleased(releasedChild, dx,
                        xvel) : onMoveLeftReleased(releasedChild, dx, xvel);
            } else {
                handled = xvel <= 0 ? onMoveLeftReleased(releasedChild, dx,
                        xvel) : onMoveRightReleased(releasedChild, dx, xvel);
            }

            if(!handled) {
                startScrollAnimation(releasedChild, releasedChild.getLeft() - centerView.getLeft(),
                        false, dx > 0);
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            offsetChildren(changedView, dx);
        }

        private int clampMoveRight(View child, int left) {
            if(leftView == null) {
                return child == centerView ? Math.min(left, 0) : Math.min(left, getWidth());
            }

            return Math.min(left, child.getLeft() - leftView.getLeft());
        }

        private int clampMoveLeft(View child, int left) {
            if(rightView == null) {
                return child == centerView ? Math.max(left, 0) : Math.max(left, -child.getWidth());
            }

            return Math.max(left,
                    getWidth() - rightView.getLeft() + child.getLeft() - rightView.getWidth());
        }

        private boolean onMoveRightReleased(View child, int dx, float xvel) {

            if(xvel > velocityThreshold) {
                int left = centerView.getLeft() < 0 ? child.getLeft() - centerView
                        .getLeft() : getWidth();
                boolean moveToOriginal = centerView.getLeft() < 0;
                startScrollAnimation(child, clampMoveRight(child, left), !moveToOriginal, true);
                return true;
            }

            if(leftView == null) {
                startScrollAnimation(child, child.getLeft() - centerView.getLeft(), false, true);
                return true;
            }

            LayoutParams lp = getLayoutParams(leftView);

            if(dx > 0 && xvel >= 0 && leftViewClampReached()) {
                if(onStateChangedListener != null) {
                    onStateChangedListener.onChanged(isOpen(), SwipeLayout.this);
                }
                return true;
            }

            if(dx > 0 && xvel >= 0 && lp.bringToClamp != LayoutParams.BRING_TO_CLAMP_NO && leftView
                    .getRight() > lp.bringToClamp) {
                int left = centerView.getLeft() < 0 ? child.getLeft() - centerView
                        .getLeft() : getWidth();
                startScrollAnimation(child, clampMoveRight(child, left), true, true);
                return true;
            }

            return false;
        }

        private boolean onMoveLeftReleased(View child, int dx, float xvel) {
            if(-xvel > velocityThreshold) {
                int left = centerView.getLeft() > 0 ? child.getLeft() - centerView
                        .getLeft() : -getWidth();
                boolean moveToOriginal = centerView.getLeft() > 0;
                startScrollAnimation(child, clampMoveLeft(child, left), !moveToOriginal, false);
                return true;
            }

            if(rightView == null) {
                startScrollAnimation(child, child.getLeft() - centerView.getLeft(), false, false);
                return true;
            }

            LayoutParams lp = getLayoutParams(rightView);

            if(dx < 0 && xvel <= 0 && rightViewClampReached()) {
                if(onStateChangedListener != null) {
                    onStateChangedListener.onChanged(isOpen(), SwipeLayout.this);
                }
                return true;
            }

            if(dx < 0 && xvel <= 0 && lp.bringToClamp != LayoutParams.BRING_TO_CLAMP_NO && rightView
                    .getLeft() + lp.bringToClamp < getWidth()) {
                int left = centerView.getLeft() > 0 ? child.getLeft() - centerView
                        .getLeft() : -getWidth();
                startScrollAnimation(child, clampMoveLeft(child, left), true, false);
                return true;
            }

            return false;
        }

        private void startScrollAnimation(final View view, int targetX, boolean moveToClamp,
                                          boolean toRight) {
            if(viewDragHelper.settleCapturedViewAt(targetX, view.getTop())) {
                ViewCompat.postOnAnimation(view, new Runnable() {
                    @Override
                    public void run() {
                        if(viewDragHelper.continueSettling(true)) {
                            ViewCompat.postOnAnimation(view, this);
                        } else {
                            if(onStateChangedListener != null) {
                                onStateChangedListener.onChanged(isOpen(), SwipeLayout.this);
                            }
                        }
                    }
                });
            }
        }

        private boolean leftViewClampReached() {
            if(leftView == null) {
                return false;
            }
            return leftView.getRight() >= leftView.getWidth();
        }

        private boolean rightViewClampReached() {
            if(rightView == null) {
                return false;
            }

            return rightView.getRight() <= getWidth();
        }

        private LayoutParams getLayoutParams(View view) {
            return (LayoutParams) view.getLayoutParams();
        }
    };

    public void setOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        this.onStateChangedListener = onStateChangedListener;
    }

    /**
     * 状态变化监听器
     */
    public interface OnStateChangedListener {
        /**
         * @param isOpen      true: 侧滑菜单打开了
         * @param swipeLayout {@link SwipeLayout}
         */
        void onChanged(boolean isOpen, SwipeLayout swipeLayout);
    }
}
